package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceNotFoundException;
import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwiftCodeApiServiceGetSwiftCodeDetailsTest {

    @Mock
    private SwiftCodeInfoRepository repository;

    @InjectMocks
    private SwiftCodeApiService swiftCodeApiService;

    private SwiftCodeInfo hqAl; // AAISALTRXXX
    private SwiftCodeInfo hqPl; // AIPOPLP1XXX
    private SwiftCodeInfo branchPl8Char; // DEUTPLPX
    private SwiftCodeInfo branchPl11Char; // TESTPLPWABC
    private SwiftCodeInfo branchAl1;
    private SwiftCodeInfo branchAl2;
    private SwiftCodeInfo hqPl8CharAsHq; // DEUTPLPX
    private SwiftCodeInfo branchPlFor8CharHq1;
    private SwiftCodeInfo branchPlFor8CharHq2;
    private SwiftCodeInfo branchPlEmptyAddr; // EMPTPLPX


    @BeforeEach
    void setUp() {
        hqAl = new SwiftCodeInfo("AAISALTRXXX", "UNITED BANK OF ALBANIA SH.A", "HYRJA 3 RR. DRITAN HOXHA ND. 11 TIRANA, TIRANA, 1023", "TIRANA", "AL", "ALBANIA", true);
        hqPl = new SwiftCodeInfo("AIPOPLP1XXX", "SANTANDER CONSUMER BANK SPOLKA AKCYJNA", "STRZEGOMSKA 42C WROCLAW, DOLNOSLASKIE, 53-611", "WROCLAW", "PL", "POLAND", true);

        branchPl8Char = new SwiftCodeInfo("DEUTPLPX", "DEUTSCHE BANK POLSKA S.A.", "FOCUS AL. ARMII LUDOWEJ 26", "WARSZAWA", "PL", "POLAND", false);
        branchPl11Char = new SwiftCodeInfo("TESTPLPWABC", "TEST BANK NON-HQ", "TEST ADDRESS 1", "TEST TOWN", "PL", "POLAND", false);
        branchPlEmptyAddr = new SwiftCodeInfo("EMPTPLPX", "EMPTY ADDRESS BANK", null, null, "PL", "POLAND", false);

        branchAl1 = new SwiftCodeInfo("AAISALTRB01", "UBA Branch 1", "Branch Address 1", "Tirana B1", "AL", "ALBANIA", false);
        branchAl2 = new SwiftCodeInfo("AAISALTRB02", "UBA Branch 2", null, "Tirana B2 Town", "AL", "ALBANIA", false);

        hqPl8CharAsHq = new SwiftCodeInfo("DEUTPLPX", "DEUTSCHE BANK POLSKA S.A.", "FOCUS AL. ARMII LUDOWEJ 26", "WARSZAWA", "PL", "POLAND", true); // Kluczowe: isHeadquarter=true
        branchPlFor8CharHq1 = new SwiftCodeInfo("DEUTPLP1", "DB Branch 1", "DB Branch Addr 1", "Warsaw B1", "PL", "POLAND", false);
        branchPlFor8CharHq2 = new SwiftCodeInfo("DEUTPLP2", "DB Branch 2", "DB Branch Addr 2", "Warsaw B2", "PL", "POLAND", false);

    }

    @Nested
    @DisplayName("Tests for finding Headquarters (HQ)")
    class HeadquarterTests {

        @Test
        @DisplayName("Should return 11-char HQ details with branches when HQ found and branches exist")
        void getSwiftCodeDetails_shouldReturn11CharHqWithBranches_whenHqFoundAndBranchesExist() {
            String hqSwiftCode = hqAl.getSwiftCode();
            String hqPrefix = hqSwiftCode.substring(0, 8);
            when(repository.findBySwiftCodeIgnoreCase(hqSwiftCode)).thenReturn(Optional.of(hqAl));
            when(repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCode))
                    .thenReturn(Arrays.asList(branchAl1, branchAl2));

            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(hqSwiftCode);

            assertNotNull(result);
            assertEquals(hqAl.getSwiftCode(), result.getSwiftCode());
            assertEquals(hqAl.getBankName(), result.getBankName());
            assertEquals(hqAl.getAddress(), result.getAddress());
            assertEquals(hqAl.getCountryISO2(), result.getCountryISO2());
            assertEquals(hqAl.getCountryName(), result.getCountryName());
            assertTrue(result.isHeadquarter());

            assertNotNull(result.getBranches());
            assertThat(result.getBranches()).hasSize(2);

            SwiftCodeResponse branchRes1 = result.getBranches().stream().filter(b -> b.getSwiftCode().equals(branchAl1.getSwiftCode())).findFirst().orElse(null);
            SwiftCodeResponse branchRes2 = result.getBranches().stream().filter(b -> b.getSwiftCode().equals(branchAl2.getSwiftCode())).findFirst().orElse(null);

            assertNotNull(branchRes1);
            assertEquals(branchAl1.getAddress(), branchRes1.getAddress());
            assertNull(branchRes1.getCountryName());
            assertFalse(branchRes1.isHeadquarter());

            assertNotNull(branchRes2);
            assertEquals(branchAl2.getTownName(), branchRes2.getAddress());
            assertNull(branchRes2.getCountryName());
            assertFalse(branchRes2.isHeadquarter());

            verify(repository).findBySwiftCodeIgnoreCase(hqSwiftCode);
            verify(repository).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCode);
        }

        @Test
        @DisplayName("Should return 11-char HQ details with null branches when HQ found but no branches exist")
        void getSwiftCodeDetails_shouldReturn11CharHqWithNullBranches_whenHqFoundAndNoBranchesExist() {
            String hqSwiftCode = hqPl.getSwiftCode();
            String hqPrefix = hqSwiftCode.substring(0, 8);
            when(repository.findBySwiftCodeIgnoreCase(hqSwiftCode)).thenReturn(Optional.of(hqPl));
            when(repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCode))
                    .thenReturn(Collections.emptyList());


            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(hqSwiftCode);


            assertNotNull(result);
            assertEquals(hqPl.getSwiftCode(), result.getSwiftCode());
            assertTrue(result.isHeadquarter());
            assertEquals(hqPl.getAddress(), result.getAddress());
            assertEquals(hqPl.getCountryName(), result.getCountryName());
            assertNull(result.getBranches());

            verify(repository).findBySwiftCodeIgnoreCase(hqSwiftCode);
            verify(repository).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCode);
        }

        @Test
        @DisplayName("Should return 8-char HQ details with branches when 8-char code is HQ and branches exist")
        void getSwiftCodeDetails_shouldReturn8CharHqWithBranches_when8CharIsHqAndBranchesExist() {
            String hqSwiftCode = hqPl8CharAsHq.getSwiftCode();
            String hqPrefix = hqSwiftCode;
            when(repository.findBySwiftCodeIgnoreCase(hqSwiftCode)).thenReturn(Optional.of(hqPl8CharAsHq));
            when(repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCode))
                    .thenReturn(Arrays.asList(branchPlFor8CharHq1, branchPlFor8CharHq2));

            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(hqSwiftCode);

            assertNotNull(result);
            assertEquals(hqPl8CharAsHq.getSwiftCode(), result.getSwiftCode());
            assertEquals(hqPl8CharAsHq.getBankName(), result.getBankName());
            assertEquals(hqPl8CharAsHq.getAddress(), result.getAddress());
            assertEquals(hqPl8CharAsHq.getCountryISO2(), result.getCountryISO2());
            assertEquals(hqPl8CharAsHq.getCountryName(), result.getCountryName());
            assertTrue(result.isHeadquarter());

            assertNotNull(result.getBranches());
            assertThat(result.getBranches()).hasSize(2);

            SwiftCodeResponse branchRes1 = result.getBranches().get(0);
            assertEquals(branchPlFor8CharHq1.getSwiftCode(), branchRes1.getSwiftCode());
            assertNull(branchRes1.getCountryName());
            assertFalse(branchRes1.isHeadquarter());

            SwiftCodeResponse branchRes2 = result.getBranches().get(1);
            assertEquals(branchPlFor8CharHq2.getSwiftCode(), branchRes2.getSwiftCode());
            assertNull(branchRes2.getCountryName());
            assertFalse(branchRes2.isHeadquarter());

            verify(repository).findBySwiftCodeIgnoreCase(hqSwiftCode);
            verify(repository).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCode);
        }

        @Test
        @DisplayName("Should return HQ details ignoring case in input swift code")
        void getSwiftCodeDetails_shouldReturnHqDetails_whenInputIsLowercase() {
            String hqSwiftCodeLower = "aaisaltrxxx";
            String hqSwiftCodeUpper = hqAl.getSwiftCode(); // AAISALTRXXX
            String hqPrefix = hqSwiftCodeUpper.substring(0, 8); // AAISALTR
            when(repository.findBySwiftCodeIgnoreCase(hqSwiftCodeLower)).thenReturn(Optional.of(hqAl));
            when(repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCodeUpper))
                    .thenReturn(Collections.emptyList());

            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(hqSwiftCodeLower);

            assertNotNull(result);
            assertEquals(hqSwiftCodeUpper, result.getSwiftCode());
            assertTrue(result.isHeadquarter());
            assertNull(result.getBranches());

            verify(repository).findBySwiftCodeIgnoreCase(hqSwiftCodeLower);
            verify(repository).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(hqPrefix, hqSwiftCodeUpper);
        }
    }

    @Nested
    @DisplayName("Tests for finding Branches (Non-HQ)")
    class BranchTests {

        @Test
        @DisplayName("Should return 11-char Branch (non-XXX) details when found")
        void getSwiftCodeDetails_shouldReturn11CharNonXxxBranchDetails_whenFound() {
            String branchSwiftCode = branchPl11Char.getSwiftCode(); // TESTPLPWABC
            when(repository.findBySwiftCodeIgnoreCase(branchSwiftCode)).thenReturn(Optional.of(branchPl11Char));

            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(branchSwiftCode);

            assertNotNull(result);
            assertEquals(branchPl11Char.getSwiftCode(), result.getSwiftCode());
            assertEquals(branchPl11Char.getBankName(), result.getBankName());
            assertEquals(branchPl11Char.getAddress(), result.getAddress());
            assertEquals(branchPl11Char.getCountryISO2(), result.getCountryISO2());
            assertEquals(branchPl11Char.getCountryName(), result.getCountryName());
            assertFalse(result.isHeadquarter());
            assertNull(result.getBranches());

            verify(repository).findBySwiftCodeIgnoreCase(branchSwiftCode);
            verify(repository, never()).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 8-char Branch details when found (and treated as non-HQ)")
        void getSwiftCodeDetails_shouldReturn8CharBranchDetails_whenFoundAndIsBranch() {
            String branchSwiftCode = branchPl8Char.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(branchSwiftCode)).thenReturn(Optional.of(branchPl8Char));

            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(branchSwiftCode);

            assertNotNull(result);
            assertEquals(branchPl8Char.getSwiftCode(), result.getSwiftCode());
            assertFalse(result.isHeadquarter());
            assertEquals(branchPl8Char.getCountryName(), result.getCountryName());
            assertNull(result.getBranches());

            verify(repository).findBySwiftCodeIgnoreCase(branchSwiftCode);
            verify(repository, never()).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(anyString(), anyString());
        }


        @Test
        @DisplayName("Should return Branch details ignoring case in input swift code")
        void getSwiftCodeDetails_shouldReturnBranchDetails_whenInputIsMixedCase() {
            String branchSwiftCodeMixed = "dEuTpLpX";
            String branchSwiftCodeUpper = branchPl8Char.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(branchSwiftCodeMixed)).thenReturn(Optional.of(branchPl8Char));

            SwiftCodeResponse result = swiftCodeApiService.getSwiftCodeDetails(branchSwiftCodeMixed);

            assertNotNull(result);
            assertEquals(branchSwiftCodeUpper, result.getSwiftCode());
            assertFalse(result.isHeadquarter());
            assertNull(result.getBranches());

            verify(repository).findBySwiftCodeIgnoreCase(branchSwiftCodeMixed);
            verify(repository, never()).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Tests for Address Mapping Logic")
    class AddressMappingTests {

        @Test
        @DisplayName("Should use 'address' field when it is not null or empty")
        void mapEntityToDto_shouldUseAddressField_whenValid() {
            String swiftCode = hqAl.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(swiftCode)).thenReturn(Optional.of(hqAl));
            SwiftCodeResponse response = swiftCodeApiService.getSwiftCodeDetails(swiftCode);
            assertEquals(hqAl.getAddress(), response.getAddress());
        }

        @Test
        @DisplayName("Should use 'townName' field when 'address' is null")
        void mapEntityToDto_shouldUseTownNameField_whenAddressIsNull() {
            String swiftCode = branchAl2.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(swiftCode)).thenReturn(Optional.of(branchAl2));
            SwiftCodeResponse response = swiftCodeApiService.getSwiftCodeDetails(swiftCode);
            assertEquals(branchAl2.getTownName(), response.getAddress());
        }

        @Test
        @DisplayName("Should use 'townName' field when 'address' is empty or whitespace")
        void mapEntityToDto_shouldUseTownNameField_whenAddressIsEmpty() {
            SwiftCodeInfo entityWithEmptyAddress = new SwiftCodeInfo("TESTCODE003", "Bank", "  ", "Town For Empty", "XX", "TESTLAND", false);
            String swiftCode = entityWithEmptyAddress.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(swiftCode)).thenReturn(Optional.of(entityWithEmptyAddress));

            SwiftCodeResponse response = swiftCodeApiService.getSwiftCodeDetails(swiftCode);
            assertEquals(entityWithEmptyAddress.getTownName(), response.getAddress());
        }

        @Test
        @DisplayName("Should return empty string when both 'address' and 'townName' are null")
        void mapEntityToDto_shouldReturnEmptyString_whenBothAddressAndTownNameAreNull() {
            String swiftCode = branchPlEmptyAddr.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(swiftCode)).thenReturn(Optional.of(branchPlEmptyAddr));
            SwiftCodeResponse response = swiftCodeApiService.getSwiftCodeDetails(swiftCode);
            assertEquals("", response.getAddress());
        }

        @Test
        @DisplayName("Should return empty string when both 'address' and 'townName' are empty/whitespace")
        void mapEntityToDto_shouldReturnEmptyString_whenBothAddressAndTownNameAreEmpty() {
            SwiftCodeInfo entityWithEmptyStrings = new SwiftCodeInfo("TESTCODE005", "Bank", " ", "", "XX", "TESTLAND", false);
            String swiftCode = entityWithEmptyStrings.getSwiftCode();
            when(repository.findBySwiftCodeIgnoreCase(swiftCode)).thenReturn(Optional.of(entityWithEmptyStrings));
            SwiftCodeResponse response = swiftCodeApiService.getSwiftCodeDetails(swiftCode);

            assertEquals("", response.getAddress());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when SWIFT code does not exist")
        void getSwiftCodeDetails_shouldThrowResourceNotFoundException_whenCodeNotFound() {
            String nonExistentSwiftCode = "XXXXXXXXXXX";
            when(repository.findBySwiftCodeIgnoreCase(nonExistentSwiftCode)).thenReturn(Optional.empty());


            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                swiftCodeApiService.getSwiftCodeDetails(nonExistentSwiftCode);
            });

            assertEquals("SWIFT code '" + nonExistentSwiftCode + "' not found.", exception.getMessage());

            verify(repository).findBySwiftCodeIgnoreCase(nonExistentSwiftCode);
            verify(repository, never()).findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(anyString(), anyString());
        }
    }
}