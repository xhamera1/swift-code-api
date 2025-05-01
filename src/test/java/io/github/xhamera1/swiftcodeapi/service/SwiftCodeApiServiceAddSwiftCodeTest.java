package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeRequest;
import io.github.xhamera1.swiftcodeapi.exceptions.InconsistentSwiftDataException;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceAlreadyExistsException;
import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class SwiftCodeApiServiceAddSwiftCodeTest {

    @Mock
    private SwiftCodeInfoRepository repository;

    @InjectMocks
    private SwiftCodeApiService swiftCodeApiService;

    @Captor
    private ArgumentCaptor<SwiftCodeInfo> swiftCodeInfoCaptor;


    @Test
    @DisplayName("Should add valid 11-char HQ code (AAISALTRXXX) successfully")
    void addSwiftCode_shouldAddValidHqFromCsvSuccessfully() {
        SwiftCodeRequest request = createRequest("AAISALTRXXX", "UNITED BANK OF ALBANIA SH.A", "HQ Addr", "AL", "ALBANIA", true);
        String expectedSwiftCodeUpper = "AAISALTRXXX";
        when(repository.existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper)).thenReturn(false);
        when(repository.save(any(SwiftCodeInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = swiftCodeApiService.addSwiftCode(request);


        assertEquals("SWIFT code 'AAISALTRXXX' added successfully.", response.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper);
        verify(repository).save(swiftCodeInfoCaptor.capture());
        SwiftCodeInfo savedEntity = swiftCodeInfoCaptor.getValue();
        assertEquals(expectedSwiftCodeUpper, savedEntity.getSwiftCode());
        assertEquals("AL", savedEntity.getCountryISO2());
        assertEquals("ALBANIA", savedEntity.getCountryName());
        assertTrue(savedEntity.isHeadquarter());
    }

    @Test
    @DisplayName("Should add valid 8-char Branch code (DEUTPLPX) successfully")
    void addSwiftCode_shouldAddValid8CharBranchFromCsvSuccessfully() {
        SwiftCodeRequest request = createRequest("DEUTPLPX", "DEUTSCHE BANK POLSKA S.A.", "Branch Addr", "PL", "POLAND", false);
        String expectedSwiftCodeUpper = "DEUTPLPX";
        when(repository.existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper)).thenReturn(false);
        when(repository.save(any(SwiftCodeInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = swiftCodeApiService.addSwiftCode(request);

        assertEquals("SWIFT code 'DEUTPLPX' added successfully.", response.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper);
        verify(repository).save(swiftCodeInfoCaptor.capture());
        SwiftCodeInfo savedEntity = swiftCodeInfoCaptor.getValue();
        assertEquals(expectedSwiftCodeUpper, savedEntity.getSwiftCode());
        assertEquals("PL", savedEntity.getCountryISO2());
        assertEquals("POLAND", savedEntity.getCountryName());
        assertFalse(savedEntity.isHeadquarter());
    }

    @Test
    @DisplayName("Should add valid 11-char Branch code (TESTPLPWABC) successfully")
    void addSwiftCode_shouldAddValid11CharBranchFromCsvSuccessfully() {
        SwiftCodeRequest request = createRequest("TESTPLPWABC", "TEST BANK NON-HQ", "Test Addr", "PL", "POLAND", false);
        String expectedSwiftCodeUpper = "TESTPLPWABC";
        when(repository.existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper)).thenReturn(false);
        when(repository.save(any(SwiftCodeInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = swiftCodeApiService.addSwiftCode(request);

        assertEquals("SWIFT code 'TESTPLPWABC' added successfully.", response.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper);
        verify(repository).save(swiftCodeInfoCaptor.capture());
        SwiftCodeInfo savedEntity = swiftCodeInfoCaptor.getValue();
        assertEquals(expectedSwiftCodeUpper, savedEntity.getSwiftCode());
        assertEquals("PL", savedEntity.getCountryISO2());
        assertEquals("POLAND", savedEntity.getCountryName());
        assertFalse(savedEntity.isHeadquarter());
    }

    @Test
    @DisplayName("Should handle case insensitivity and trimming for input fields using CSV example")
    void addSwiftCode_shouldHandleCaseAndTrimUsingCsvExample() {
        SwiftCodeRequest request = createRequest("  deutplpx  ", "deutsche bank polska s.a.", "Focus Addr", "pl", "poland", false);
        String expectedSwiftCodeUpperTrimmed = "DEUTPLPX";
        String expectedCountryIsoUpper = "PL";
        String expectedCountryNameUpper = "POLAND";

        when(repository.existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpperTrimmed)).thenReturn(false);
        when(repository.save(any(SwiftCodeInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = swiftCodeApiService.addSwiftCode(request);

        assertEquals("SWIFT code '" + expectedSwiftCodeUpperTrimmed + "' added successfully.", response.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpperTrimmed);
        verify(repository).save(swiftCodeInfoCaptor.capture());
        SwiftCodeInfo savedEntity = swiftCodeInfoCaptor.getValue();
        assertEquals(expectedSwiftCodeUpperTrimmed, savedEntity.getSwiftCode());
        assertEquals(expectedCountryIsoUpper, savedEntity.getCountryISO2());
        assertEquals(expectedCountryNameUpper, savedEntity.getCountryName());
        assertFalse(savedEntity.isHeadquarter());
    }



    @Test
    @DisplayName("Should throw ResourceAlreadyExistsException when SWIFT code (AAISALTRXXX) already exists")
    void addSwiftCode_shouldThrowResourceAlreadyExistsException_whenCodeFromCsvExists() {
        SwiftCodeRequest request = createRequest("AAISALTRXXX", "UNITED BANK OF ALBANIA SH.A", "HQ Addr", "AL", "ALBANIA", true);
        String existingSwiftCodeUpper = "AAISALTRXXX";
        when(repository.existsBySwiftCodeIgnoreCase(existingSwiftCodeUpper)).thenReturn(true);

        ResourceAlreadyExistsException exception = assertThrows(ResourceAlreadyExistsException.class, () -> {
            swiftCodeApiService.addSwiftCode(request);
        });

        assertEquals("SWIFT code '" + existingSwiftCodeUpper + "' already exists.", exception.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(existingSwiftCodeUpper);
        verify(repository, never()).save(any(SwiftCodeInfo.class));
    }

    @Test
    @DisplayName("Should throw InconsistentSwiftDataException when country code mismatches (ADCRBGDS1XX vs country BD)")
    void addSwiftCode_shouldThrowInconsistentDataException_whenCountryCodeMismatchFromCsvExample() {
        SwiftCodeRequest request = createRequest("ADCRBGDS1XX", "Adamant Capital", "Sofia Addr", "BD", "BANGLADESH", false);
        String swiftCodeUpper = "ADCRBGDS1XX";
        String providedCountryIsoUpper = "BD";
        String embeddedCountryCode = "BG";

        when(repository.existsBySwiftCodeIgnoreCase(swiftCodeUpper)).thenReturn(false);

        InconsistentSwiftDataException exception = assertThrows(InconsistentSwiftDataException.class, () -> {
            swiftCodeApiService.addSwiftCode(request);
        });

        String expectedMessage = String.format(
                "Data consistency error: The country code from SWIFT ('%s' in '%s') does not match the provided Country ISO2 ('%s').",
                embeddedCountryCode, swiftCodeUpper, providedCountryIsoUpper
        );
        assertEquals(expectedMessage, exception.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(swiftCodeUpper);
        verify(repository, never()).save(any(SwiftCodeInfo.class));
    }

    @Test
    @DisplayName("Should throw InconsistentSwiftDataException when isHeadquarter is false for HQ code (AAISALTRXXX)")
    void addSwiftCode_shouldThrowInconsistentDataException_whenIsHqFalseAndCodeIsHqCsv() {
        SwiftCodeRequest request = createRequest("AAISALTRXXX", "Bank", "Addr", "AL", "ALBANIA", false);
        String swiftCodeUpper = "AAISALTRXXX";
        when(repository.existsBySwiftCodeIgnoreCase(swiftCodeUpper)).thenReturn(false);

        InconsistentSwiftDataException exception = assertThrows(InconsistentSwiftDataException.class, () -> {
            swiftCodeApiService.addSwiftCode(request);
        });

        assertEquals("Provided 'isHeadquarter' flag (false) is inconsistent with the SWIFT code format (AAISALTRXXX).", exception.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(swiftCodeUpper);
        verify(repository, never()).save(any(SwiftCodeInfo.class));
    }

    @Test
    @DisplayName("Should throw InconsistentSwiftDataException when isHeadquarter is true for Branch code (TESTPLPWABC)")
    void addSwiftCode_shouldThrowInconsistentDataException_whenIsHqTrueAndCodeIsBranch11Csv() {
        SwiftCodeRequest request = createRequest("TESTPLPWABC", "Bank", "Addr", "PL", "POLAND", true);
        String swiftCodeUpper = "TESTPLPWABC";
        when(repository.existsBySwiftCodeIgnoreCase(swiftCodeUpper)).thenReturn(false);

        InconsistentSwiftDataException exception = assertThrows(InconsistentSwiftDataException.class, () -> {
            swiftCodeApiService.addSwiftCode(request);
        });

        assertEquals("Provided 'isHeadquarter' flag (true) is inconsistent with the SWIFT code format (TESTPLPWABC).", exception.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(swiftCodeUpper);
        verify(repository, never()).save(any(SwiftCodeInfo.class));
    }

    @Test
    @DisplayName("Should throw InconsistentSwiftDataException when isHeadquarter is true for 8-char Branch code (DEUTPLPX)")
    void addSwiftCode_shouldThrowInconsistentDataException_whenIsHqTrueAndCodeIsBranch8Csv() {
        SwiftCodeRequest request = createRequest("DEUTPLPX", "Bank", "Addr", "PL", "POLAND", true);
        String swiftCodeUpper = "DEUTPLPX";
        when(repository.existsBySwiftCodeIgnoreCase(swiftCodeUpper)).thenReturn(false);

        InconsistentSwiftDataException exception = assertThrows(InconsistentSwiftDataException.class, () -> {
            swiftCodeApiService.addSwiftCode(request);
        });

        assertEquals("Provided 'isHeadquarter' flag (true) is inconsistent with the SWIFT code format (DEUTPLPX).", exception.getMessage());
        verify(repository).existsBySwiftCodeIgnoreCase(swiftCodeUpper);
        verify(repository, never()).save(any(SwiftCodeInfo.class));
    }

    private SwiftCodeRequest createRequest(String swiftCode, String bankName, String address, String countryISO2, String countryName, boolean isHeadquarter) {
        SwiftCodeRequest request = new SwiftCodeRequest();
        request.setSwiftCode(swiftCode);
        request.setBankName(bankName);
        request.setAddress(address);
        request.setCountryISO2(countryISO2);
        request.setCountryName(countryName);
        request.setIsHeadquarter(isHeadquarter);
        return request;
    }

    @Test
    @DisplayName("Should save entity with null address when request address is null")
    void addSwiftCode_shouldHandleNullAddressInRequest() {
        String swiftCode = "NULLPLPXXXX";
        SwiftCodeRequest requestWithNullAddress = createRequest(swiftCode, "Null Addr Bank", null, "PL", "POLAND", true);
        String expectedSwiftCodeUpper = swiftCode.toUpperCase();

        when(repository.existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper)).thenReturn(false);
        when(repository.save(any(SwiftCodeInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        swiftCodeApiService.addSwiftCode(requestWithNullAddress);

        verify(repository).save(swiftCodeInfoCaptor.capture());
        SwiftCodeInfo savedEntity = swiftCodeInfoCaptor.getValue();
        assertNull(savedEntity.getAddress(), "Address in saved entity should be null when request address is null");
        assertEquals(expectedSwiftCodeUpper, savedEntity.getSwiftCode());
        assertTrue(savedEntity.isHeadquarter());
        assertEquals("PL", savedEntity.getCountryISO2());
    }

    @Test
    @DisplayName("Should save entity with empty address when request address is empty")
    void addSwiftCode_shouldHandleEmptyAddressInRequest() {
        String swiftCode = "EMPTPLPXXXX";
        SwiftCodeRequest requestWithEmptyAddress = createRequest(swiftCode, "Empty Addr Bank", "   ", "PL", "POLAND", true);
        String expectedSwiftCodeUpper = swiftCode.toUpperCase();

        when(repository.existsBySwiftCodeIgnoreCase(expectedSwiftCodeUpper)).thenReturn(false);
        when(repository.save(any(SwiftCodeInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        swiftCodeApiService.addSwiftCode(requestWithEmptyAddress);

        verify(repository).save(swiftCodeInfoCaptor.capture());
        SwiftCodeInfo savedEntity = swiftCodeInfoCaptor.getValue();
        assertEquals("   ", savedEntity.getAddress(), "Address in saved entity should be preserved whitespace when request address is whitespace");
        assertEquals(expectedSwiftCodeUpper, savedEntity.getSwiftCode());
        assertTrue(savedEntity.isHeadquarter());
        assertEquals("PL", savedEntity.getCountryISO2());
    }


}