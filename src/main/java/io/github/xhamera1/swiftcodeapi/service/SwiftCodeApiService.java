package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.dto.CountrySwiftCodesResponse;
import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeRequest;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceAlreadyExistsException;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceNotFoundException;
import io.github.xhamera1.swiftcodeapi.model.SwiftCodeInfo;
import io.github.xhamera1.swiftcodeapi.repository.SwiftCodeInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SwiftCodeApiService {

    Logger log = LoggerFactory.getLogger(SwiftCodeApiService.class);
    private final SwiftCodeInfoRepository repository;

    @Autowired
    public SwiftCodeApiService(SwiftCodeInfoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SwiftCodeResponse getSwiftCodeDetails(String swiftCode) {
        log.debug("Attempting to retrieve details for SWIFT code: {}", swiftCode);

        SwiftCodeInfo swiftCodeInfo = repository.findBySwiftCodeIgnoreCase(swiftCode)
                .orElseThrow(() -> {
                    log.warn("ResourceNotFoundException: SWIFT code '{}' not found.", swiftCode);
                    return new ResourceNotFoundException("SWIFT code '" + swiftCode + "' not found.");
                });
        log.debug("Found SWIFT code info: {}", swiftCodeInfo.getSwiftCode());

        if (swiftCodeInfo.isHeadquarter()) {
            log.debug("SWIFT code {} is a headquarter. Fetching branches.", swiftCodeInfo.getSwiftCode());

            String prefix = swiftCodeInfo.getSwiftCode().substring(0,8);
            List<SwiftCodeInfo> branchEntities = repository.findBySwiftCodeStartingWithIgnoreCaseAndSwiftCodeNotIgnoreCase(prefix, swiftCodeInfo.getSwiftCode());
            log.debug("Found {} branches for prefix '{}'", branchEntities.size(), prefix);

            List<SwiftCodeResponse> branchDtos = branchEntities.stream()
                    .map(branchEntity -> mapEntityToDto(branchEntity, false)) // mapping without Country name
                    .collect(Collectors.toList());

            SwiftCodeResponse response = mapEntityToDto(swiftCodeInfo, true);
            response.setBranches(branchDtos.isEmpty() ? null : branchDtos);

            log.info("Returning details for HQ: {}", swiftCodeInfo.getSwiftCode());
            return response;
        }
        else {
            log.info("Returning details for branch: {}", swiftCodeInfo.getSwiftCode());
            return mapEntityToDto(swiftCodeInfo, true);
        }
    }

    @Transactional(readOnly = true)
    public CountrySwiftCodesResponse getSwiftCodesByCountry(String countryISO2) {
        String processedCountryISO2 = Optional.ofNullable(countryISO2)
                .map(String::toUpperCase)
                .orElseThrow(() -> new IllegalArgumentException("Country ISO2 code cannot be null"));

        log.debug("Attempting to retrieve SWIFT codes for country: {}", processedCountryISO2);

        List<SwiftCodeInfo> entities = repository.findByCountryISO2IgnoreCase(processedCountryISO2);

        List<SwiftCodeResponse> swiftCodeDtos = entities.stream()
                .map(entity -> mapEntityToDto(entity, false))
                .collect(Collectors.toList());

        String countryName = "";
        if (!entities.isEmpty()) {

            countryName = Optional.ofNullable(entities.get(0).getCountryName())
                    .map(String::toUpperCase)
                    .orElse("");
        } else {
            log.info("No SWIFT codes found for country code: {}", processedCountryISO2);
        }

        log.info("Found {} SWIFT codes for country {}", swiftCodeDtos.size(), processedCountryISO2);

        return CountrySwiftCodesResponse.builder()
                .countryISO2(processedCountryISO2)
                .countryName(countryName)
                .swiftCodes(swiftCodeDtos)
                .build();
    }

    @Transactional
    public MessageResponse addSwiftCode(SwiftCodeRequest requestDto) {
        String swiftCode = requestDto.getSwiftCode().trim().toUpperCase();
        String countryIso2 = requestDto.getCountryISO2().toUpperCase();
        String countryName = requestDto.getCountryName().toUpperCase();

        log.debug("Attempting to add SWIFT code: {}", swiftCode);

        if (repository.existsBySwiftCodeIgnoreCase(swiftCode)) {
            log.warn("Attempted to add duplicate SWIFT code: {}", swiftCode);
            throw new ResourceAlreadyExistsException("SWIFT code '" + swiftCode + "' already exists.");
        }

        boolean isHqAccordingToCode = swiftCode.endsWith("XXX");
        if (requestDto.getIsHeadquarter() != isHqAccordingToCode) {
            log.warn("Inconsistent isHeadquarter flag for SWIFT code {}. Flag was: {}, expected based on code: {}",
                    swiftCode, requestDto.getIsHeadquarter(), isHqAccordingToCode);
            throw new IllegalArgumentException("Provided 'isHeadquarter' flag (" + requestDto.getIsHeadquarter()
                    + ") is inconsistent with the SWIFT code format (" + swiftCode + ").");
        }

        SwiftCodeInfo newSwiftCodeInfo = new SwiftCodeInfo();
        newSwiftCodeInfo.setSwiftCode(swiftCode);
        newSwiftCodeInfo.setBankName(requestDto.getBankName());
        newSwiftCodeInfo.setAddress(requestDto.getAddress());
        //  newSwiftCodeInfo.setTownName(null) // null is default
        newSwiftCodeInfo.setCountryISO2(countryIso2);
        newSwiftCodeInfo.setCountryName(countryName);
        newSwiftCodeInfo.setHeadquarter(isHqAccordingToCode);

        repository.save(newSwiftCodeInfo);
        log.info("Successfully added SWIFT code: {}", swiftCode);

        return new MessageResponse("SWIFT code '" + swiftCode + "' added successfully.");
    }

    @Transactional
    public MessageResponse deleteSwiftCode(String swiftCode) {
        String processedSwiftCode = swiftCode.trim().toUpperCase();
        log.debug("Attempting to delete SWIFT code: {}", processedSwiftCode);

        SwiftCodeInfo swiftCodeToDelete = repository.findBySwiftCodeIgnoreCase(processedSwiftCode)
                .orElseThrow(() -> {
                    log.warn("Attempted to delete non-existent SWIFT code: {}", processedSwiftCode);
                    return new ResourceNotFoundException("SWIFT code '" + processedSwiftCode + "' not found, cannot delete.");
                });

        repository.delete(swiftCodeToDelete);
        log.info("Successfully deleted SWIFT code: {}", processedSwiftCode);

        return new MessageResponse("SWIFT code '" + processedSwiftCode + "' deleted successfully.");
    }


    private SwiftCodeResponse mapEntityToDto(SwiftCodeInfo entity, boolean includeCountryName) {
        String finalAddressString;
        String dbAddress = entity.getAddress();
        String dbTownName = entity.getTownName();

        if (dbAddress != null && !dbAddress.trim().isEmpty()) {
            finalAddressString = dbAddress;
        } else {
            finalAddressString = dbTownName;
        }

        if (finalAddressString == null) {
            finalAddressString = "";
        }

        return SwiftCodeResponse.builder()
                .swiftCode(entity.getSwiftCode())
                .bankName(entity.getBankName())
                .address(finalAddressString)
                .countryISO2(entity.getCountryISO2())
                .countryName(includeCountryName ? entity.getCountryName() : null)
                .isHeadquarter(entity.isHeadquarter())
                .build();
    }
}
