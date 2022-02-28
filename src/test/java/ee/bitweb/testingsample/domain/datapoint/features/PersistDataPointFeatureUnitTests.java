package ee.bitweb.testingsample.domain.datapoint.features;

import ee.bitweb.testingsample.common.exception.persistence.ConflictException;
import ee.bitweb.testingsample.domain.datapoint.DataPointHelper;
import ee.bitweb.testingsample.domain.datapoint.common.DataPoint;
import ee.bitweb.testingsample.domain.datapoint.common.DataPointRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
public class PersistDataPointFeatureUnitTests {

    @Mock
    private DataPointRepository repository;

    @InjectMocks
    PersistDataPointFeature persistDataPointFeature;


    @Test
    void onValidDataPointShouldSaveAndReturnEntity() throws Exception {
        DataPoint point = DataPointHelper.create(1L);
        point.setId(1L);

        doReturn(point).when(repository).save(point);
        persistDataPointFeature.save(point);

        assertAll(
                () -> assertEquals(1L, point.getId()),
                () -> assertEquals("some-value-1", point.getValue()),
                () -> assertEquals("external-id-1", point.getExternalId()),
                () -> assertEquals("some-comment-1", point.getComment()),
                () -> assertEquals(1, point.getSignificance())
        );
    }

    @Test
    void onDuplicateDatapointThrowsDataIntegrityViolationException() throws Exception {
        DataPoint point = DataPointHelper.create(1L);
        persistDataPointFeature.save(point);

        doThrow(DataIntegrityViolationException.class).when(repository).save(any());

        assertThrows(ConflictException.class, () -> {
            persistDataPointFeature.save(point);
        });
    }
}
