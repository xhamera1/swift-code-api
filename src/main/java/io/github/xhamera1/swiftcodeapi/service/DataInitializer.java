package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


/**
 * Initializes the application's database with SWIFT code data from a CSV file upon startup.
 * Implements {@link CommandLineRunner} to execute after the application context is loaded.
 * <p>
 * Data initialization only occurs if the {@code swift_codes} table in the database is empty,
 * preventing data duplication on subsequent application restarts.
 * Data is loaded from a CSV file specified by {@link #csvFilePath} located in the classpath resources.
 * Uses Apache Commons CSV for parsing and saves data in batches for performance.
 * </p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SwiftCodeInfoRepository repository;

    private final String csvFilePath = "data/swift_code_data.csv";

    /**
     * Constructs the DataInitializer with required dependencies.
     *
     * @param swiftCodeInfoRepository The repository used for saving SWIFT code data.
     */
    @Autowired
    public DataInitializer(SwiftCodeInfoRepository swiftCodeInfoRepository) {
        this.repository = swiftCodeInfoRepository;
    }


    /**
     * Executes the data initialization logic when the application starts.
     * Checks if the database is empty and triggers the CSV loading process if needed.
     * This method runs within a database transaction.
     *
     * @param args Incoming command line arguments (not used).
     * @throws Exception if an error occurs during file access or database interaction.
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (repository.count() == 0) {
            log.info("Database is empty. Initializing data from CSV: {}", csvFilePath);
            loadDataFromCsv();
        }
        else {
            log.info("Database already contains data. Skipping initialization.");
        }
    }


    /**
     * Loads SWIFT code data from the configured CSV file and persists it to the database.
     * <p>
     * Reads the CSV file record by record, validates critical fields, maps valid records
     * to {@link SwiftCodeInfo} entities, and saves them to the database in batches.
     * Logs progress, skipped records due to missing data or errors, and a final summary.
     * Handles potential I/O errors during file reading and parsing errors.
     * </p>
     */
    private void loadDataFromCsv() {
        final int BATCH_SIZE = 1000;
        List<SwiftCodeInfo> swiftCodeInfoListBatch = new ArrayList<>(BATCH_SIZE);
        Resource resource = new ClassPathResource(this.csvFilePath);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("COUNTRY ISO2 CODE", "SWIFT CODE", "CODE TYPE", "NAME", "ADDRESS", "TOWN NAME", "COUNTRY NAME", "TIME ZONE")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        log.info("Starting SWIFT code data initialization from CSV: {}", this.csvFilePath);
        long recordCount = 0;
        long successfullyMappedCount = 0;
        long errorCount = 0;

        try(Reader reader = new InputStreamReader(resource.getInputStream());
            CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : csvParser) {
                recordCount++;
                try {
                    String swiftCode = record.get("SWIFT CODE");
                    String countryIso2 = record.get("COUNTRY ISO2 CODE");
                    String countryName = record.get("COUNTRY NAME");
                    String bankName = record.get("NAME");
                    String address = record.get("ADDRESS");
                    String townName = record.get("TOWN NAME");


                    if (swiftCode == null || swiftCode.isEmpty() ||
                            countryIso2 == null || countryIso2.isEmpty() ||
                            bankName == null || bankName.isEmpty() ||
                            countryName == null || countryName.isEmpty()) {
                        log.warn("Record {}: Skipping due to missing critical data (SWIFT, ISO2, BankName, or CountryName).", record.getRecordNumber());
                        errorCount++;
                        continue;
                    }

                    if (!(swiftCode.length() == 8 || swiftCode.length() == 11)) {
                        log.warn("Record {}: Invalid SWIFT code length ('{}'). Expected 8 or 11 characters. Skipping record.",
                                record.getRecordNumber(), swiftCode);
                        errorCount++;
                        continue;
                    }

                    String embeddedCountryCode = swiftCode.substring(4, 6);
                    if (!embeddedCountryCode.equalsIgnoreCase(countryIso2)) {
                        log.warn("Record {}: SWIFT code country part ('{}') does not match provided Country ISO2 ('{}'). Skipping record.",
                                record.getRecordNumber(), embeddedCountryCode, countryIso2);
                        errorCount++;
                        continue;
                    }

                    SwiftCodeInfo swiftCodeInfo = new SwiftCodeInfo();

                    swiftCodeInfo.setSwiftCode(swiftCode);
                    swiftCodeInfo.setBankName(bankName);
                    swiftCodeInfo.setAddress((address != null && !address.isEmpty()) ? address : null);
                    swiftCodeInfo.setTownName((townName != null && !townName.isEmpty()) ? townName : null);
                    swiftCodeInfo.setCountryISO2(countryIso2.toUpperCase());
                    swiftCodeInfo.setCountryName(countryName.toUpperCase());
                    swiftCodeInfo.setHeadquarter(swiftCode.endsWith("XXX"));

                    swiftCodeInfoListBatch.add(swiftCodeInfo);
                    successfullyMappedCount++;

                    if (swiftCodeInfoListBatch.size() >= BATCH_SIZE) {
                        repository.saveAll(swiftCodeInfoListBatch);
                        log.debug("Saved batch of {} records.", swiftCodeInfoListBatch.size());
                        swiftCodeInfoListBatch.clear();
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Skipping record {} due to invalid data or missing header: {} - Record: {}", record.getRecordNumber(), e.getMessage(), record.toString());
                    errorCount++;
                } catch (Exception e) {
                    log.error("Error processing record {}: {}", record.getRecordNumber(), record.toString(), e);
                    errorCount++;
                }
            }
            if (!swiftCodeInfoListBatch.isEmpty()) {
                repository.saveAll(swiftCodeInfoListBatch);
                log.info("Saved final batch of {} records.", swiftCodeInfoListBatch.size());
            }
            log.info("Finished processing CSV file. Total records processed: {}. Records successfully loaded: {}. Errors/Skipped: {}",
                    recordCount, recordCount - errorCount, errorCount);
        }
        catch (Exception e) {
            log.error("Failed to load data from CSV file: {}", csvFilePath, e);
        }
    }



}
