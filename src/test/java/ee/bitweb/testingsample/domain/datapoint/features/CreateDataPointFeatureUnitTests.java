package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class CreateDataPointFeatureUnitTests {

    @Mock
    private UpdateDataPointFeature updateDataPointFeature;

    @Mock
    private PersistDataPointFeature persistDataPointFeature;

    @Mock
    private DataPointRepository repository;

    @InjectMocks
    private CreateDataPointFeature createDataPointFeature;

    @Captor
    private ArgumentCaptor<DataPoint> dataPointArgumentCaptor;

    @Captor
    private ArgumentCaptor<UpdateDataPointModel> dataPointModelArgumentCaptor;

    @Test
    void onValidModelCreateDatapointFromModelAndReturnSuccess() throws Exception {
        CreateDataPointModel dataPointModel = new CreateDataPointModel(
        "external-id-2",
        "some-value-2",
        "some-comment-2",
        0
        );

        DataPoint point = DataPointHelper.create(1L);
        doReturn(point).when(updateDataPointFeature).update(any(), any());
        DataPoint dataPoint = createDataPointFeature.create(dataPointModel);

        assertAll(
                () -> assertEquals("external-id-1",dataPoint.getExternalId()),
                () -> assertEquals("some-value-1",dataPoint.getValue()),
                () -> assertEquals("some-comment-1",dataPoint.getComment()),
                () -> assertEquals(1,dataPoint.getSignificance())
        );
    }

    @Test
    void onValidDataModelShouldSaveAndReturn() throws Exception {
        CreateDataPointModel dataPointModel = new CreateDataPointModel(
                "external-id-2",
                "some-value-2",
                "some-comment-2",
                0
        );

        createDataPointFeature.create(dataPointModel);
        verify(updateDataPointFeature, times(1)).update(dataPointArgumentCaptor.capture(), dataPointModelArgumentCaptor.capture());

        // Assert blank dataPoint
        assertAll(
                () -> assertNull(dataPointArgumentCaptor.getValue().getId()),
                () -> assertNull(dataPointArgumentCaptor.getValue().getValue()),
                () -> assertNull(dataPointArgumentCaptor.getValue().getExternalId()),
                () -> assertNull(dataPointArgumentCaptor.getValue().getComment()),
                () -> assertEquals(1, dataPointArgumentCaptor.getValue().getSignificance())
        );

        // Assert dataPoint model
        assertAll(
                () -> assertEquals("external-id-2", dataPointModelArgumentCaptor.getValue().getExternalId()),
                () -> assertEquals("some-value-2", dataPointModelArgumentCaptor.getValue().getValue()),
                () -> assertEquals("some-comment-2", dataPointModelArgumentCaptor.getValue().getComment()),
                () -> assertEquals(1, dataPointArgumentCaptor.getValue().getSignificance())
        );
    }
}
