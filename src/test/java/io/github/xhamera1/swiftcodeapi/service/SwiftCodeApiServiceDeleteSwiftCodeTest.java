package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceNotFoundException;
import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeApiServiceDeleteSwiftCodeTest {

    @Mock
    private SwiftCodeInfoRepository repository;

    @InjectMocks
    private SwiftCodeApiService swiftCodeApiService;

    @Captor
    private ArgumentCaptor<SwiftCodeInfo> swiftCodeInfoCaptor;

    private SwiftCodeInfo existingHqAl;
    private SwiftCodeInfo existingBranchPl8;

    @BeforeEach
    void setUp() {
        existingHqAl = new SwiftCodeInfo("AAISALTRXXX", "UNITED BANK OF ALBANIA SH.A", "HQ Addr AL", "Tirana HQ", "AL", "ALBANIA", true);
        existingBranchPl8 = new SwiftCodeInfo("DEUTPLPX", "DEUTSCHE BANK POLSKA S.A.", "Branch Addr PL 8", "Warszawa B8", "PL", "POLAND", false);
    }

    @Test
    @DisplayName("Should delete existing SWIFT code successfully")
    void deleteSwiftCode_shouldDeleteExistingCodeSuccessfully() {
        String swiftCodeToDelete = existingHqAl.getSwiftCode();
        String expectedProcessedCode = swiftCodeToDelete.toUpperCase();

        when(repository.findBySwiftCodeIgnoreCase(expectedProcessedCode)).thenReturn(Optional.of(existingHqAl));
        doNothing().when(repository).delete(any(SwiftCodeInfo.class));

        MessageResponse response = swiftCodeApiService.deleteSwiftCode(swiftCodeToDelete);

        assertNotNull(response);
        assertEquals("SWIFT code '" + expectedProcessedCode + "' deleted successfully.", response.getMessage());

        verify(repository).findBySwiftCodeIgnoreCase(expectedProcessedCode);
        verify(repository).delete(swiftCodeInfoCaptor.capture());

        assertSame(existingHqAl, swiftCodeInfoCaptor.getValue(), "Should delete the exact entity object found");
    }

    @Test
    @DisplayName("Should handle case insensitivity when finding code to delete")
    void deleteSwiftCode_shouldHandleCaseInsensitivity() {
        String swiftCodeLower = "deutplpx";
        String expectedProcessedCode = swiftCodeLower.toUpperCase();

        when(repository.findBySwiftCodeIgnoreCase(expectedProcessedCode)).thenReturn(Optional.of(existingBranchPl8));
        doNothing().when(repository).delete(existingBranchPl8);

        MessageResponse response = swiftCodeApiService.deleteSwiftCode(swiftCodeLower);

        assertNotNull(response);
        assertEquals("SWIFT code '" + expectedProcessedCode + "' deleted successfully.", response.getMessage());

        verify(repository).findBySwiftCodeIgnoreCase(expectedProcessedCode);
        verify(repository).delete(existingBranchPl8);
    }

    @Test
    @DisplayName("Should handle trimming of input SWIFT code")
    void deleteSwiftCode_shouldHandleTrimming() {
        String swiftCodeWithSpaces = "  AAISALTRXXX  ";
        String expectedProcessedCode = swiftCodeWithSpaces.trim().toUpperCase();

        when(repository.findBySwiftCodeIgnoreCase(expectedProcessedCode)).thenReturn(Optional.of(existingHqAl));
        doNothing().when(repository).delete(existingHqAl);

        MessageResponse response = swiftCodeApiService.deleteSwiftCode(swiftCodeWithSpaces);

        assertNotNull(response);
        assertEquals("SWIFT code '" + expectedProcessedCode + "' deleted successfully.", response.getMessage());

        verify(repository).findBySwiftCodeIgnoreCase(expectedProcessedCode);
        verify(repository).delete(existingHqAl);
    }


    @Test
    @DisplayName("Should throw ResourceNotFoundException when SWIFT code does not exist")
    void deleteSwiftCode_shouldThrowResourceNotFoundException_whenCodeNotFound() {
        String nonExistentSwiftCode = "NONEXISTPLX";
        String expectedProcessedCode = nonExistentSwiftCode.toUpperCase();

        when(repository.findBySwiftCodeIgnoreCase(expectedProcessedCode)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            swiftCodeApiService.deleteSwiftCode(nonExistentSwiftCode);
        });

        assertEquals("SWIFT code '" + expectedProcessedCode + "' not found, cannot delete.", exception.getMessage());

        verify(repository).findBySwiftCodeIgnoreCase(expectedProcessedCode);
        verify(repository, never()).delete(any(SwiftCodeInfo.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException even if input format is technically invalid (e.g., 9 chars) but not found")
    void deleteSwiftCode_shouldThrowNotFound_forInvalidFormatNotFound() {
        String invalidFormatSwiftCode = "AFAAUYM1X";
        String expectedProcessedCode = invalidFormatSwiftCode.toUpperCase();

        when(repository.findBySwiftCodeIgnoreCase(expectedProcessedCode)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            swiftCodeApiService.deleteSwiftCode(invalidFormatSwiftCode);
        });

        assertEquals("SWIFT code '" + expectedProcessedCode + "' not found, cannot delete.", exception.getMessage());

        verify(repository).findBySwiftCodeIgnoreCase(expectedProcessedCode);
        verify(repository, never()).delete(any(SwiftCodeInfo.class));
    }

    @Test
    @DisplayName("Should delete successfully even if input format is technically invalid but code *is* found")
    void deleteSwiftCode_shouldDeleteSuccessfully_forInvalidFormatIfFound() {
        String invalidFormatSwiftCode = "SHORTPX";
        String expectedProcessedCode = invalidFormatSwiftCode.toUpperCase();
        SwiftCodeInfo foundInvalidEntity = new SwiftCodeInfo(expectedProcessedCode, "Invalid Bank", null, null, "PL", "POLAND", false);

        when(repository.findBySwiftCodeIgnoreCase(expectedProcessedCode)).thenReturn(Optional.of(foundInvalidEntity));
        doNothing().when(repository).delete(foundInvalidEntity);

        MessageResponse response = swiftCodeApiService.deleteSwiftCode(invalidFormatSwiftCode);

        assertNotNull(response);
        assertEquals("SWIFT code '" + expectedProcessedCode + "' deleted successfully.", response.getMessage());

        verify(repository).findBySwiftCodeIgnoreCase(expectedProcessedCode);
        verify(repository).delete(foundInvalidEntity);
    }
}