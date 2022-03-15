package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.common.exception.persistence.EntityNotFoundException;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.external.ExternalService;
import ee.bitweb.testingsample.domain.datapoint.external.ExternalServiceApi;
import ee.bitweb.testingsample.domain.datapoint.features.create.CreateDataPointFeature;
import ee.bitweb.testingsample.domain.datapoint.features.create.CreateDataPointModel;
import ee.bitweb.testingsample.domain.datapoint.features.update.UpdateDataPointFeature;
import ee.bitweb.testingsample.domain.datapoint.features.update.UpdateDataPointModel;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class ImportDataPointsFeatureUnitTests {

    @Mock
    private UpdateDataPointFeature updateDataPointFeature;

    @Mock
    private CreateDataPointFeature createDataPointFeature;

    @Mock
    private GetDataPointByExternalIdFeature getDataPointByExternalIdFeature;

    @Mock
    private ExternalService externalService;

    @InjectMocks
    private ImportDataPointsFeature importDataPointsFeature;

    @Captor
    private ArgumentCaptor<DataPoint> dataPointArgumentCaptor;

    @Captor
    private ArgumentCaptor<UpdateDataPointModel> updateDataPointModelArgumentCaptor;

    @Captor
    private ArgumentCaptor<CreateDataPointModel> createDataPointModelArgumentCaptor;

    @Test
    void onExecuteShouldReturnListOfDataPoints() throws Exception {
        ExternalServiceApi.DataPointResponse response = DefaultExternalServiceResponse();
        DataPoint dataPoint = DataPointHelper.create(1L);

        doReturn(List.of(response)).when(externalService).getAll();
        doReturn(dataPoint).when(getDataPointByExternalIdFeature).get(any());

        importDataPointsFeature.execute();
        verify(externalService, times(1)).getAll();
        verify(updateDataPointFeature, times(1))
                .update(
                        dataPointArgumentCaptor.capture(),
                        updateDataPointModelArgumentCaptor.capture()
                );

        // Captured dataPoint
        assertAll(
                () -> assertEquals("external-id-1", dataPointArgumentCaptor.getValue().getExternalId()),
                () -> assertEquals("some-value-1", dataPointArgumentCaptor.getValue().getValue()),
                () -> assertEquals("some-comment-1", dataPointArgumentCaptor.getValue().getComment()),
                () -> assertEquals(1, dataPointArgumentCaptor.getValue().getSignificance())
        );

        // Captured updateDataPointModel
        assertAll(
                ()-> assertNull(dataPointArgumentCaptor.getValue().getId()),
                ()-> assertEquals("external-id-1", dataPointArgumentCaptor.getValue().getExternalId()),
                ()-> assertEquals("some-value-1", dataPointArgumentCaptor.getValue().getValue()),
                ()-> assertEquals("some-comment-1", dataPointArgumentCaptor.getValue().getComment()),
                ()-> assertEquals(1, dataPointArgumentCaptor.getValue().getSignificance())
        );
    }

    @Test
    void onInvalidIdThrowsEntityNotFoundException() throws Exception {
        ExternalServiceApi.DataPointResponse response = DefaultExternalServiceResponse();
        DataPoint dataPoint = DataPointHelper.create(1L);

        doReturn(List.of(response)).when(externalService).getAll();
        doReturn(dataPoint).when(createDataPointFeature).create(any());
        doThrow(EntityNotFoundException.class)
                .when(getDataPointByExternalIdFeature)
                .get(anyString());

        importDataPointsFeature.execute();

        verify(createDataPointFeature, times(1))
                .create(createDataPointModelArgumentCaptor.capture());

        assertAll(
                () -> assertEquals("external-id-3", createDataPointModelArgumentCaptor.getValue().getExternalId()),
                () -> assertEquals("some-value-3", createDataPointModelArgumentCaptor.getValue().getValue()),
                () -> assertEquals("some-comment-3", createDataPointModelArgumentCaptor.getValue().getComment()),
                () -> assertEquals(1, createDataPointModelArgumentCaptor.getValue().getSignificance())
        );
    }

    private ExternalServiceApi.DataPointResponse DefaultExternalServiceResponse() {
        ExternalServiceApi.DataPointResponse response = new ExternalServiceApi.DataPointResponse();
        response.setExternalId("external-id-3");
        response.setComment("some-comment-3");
        response.setValue("some-value-3");
        response.setSignificance(1);

        return response;
    }
}
