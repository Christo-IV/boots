package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.common.exception.persistence.EntityNotFoundException;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
@RequiredArgsConstructor
public class GetDataPointByIdFeatureUnitTests {

    @InjectMocks
    GetDataPointByIdFeature getDataPointByIdFeature;

    @Mock
    private DataPointRepository repository;

    @Test
    void onValidIdReturnDataPoint() throws Exception {
        DataPoint point = DataPointHelper.create(1L);

        doReturn(Optional.of(point)).when(repository).findOne(DataPointSpecification.id(any()));
        DataPoint dataPoint = getDataPointByIdFeature.get(1L);

        assertAll(
                () -> assertEquals("external-id-1", dataPoint.getExternalId()),
                () -> assertEquals("some-value-1", dataPoint.getValue()),
                () -> assertEquals("some-comment-1", dataPoint.getComment()),
                () -> assertEquals(1, dataPoint.getSignificance())
        );
    }

    @Test
    void onInvalidIdThrowsEntityNotFoundException() throws Exception {
        DataPoint point = DataPointHelper.create(1L);
        assertThrows(EntityNotFoundException.class, () -> {
            getDataPointByIdFeature.get(1L);
        });
    }
}
