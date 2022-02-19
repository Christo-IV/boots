package ee.bitweb.testingsample.domain.datapoint.api;

import ee.bitweb.testingsample.common.trace.TraceIdCustomizerImpl;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.*;
import org.springframework.boot.test.context.*;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static ee.bitweb.testingsample.domain.datapoint.DataPointHelper.create;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GetByIdIntegrationTests {

    private static final String URI = "/data-points";

    private static final String REQUEST_ID = "ThisIsARequestId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataPointRepository repository;

    @Test
    @Transactional
    void onValidIdShouldReturnSuccessResponse() throws Exception {
        Long dataPoint = 421L;
        Integer id = 1;

        repository.save(create(dataPoint));

        mockMvc.perform(createDefaultRequest(id.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(5)))
                .andExpect(jsonPath("$.id", is(id)))
                .andExpect(jsonPath("$.externalId", is("external-id-" + dataPoint)))
                .andExpect(jsonPath("$.value", is("some-value-" + dataPoint)))
                .andExpect(jsonPath("$.comment", is("some-comment-" + dataPoint)))
                .andExpect(jsonPath("$.significance", is(1)));
    }

    @Test
    @Transactional
    void onInvalidIdShouldReturnNotFound() throws Exception {
        Integer id = 1;

        mockMvc.perform(createDefaultRequest(id.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.id", is(startsWith(REQUEST_ID))))
                .andExpect(jsonPath("$.message", is("Entity DataPoint not found")))
                .andExpect(jsonPath("$.entity", is("DataPoint")))
                .andExpect(jsonPath("$.criteria[0]", is(hasEntry("field", "id"))))
                .andExpect(jsonPath("$.criteria[0]", is(hasEntry("value", id.toString()))));
    }

    @Test
    @Transactional
    void onInvalidNegativeIdShouldReturnNotFound() throws Exception {
        Integer id = -1;

        mockMvc.perform(createDefaultRequest(id.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(4)))
                .andExpect(jsonPath("$.id", is(startsWith(REQUEST_ID))))
                .andExpect(jsonPath("$.message", is("Entity DataPoint not found")))
                .andExpect(jsonPath("$.entity", is("DataPoint")))
                .andExpect(jsonPath("$.criteria[0]", is(hasEntry("field", "id"))))
                .andExpect(jsonPath("$.criteria[0]", is(hasEntry("value", id.toString()))));
    }

    @Test
    @Transactional
    void onMalformedIdShouldReturnBadRequest() throws Exception {
        String id = "Slitherfang";

        mockMvc.perform(createDefaultRequest(id.toString()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(3)))
                .andExpect(jsonPath("$.id", is(startsWith(REQUEST_ID))))
                .andExpect(jsonPath("$.message", is("INVALID_ARGUMENT")))
                .andExpect(jsonPath("$.errors[0]", is(hasEntry("field", "id"))))
                .andExpect(jsonPath("$.errors[0]", is(hasEntry("reason", "InvalidType"))))
                .andExpect(jsonPath("$.errors[0]", is(hasEntry("message", "Request parameter is invalid"))));
    }


    private MockHttpServletRequestBuilder createDefaultRequest(String param) {
        return get(URI + "/" + param)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TraceIdCustomizerImpl.DEFAULT_HEADER_NAME, REQUEST_ID);
    }
}
