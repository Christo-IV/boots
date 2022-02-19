package ee.bitweb.testingsample.domain.datapoint.api;

import ee.bitweb.testingsample.common.trace.TraceIdCustomizerImpl;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;
import org.springframework.transaction.annotation.Transactional;

import static ee.bitweb.testingsample.domain.datapoint.DataPointHelper.create;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class GetByExternalIdIntegrationTests {

    private static final String URI = "/data-points/external-id";

    private static final String REQUEST_ID = "ThisIsARequestId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataPointRepository repository;

    @Test
    @Transactional
    void onValidIdShouldReturnSuccessResponse() throws Exception {
        DataPoint point = repository.save(create(4L));
        mockMvc.perform(createDefaultRequest("external-id-4"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(5)))
                .andExpect(jsonPath("$.id", is(point.getId().intValue())))
                .andExpect(jsonPath("$.externalId", is("external-id-4")))
                .andExpect(jsonPath("$.value", is("some-value-4")))
                .andExpect(jsonPath("$.comment", is("some-comment-4")))
                .andExpect(jsonPath("$.significance", is(0)));
    }

    @Test
    @Transactional
    void OnInvalidShouldReturnNotFound() throws Exception {
        DataPoint point = repository.save(create(4L));
        mockMvc.perform(createDefaultRequest("external-id-none"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.id", is(startsWith(REQUEST_ID))))
                .andExpect(jsonPath("$.message", is("Entity DataPoint not found")))
                .andExpect(jsonPath("$.entity", is("DataPoint")))
                .andExpect(jsonPath("$.criteria[0]", is(hasEntry("field", "externalId"))))
                .andExpect(jsonPath("$.criteria[0]", is(hasEntry("value", "external-id-none"))));
    }

    private MockHttpServletRequestBuilder createDefaultRequest(String param) {
        return get(URI + "/" + param)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TraceIdCustomizerImpl.DEFAULT_HEADER_NAME, REQUEST_ID);
    }
}
