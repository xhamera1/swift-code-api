package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.dto.CountrySwiftCodesResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
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
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SwiftCodeApiServiceGetCodesByCountryTest {

    @Mock
    private SwiftCodeInfoRepository repository;

    @InjectMocks
    private SwiftCodeApiService swiftCodeApiService;

    private SwiftCodeInfo hqAl;
    private SwiftCodeInfo hqPl;
    private SwiftCodeInfo branchPl8Char;
    private SwiftCodeInfo branchPl11Char;
    private SwiftCodeInfo branchAl1;
    private SwiftCodeInfo hqMtWithNullCountryName;
    private SwiftCodeInfo hqLvWithEmptyCountryName;


    @BeforeEach
    void setUp() {

        hqAl = new SwiftCodeInfo("AAISALTRXXX", "UNITED BANK OF ALBANIA SH.A", "HQ Addr AL", "Tirana HQ", "AL", "ALBANIA", true);
        hqPl = new SwiftCodeInfo("AIPOPLP1XXX", "SANTANDER CONSUMER BANK SPOLKA AKCYJNA", "HQ Addr PL", "Wroclaw HQ", "PL", "POLAND", true);


        branchPl8Char = new SwiftCodeInfo("DEUTPLPX", "DEUTSCHE BANK POLSKA S.A.", "Branch Addr PL 8", "Warszawa B8", "PL", "POLAND", false);
        branchPl11Char = new SwiftCodeInfo("TESTPLPWABC", "TEST BANK NON-HQ", "Branch Addr PL 11", "Test Town B11", "PL", "POLAND", false);
        branchAl1 = new SwiftCodeInfo("AAISALTRB01", "UBA Branch 1", "Branch Addr AL 1", "Tirana B1", "AL", "ALBANIA", false);


        hqMtWithNullCountryName = new SwiftCodeInfo("AKBKMTMTXXX", "AKBANK T.A.S. (MALTA BRANCH)", "Addr MT", "St Julians MT", "MT", null, true);
        hqLvWithEmptyCountryName = new SwiftCodeInfo("AIZKLV22XXX", "BANK LV", "Addr LV", "Riga LV", "LV", "   ", true);

    }

    @Nested
    @DisplayName("Happy Path Tests")
    class HappyPathTests {

        @Test
        @DisplayName("Should return codes for country when multiple codes exist")
        void getSwiftCodesByCountry_shouldReturnCodes_whenMultipleExist() {
            String countryCode = "PL";
            SwiftCodeInfo branchPlTownOnly = new SwiftCodeInfo("BRANPLPWTO", "TownOnly Bank", null, "Warsaw Town Only", "PL", "POLAND", false);
            List<SwiftCodeInfo> plEntities = Arrays.asList(hqPl, branchPl8Char, branchPlTownOnly);
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(plEntities);

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCode);

            assertNotNull(result);
            assertEquals(countryCode, result.getCountryISO2());
            assertEquals(hqPl.getCountryName(), result.getCountryName());
            assertNotNull(result.getSwiftCodes());
            assertThat(result.getSwiftCodes()).hasSize(3);

            SwiftCodeResponse dtoHq = result.getSwiftCodes().get(0);
            assertEquals(hqPl.getSwiftCode(), dtoHq.getSwiftCode());
            assertNull(dtoHq.getCountryName());
            assertEquals(hqPl.getAddress(), dtoHq.getAddress());
            assertTrue(dtoHq.isHeadquarter());

            SwiftCodeResponse dtoBranch8 = result.getSwiftCodes().get(1);
            assertEquals(branchPl8Char.getSwiftCode(), dtoBranch8.getSwiftCode());
            assertNull(dtoBranch8.getCountryName());
            assertEquals(branchPl8Char.getAddress(), dtoBranch8.getAddress());
            assertFalse(dtoBranch8.isHeadquarter());

            SwiftCodeResponse dtoBranchTown = result.getSwiftCodes().get(2);
            assertEquals(branchPlTownOnly.getSwiftCode(), dtoBranchTown.getSwiftCode());
            assertNull(dtoBranchTown.getCountryName());
            assertEquals(branchPlTownOnly.getTownName(), dtoBranchTown.getAddress());
            assertFalse(dtoBranchTown.isHeadquarter());

            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }

        @Test
        @DisplayName("Should return codes for country when only one code exists")
        void getSwiftCodesByCountry_shouldReturnCodes_whenOneExists() {

            String countryCode = "MT";

            List<SwiftCodeInfo> mtEntities = Collections.singletonList(hqMtWithNullCountryName);
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(mtEntities);

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCode);

            assertNotNull(result);
            assertEquals(countryCode, result.getCountryISO2());
            assertEquals("", result.getCountryName());
            assertThat(result.getSwiftCodes()).hasSize(1);

            SwiftCodeResponse dto = result.getSwiftCodes().get(0);
            assertEquals(hqMtWithNullCountryName.getSwiftCode(), dto.getSwiftCode());
            assertNull(dto.getCountryName());
            assertTrue(dto.isHeadquarter());

            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }
    }

    @Nested
    @DisplayName("Edge Case and Error Handling Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should return empty list and empty country name when no codes found for country")
        void getSwiftCodesByCountry_shouldReturnEmptyList_whenNoCodesFound() {
            String countryCode = "XX";
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(Collections.emptyList());

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCode);


            assertNotNull(result);
            assertEquals(countryCode, result.getCountryISO2());
            assertEquals("", result.getCountryName());
            assertNotNull(result.getSwiftCodes());
            assertThat(result.getSwiftCodes()).isEmpty();

            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }

        @Test
        @DisplayName("Should handle lowercase country code input correctly")
        void getSwiftCodesByCountry_shouldHandleLowercaseInput() {
            String countryCodeLower = "al";
            String countryCodeUpper = "AL";
            List<SwiftCodeInfo> alEntities = Arrays.asList(hqAl, branchAl1);
            when(repository.findByCountryISO2IgnoreCase(countryCodeUpper)).thenReturn(alEntities);

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCodeLower);

            assertNotNull(result);
            assertEquals(countryCodeUpper, result.getCountryISO2());
            assertEquals(hqAl.getCountryName(), result.getCountryName());
            assertThat(result.getSwiftCodes()).hasSize(2);

            verify(repository).findByCountryISO2IgnoreCase(countryCodeUpper);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when country code is null")
        void getSwiftCodesByCountry_shouldThrowException_whenInputIsNull() {
            String countryCode = null;

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                swiftCodeApiService.getSwiftCodesByCountry(countryCode);
            });

            assertEquals("Country ISO2 code cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should return empty country name when first entity has null country name")
        void getSwiftCodesByCountry_shouldReturnEmptyCountryName_whenFirstEntityHasNullName() {
            String countryCode = "MT";
            List<SwiftCodeInfo> mtEntities = Collections.singletonList(hqMtWithNullCountryName);
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(mtEntities);

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCode);

            assertEquals("", result.getCountryName().trim());
            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }

        @Test
        @DisplayName("Should return empty country name when first entity has empty/whitespace country name")
        void getSwiftCodesByCountry_shouldReturnEmptyCountryName_whenFirstEntityHasEmptyName() {
            String countryCode = "LV";
            List<SwiftCodeInfo> lvEntities = Collections.singletonList(hqLvWithEmptyCountryName);
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(lvEntities);

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCode);

            assertEquals("   ", result.getCountryName());
            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }

        @Test
        @DisplayName("Should map entities to DTO without country name in the list items")
        void getSwiftCodesByCountry_shouldMapDtoWithoutCountryNameInListItems() {
            String countryCode = "PL";
            List<SwiftCodeInfo> plEntities = Arrays.asList(hqPl, branchPl8Char);
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(plEntities);

            CountrySwiftCodesResponse result = swiftCodeApiService.getSwiftCodesByCountry(countryCode);

            assertNotNull(result.getSwiftCodes());
            assertThat(result.getSwiftCodes()).hasSize(2);

            result.getSwiftCodes().forEach(dto -> {
                assertNull(dto.getCountryName(), "CountryName in SwiftCodeResponse list item should be null");
                assertNotNull(dto.getSwiftCode());
                assertNotNull(dto.getBankName());
                assertNotNull(dto.getAddress());
                assertNotNull(dto.getCountryISO2());
            });

            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }

        @Test
        @DisplayName("Should handle list containing null entity from repository gracefully")
        void getSwiftCodesByCountry_shouldHandleNullEntityInList() {
            String countryCode = "XX";
            List<SwiftCodeInfo> entitiesWithNull = Arrays.asList(null, hqAl);
            when(repository.findByCountryISO2IgnoreCase(countryCode)).thenReturn(entitiesWithNull);

            assertThrows(NullPointerException.class, () -> {
                swiftCodeApiService.getSwiftCodesByCountry(countryCode);
            }, "Should throw NullPointerException when processing null entity in the list");

            verify(repository).findByCountryISO2IgnoreCase(countryCode);
        }
    }
}