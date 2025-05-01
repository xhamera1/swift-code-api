package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks; // Using InjectMocks again
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the DataInitializer class.
 * These tests focus on the logic within the run() method, mocking the repository.
 * They rely on the default CSV file path specified in DataInitializer.
 */
@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private SwiftCodeInfoRepository repository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Captor
    ArgumentCaptor<List<SwiftCodeInfo>> swiftCodeInfoListCaptor;

    @Test
    @DisplayName("run() should trigger data loading using default CSV when repository is empty")
    void run_whenRepositoryIsEmpty_shouldLoadDataFromDefaultCsv() throws Exception {

        when(repository.count()).thenReturn(0L);
        dataInitializer.run();
        verify(repository, times(1)).count();

        try {
            verify(repository, times(1)).saveAll(swiftCodeInfoListCaptor.capture());

            List<SwiftCodeInfo> savedList = swiftCodeInfoListCaptor.getValue();

            assertThat(savedList).hasSize(8);
            assertThat(savedList.get(0).getSwiftCode()).isEqualTo("AAISALTRXXX");
            assertThat(savedList.get(7).getSwiftCode()).isEqualTo("EMPTPLPX");

        } catch (org.mockito.exceptions.verification.WantedButNotInvoked e) {
            fail("repository.saveAll() was expected to be called but was not.");
        } catch (Throwable t) {
            throw t;
        }
    }

    @Test
    @DisplayName("run() should NOT trigger data loading when repository is not empty")
    void run_whenRepositoryIsNotEmpty_shouldNotTriggerLoadData() throws Exception {
        when(repository.count()).thenReturn(1L);
        dataInitializer.run();

        verify(repository, times(1)).count();
        verify(repository, never()).saveAll(any());
    }
}