package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import ee.bitweb.testingsample.domain.datapoint.features.create.CreateDataPointFeature;
import ee.bitweb.testingsample.domain.datapoint.features.update.UpdateDataPointFeature;
import ee.bitweb.testingsample.domain.datapoint.features.update.UpdateDataPointModel;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class UpdateDataPointFeatureUnitTests {

    @Mock
    private PersistDataPointFeature persistDataPointFeature;

    @InjectMocks
    private UpdateDataPointFeature updateDataPointFeature;

    @Captor
    private ArgumentCaptor<DataPoint> dataPointArgumentCaptor;

    @Test
    void onUpdateShouldSaveDataPoint() throws Exception {
        UpdateDataPointModel dataPointModel = new UpdateDataPointModel(
                "external-id-1",
                "some-value-1",
                "some-comment-1",
                1
        );

        DataPoint dataPoint = DataPointHelper.create(1L);
        doReturn(dataPoint).when(persistDataPointFeature).save(dataPoint);
        updateDataPointFeature.update(dataPoint, dataPointModel);

        Assertions.assertAll(
                () -> assertEquals("external-id-1",dataPoint.getExternalId()),
                () -> assertEquals("some-value-1",dataPoint.getValue()),
                () -> assertEquals("some-comment-1",dataPoint.getComment()),
                () -> assertEquals(1,dataPoint.getSignificance())
        );
    }

    @Test
    void onUpdateShouldSaveAndVerifyDataPoint() throws Exception {
        UpdateDataPointModel dataPointModel = new UpdateDataPointModel(
                "external-id-1",
                "some-value-1",
                "some-comment-1",
                1
        );

        DataPoint dataPoint = DataPointHelper.create(1L);
        updateDataPointFeature.update(dataPoint, dataPointModel);
        verify(persistDataPointFeature, times(1)).save(dataPointArgumentCaptor.capture());

        Assertions.assertAll(
                () -> assertEquals("external-id-1",dataPointArgumentCaptor.getValue().getExternalId()),
                () -> assertEquals("some-value-1",dataPointArgumentCaptor.getValue().getValue()),
                () -> assertEquals("some-comment-1",dataPointArgumentCaptor.getValue().getComment()),
                () -> assertEquals(1,dataPointArgumentCaptor.getValue().getSignificance())
        );
    }
}
