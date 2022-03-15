package ee.bitweb.testingsample.domain.datapoint.api;

import ee.bitweb.testingsample.common.trace.TraceIdCustomizerImpl;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.MockServerHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.internal.matchers.StartsWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.verify.VerificationTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockserver.model.HttpRequest.request;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"data-points.external.baseUrl=http://localhost:12347/"}
)
public class ImportIntegrationTestsWithoutMock {
    private static final String URI = "/data-points/import";
    private static final String REQUEST_ID = "ThisIsARequestId";
    private static ClientAndServer externalService;

    @BeforeAll
    static void setup() { externalService = ClientAndServer.startClientAndServer(12347); }

    @BeforeEach
    void beforeEach() { externalService.reset(); }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataPointRepository repository;

    @Test
    @Transactional
    void onRequestShouldRequestDataPointsFromExternalServiceAndPersist() throws Exception {
        repository.save(DataPointHelper.create(1L));
        MockServerHelper.setupGetMockRouteWithString(
                externalService,
                "/data-points",
                200,
                1,
                createExternalServiceResponse(
                        List.of(
                                createExternalServiceResponse(1L),
                                createExternalServiceResponse(2L),
                                createExternalServiceResponse(3L)
                        )
                ).toString()
        );

        mockMvc.perform(createDefaultRequest())
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0]", aMapWithSize(5)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].externalId", is("external-id-1")))
                .andExpect(jsonPath("$[0].value", is("some-value-1")))
                .andExpect(jsonPath("$[0].comment", is("some-comment-1")))
                .andExpect(jsonPath("$[0].significance", is(1)))
                .andExpect(jsonPath("$[0]", aMapWithSize(5)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].externalId", is("external-id-2")))
                .andExpect(jsonPath("$[1].value", is("some-value-2")))
                .andExpect(jsonPath("$[1].comment", is("some-comment-2")))
                .andExpect(jsonPath("$[1].significance", is(0)))
                .andExpect(jsonPath("$[2]", aMapWithSize(5)))
                .andExpect(jsonPath("$[2].id", is(3)))
                .andExpect(jsonPath("$[2].externalId", is("external-id-3")))
                .andExpect(jsonPath("$[2].value", is("some-value-3")))
                .andExpect(jsonPath("$[2].comment", is("some-comment-3")))
                .andExpect(jsonPath("$[2].significance", is(1)));

        List<DataPoint> dataPoints = repository.findAll();

        assertAll(
                () -> assertEquals(3L, dataPoints.size()),
                () -> assertEquals("external-id-1", dataPoints.get(0).getExternalId()),
                () -> assertEquals("some-value-1", dataPoints.get(0).getValue()),
                () -> assertEquals("some-comment-1", dataPoints.get(0).getComment()),
                () -> assertEquals(1, dataPoints.get(0).getSignificance()),
                () -> assertEquals("external-id-2", dataPoints.get(1).getExternalId()),
                () -> assertEquals("some-value-2", dataPoints.get(1).getValue()),
                () -> assertEquals("some-comment-2", dataPoints.get(1).getComment()),
                () -> assertEquals(0, dataPoints.get(1).getSignificance()),
                () -> assertEquals("external-id-3", dataPoints.get(2).getExternalId()),
                () -> assertEquals("some-value-3", dataPoints.get(2).getValue()),
                () -> assertEquals("some-comment-3", dataPoints.get(2).getComment()),
                () -> assertEquals(1, dataPoints.get(2).getSignificance())
        );

        externalService.verify(
                request()
                        .withMethod("GET")
                        .withPath("/data-points"),
                VerificationTimes.exactly(1)
        );
    }


    private MockHttpServletRequestBuilder createDefaultRequest() {
        return post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(TraceIdCustomizerImpl.DEFAULT_HEADER_NAME, REQUEST_ID);
    }

    JSONArray createExternalServiceResponse(Collection<JSONObject> objects) {
        JSONArray array = new JSONArray();
        objects.forEach(array::put);

        return array;
    }

    JSONObject createExternalServiceResponse(Long id) {
        JSONObject element = new JSONObject();

        element.put("externalId", "external-id-" + id);
        element.put("value", "some-value-" + id);
        element.put("comment", "some-comment-" + id);
        element.put("significance", (id % 2));

        return element;
    }
}
