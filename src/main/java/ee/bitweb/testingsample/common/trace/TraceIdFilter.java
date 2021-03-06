package ee.bitweb.testingsample.common.trace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ee.bitweb.testingsample.common.util.HttpForwardedHeaderParser;
import ee.bitweb.testingsample.common.util.HttpForwardedHeaderParser.ForwardedExtension;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;

@Slf4j
@Order(Integer.MIN_VALUE + 20)
public class TraceIdFilter implements Filter {

    public static final String PATH = "path";
    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String QUERY_STRING = "query_string";
    public static final String USER_AGENT = "user_agent";
    public static final String X_FORWARDED_FOR = "x_forwarded_for";
    public static final String FORWARDED = "forwarded";
    public static final String FORWARDED_BY = "forwarded_by";
    public static final String FORWARDED_FOR = "forwarded_for";
    public static final String FORWARDED_HOST = "forwarded_host";
    public static final String FORWARDED_PROTO = "forwarded_proto";
    public static final String FORWARDED_EXTENSIONS = "forwarded_extensions";

    private static final String USER_AGENT_MISSING = "MISSING";
    private static final String FORWARDED_HEADER = "Forwarded";
    private static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final TraceIdCustomizer customizer;
    private final TraceIdProvider provider;

    private final List<String> sensitiveHeaders = List.of("authorization");

    public TraceIdFilter() {
        this(TraceIdCustomizerImpl.standard());
    }

    public TraceIdFilter(TraceIdCustomizer customizer) {
        this(customizer, new TraceIdProviderImpl(customizer));
    }

    public TraceIdFilter(TraceIdProvider provider) {
        this(TraceIdCustomizerImpl.standard(), provider);
    }

    public TraceIdFilter(TraceIdCustomizer customizer, TraceIdProvider provider) {
        this.customizer = customizer;
        this.provider = provider;
    }

    @Override
    public void doFilter(
            ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            TraceId.set(provider.generate(httpServletRequest));

            MDC.put(PATH, httpServletRequest.getServletPath());
            MDC.put(URL, getUrl(httpServletRequest));
            MDC.put(METHOD, httpServletRequest.getMethod());
            MDC.put(QUERY_STRING, httpServletRequest.getQueryString());
            MDC.put(USER_AGENT, getUserAgent(httpServletRequest));

            addForwardingInfo(httpServletRequest);
            addAdditionalHeaders(httpServletRequest);

            if (response instanceof HttpServletResponse) {
                ((HttpServletResponse) response).addHeader(customizer.getHeaderName(), TraceId.get());
            }

            logAllHeaders(httpServletRequest);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    String getUserAgent(HttpServletRequest httpServletRequest) {
        String header = httpServletRequest.getHeader(HttpHeaders.USER_AGENT);

        return header != null ? header : USER_AGENT_MISSING;
    }

    String getUrl(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        String queryString = request.getQueryString();
        if (queryString == null) return url;

        return url + "?" + queryString;
    }

    void addForwardingInfo(HttpServletRequest request) {
        String forwardedFor = createHeaderValues(request, X_FORWARDED_FOR_HEADER);
        if (forwardedFor != null) {
            MDC.put(X_FORWARDED_FOR, forwardedFor);
        }

        if (request.getHeader(FORWARDED_HEADER) != null) {
            var result = HttpForwardedHeaderParser.parse(request.getHeaders(FORWARDED_HEADER));
            MDC.put(FORWARDED, createHeaderValues(request, FORWARDED_HEADER));

            MDC.put(FORWARDED_BY, String.join("|", result.getBy()));
            MDC.put(FORWARDED_FOR, String.join("|", result.getAFor()));
            MDC.put(FORWARDED_HOST, String.join("|", result.getHost()));
            MDC.put(FORWARDED_PROTO, String.join("|", result.getProto()));
            MDC.put(
                    FORWARDED_EXTENSIONS,
                    result.getExtensions().stream().map(ForwardedExtension::toString).collect(Collectors.joining("|"))
            );
        }
    }

    void addAdditionalHeaders(HttpServletRequest request) {
        for (AdditionalHeader additionalHeader : customizer.getAdditionalHeaders()) {
            String header = createHeaderValues(request, additionalHeader.getHeader());

            if (header != null) {
                MDC.put(additionalHeader.getMdc(), header);
            } else if (log.isDebugEnabled()) {
                log.debug("Header with name '{}' not present in request", additionalHeader.getHeader());
            }
        }
    }

    void logAllHeaders(HttpServletRequest request) {
        if (!log.isDebugEnabled()) return;

        List<String> headers = new ArrayList<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value;

            if (sensitiveHeaders.contains(key)) {
                value = "***";
            } else {
                value = createHeaderValues(request, key);
            }

            headers.add(key + "=[" + value + "]");
        }

        log.debug("Request headers: " + String.join(",", headers));
    }

    private String createHeaderValues(HttpServletRequest request, String key) {
        StringBuilder builder = new StringBuilder();
        Enumeration<String> headerValues = request.getHeaders(key);
        if (!headerValues.hasMoreElements()) return null;

        while (headerValues.hasMoreElements()) {
            if (builder.length() != 0) {
                builder.append("|");
            }

            builder.append(headerValues.nextElement());
        }

        return builder.toString();
    }
}
