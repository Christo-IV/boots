package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class FindAllDataPointsFeatureUnitTests {

    @InjectMocks
    private FindAllDataPointsFeature findAllDataPointsFeature;

    @Mock
    private DataPointRepository repository;

    @Captor
    private ArgumentCaptor<DataPoint> dataPointArgumentCaptor;

    @Test
    void onFindingAtLeastTwoDataPointsReturnSuccess() throws Exception {
        DataPoint firstPoint = DataPointHelper.create(1L);
        DataPoint secondPoint = DataPointHelper.create(2L);


        List<DataPoint> dataPoints = findAllDataPointsFeature.find();
        verify(repository, times(1)).findAll();

        //doReturn(List.of(firstPoint, secondPoint)).when(repository).findAll();

        assertAll(
                () -> assertEquals(2L, dataPoints.size()),
                () -> assertEquals("external-id-1", dataPoints.get(0).getExternalId()),
                () -> assertEquals("some-value-1", dataPoints.get(0).getValue()),
                () -> assertEquals("some-comment-1", dataPoints.get(0).getComment()),
                () -> assertEquals(1, dataPoints.get(0).getSignificance()),
                () -> assertEquals("external-id-2", dataPoints.get(1).getExternalId()),
                () -> assertEquals("some-value-2", dataPoints.get(1).getValue()),
                () -> assertEquals("some-comment-2", dataPoints.get(1).getComment()),
                () -> assertEquals(0, dataPoints.get(1).getSignificance())
        );
    }
}

