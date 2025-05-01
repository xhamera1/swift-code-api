package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the DataInitializer class.
 * Uses the full Spring Boot context and a test database (e.g., H2).
 * The test CSV file `src/test/resources/data/swift_code_data.csv` will be used
 * instead of the production one, thanks to Spring's resource loading mechanism.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DataInitializerIntegrationTest {

    @Autowired
    private SwiftCodeInfoRepository repository;

    private static final Logger log = LoggerFactory.getLogger(DataInitializerIntegrationTest.class);

    @Test
    @DisplayName("Should load exactly 8 valid records (including edge cases) from the test CSV file on application startup")
    @Transactional
    void shouldLoadOnlyValidRecordsIncludingEdgeCasesFromTestCsvOnStartup() {
        List<SwiftCodeInfo> loadedData = repository.findAll();
        log.info("Found {} records in the repository after initialization.", loadedData.size());

        assertThat(loadedData).hasSize(8);

        Optional<SwiftCodeInfo> recordAL = loadedData.stream().filter(info -> "AAISALTRXXX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordAL.isPresent(), "Record not found for AAISALTRXXX");
        assertEquals("ALBANIA", recordAL.get().getCountryName());

        Optional<SwiftCodeInfo> recordBG = loadedData.stream().filter(info -> "ABIEBGS1XXX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordBG.isPresent(), "Record not found for ABIEBGS1XXX");
        assertEquals("BULGARIA", recordBG.get().getCountryName());

        Optional<SwiftCodeInfo> recordMT = loadedData.stream().filter(info -> "AKBKMTMTXXX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordMT.isPresent(), "Record not found for AKBKMTMTXXX");
        assertEquals("MALTA", recordMT.get().getCountryName());

        Optional<SwiftCodeInfo> recordMC = loadedData.stream().filter(info -> "AGRIMCM1XXX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordMC.isPresent(), "Record not found for AGRIMCM1XXX");
        assertEquals("MONACO", recordMC.get().getCountryName());


        Optional<SwiftCodeInfo> recordDEUTPLPX = loadedData.stream().filter(info -> "DEUTPLPX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordDEUTPLPX.isPresent(), "Record not found for DEUTPLPX (Edge Case 1)");
        recordDEUTPLPX.ifPresent(info -> assertAll("Verification of record DEUTPLPX",
                () -> assertEquals(8, info.getSwiftCode().length()),
                () -> assertEquals("DEUTSCHE BANK POLSKA S.A.", info.getBankName()),
                () -> assertEquals("FOCUS AL. ARMII LUDOWEJ 26", info.getAddress()),
                () -> assertEquals("WARSZAWA", info.getTownName()),
                () -> assertEquals("PL", info.getCountryISO2()),
                () -> assertEquals("POLAND", info.getCountryName()),
                () -> assertFalse(info.isHeadquarter(), "Should not be marked as headquarter (no XXX)")
        ));

        Optional<SwiftCodeInfo> recordTESTPLPWABC = loadedData.stream().filter(info -> "TESTPLPWABC".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordTESTPLPWABC.isPresent(), "Record not found for TESTPLPWABC (Edge Case 2)");
        recordTESTPLPWABC.ifPresent(info -> assertAll("Verification of record TESTPLPWABC",
                () -> assertEquals(11, info.getSwiftCode().length()),
                () -> assertEquals("TEST BANK NON-HQ", info.getBankName()),
                () -> assertEquals("PL", info.getCountryISO2()),
                () -> assertEquals("POLAND", info.getCountryName()),
                () -> assertFalse(info.isHeadquarter(), "Should not be marked as headquarter (no XXX)")
        ));

        Optional<SwiftCodeInfo> recordCASEPLPX = loadedData.stream().filter(info -> "CASEPLPX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordCASEPLPX.isPresent(), "Record not found for CASEPLPX (Edge Case 3)");
        recordCASEPLPX.ifPresent(info -> assertAll("Verification of record CASEPLPX",
                () -> assertEquals(8, info.getSwiftCode().length()),
                () -> assertEquals("CASE TEST BANK", info.getBankName()),
                () -> assertEquals("PL", info.getCountryISO2()),
                () -> assertEquals("POLAND", info.getCountryName()),
                () -> assertFalse(info.isHeadquarter())
        ));

        Optional<SwiftCodeInfo> recordEMPTPLPX = loadedData.stream().filter(info -> "EMPTPLPX".equals(info.getSwiftCode())).findFirst();
        assertTrue(recordEMPTPLPX.isPresent(), "Record not found for EMPTPLPX (Edge Case 4)");
        recordEMPTPLPX.ifPresent(info -> assertAll("Verification of record EMPTPLPX",
                () -> assertEquals(8, info.getSwiftCode().length()),
                () -> assertEquals("EMPTY ADDRESS BANK", info.getBankName()),
                () -> assertNull(info.getAddress(), "Address should be null"),
                () -> assertNull(info.getTownName(), "Town should be null"),
                () -> assertEquals("PL", info.getCountryISO2()),
                () -> assertEquals("POLAND", info.getCountryName()),
                () -> assertFalse(info.isHeadquarter())
        ));
    }

    @Test
    @DisplayName("Verifies DB state after DataInitializer run (with edge cases) and manual record insertion")
    void verifyDbStateAfterInitializerWithEdgeCasesAndManualInsert() {
        long countAfterInitializer = repository.count();
        log.info("Number of records after DataInitializer run (with edge cases): {}", countAfterInitializer);
        assertThat(countAfterInitializer).isEqualTo(8);

        SwiftCodeInfo existingData = new SwiftCodeInfo();
        existingData.setSwiftCode("TESTPLPWXXX");
        existingData.setBankName("Test Bank");
        existingData.setCountryISO2("PL");
        existingData.setCountryName("POLAND");
        existingData.setHeadquarter(true);
        repository.save(existingData);

        long finalCount = repository.count();
        log.info("Number of records after manual insertion: {}", finalCount);
        assertThat(finalCount).isEqualTo(9);

        Optional<SwiftCodeInfo> manualRecord = repository.findById("TESTPLPWXXX");
        assertTrue(manualRecord.isPresent(), "Manual test record TESTPLPWXXX not found");
        assertEquals("Test Bank", manualRecord.get().getBankName());
    }
}