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

@Component
public class DataInitializer implements CommandLineRunner {

    Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final SwiftCodeInfoRepository repository;

    private final String csvFilePath = "data/swift_code_data.csv";

    @Autowired
    public DataInitializer(SwiftCodeInfoRepository swiftCodeInfoRepository) {
        this.repository = swiftCodeInfoRepository;
    }

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

    private void loadDataFromCsv() {
        List<SwiftCodeInfo> swiftCodeInfoListBatch = new ArrayList<>();
        Resource resource = new ClassPathResource(csvFilePath);

        int batchSize = 1000; //saving data in batches can improve performance

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader("COUNTRY ISO2 CODE", "SWIFT CODE", "CODE TYPE", "NAME", "ADDRESS", "TOWN NAME", "COUNTRY NAME", "TIME ZONE")
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        log.info("Starting CSV parsing from {}", csvFilePath);
        long recordCount = 0;
        long errorCount = 0;

        try(Reader reader = new InputStreamReader(resource.getInputStream());
            CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            for (CSVRecord record : csvParser) {
                recordCount++;
                try {
                    SwiftCodeInfo swiftCodeInfo = new SwiftCodeInfo();
                    String swiftCode = record.get("SWIFT CODE");
                    String countryIso2 = record.get("COUNTRY ISO2 CODE");
                    String countryName = record.get("COUNTRY NAME");
                    String bankName = record.get("NAME");
                    String address = record.get("ADDRESS");
                    String townName = record.get("TOWN NAME");

                    if (swiftCode == null || swiftCode.trim().isEmpty() ||
                            countryIso2 == null || countryIso2.trim().isEmpty() ||
                            bankName == null || bankName.trim().isEmpty() ||
                            countryName == null || countryName.trim().isEmpty() ||
                            townName == null || townName.trim().isEmpty()) {
                        log.warn("Skipping record {} due to missing critical data (SWIFT, ISO2, BankName, CountryName, or TownName).", record.getRecordNumber());
                        errorCount++;
                        continue;
                    }

                    swiftCodeInfo.setSwiftCode(swiftCode.trim());
                    swiftCodeInfo.setBankName(bankName);
                    swiftCodeInfo.setAddress( (address != null && !address.trim().isEmpty()) ? address : null );
                    swiftCodeInfo.setTownName(townName);

                    swiftCodeInfo.setCountryISO2(countryIso2.toUpperCase());
                    swiftCodeInfo.setCountryName(countryName.toUpperCase());

                    swiftCodeInfo.setHeadquarter(swiftCode.trim().endsWith("XXX"));

                    swiftCodeInfoListBatch.add(swiftCodeInfo);

                    if (swiftCodeInfoListBatch.size() >= batchSize) {
                        repository.saveAll(swiftCodeInfoListBatch);
                        log.info("Saved batch of {} records.", swiftCodeInfoListBatch.size());
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
