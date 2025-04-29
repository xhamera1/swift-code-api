package io.github.xhamera1.swiftcodeapi.service;

import io.github.xhamera1.swiftcodeapi.dto.CountrySwiftCodesResponse;
import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeRequest;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.InconsistentSwiftDataException;
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

/**
 * Service layer containing the core business logic for managing SWIFT code data.
 * Interacts with the {@link SwiftCodeInfoRepository} for data persistence and
 * handles mapping between {@link SwiftCodeInfo} entities and various DTOs
 * ({@link SwiftCodeRequest}, {@link SwiftCodeResponse}, {@link CountrySwiftCodesResponse}, {@link MessageResponse}).
 * Uses declarative transaction management via {@link Transactional}.
 */
@Service
public class SwiftCodeApiService {

    Logger log = LoggerFactory.getLogger(SwiftCodeApiService.class);
    private final SwiftCodeInfoRepository repository;

    /**
     * Constructs the service and injects the required repository dependency.
     * @param repository The repository for SWIFT code data access.
     */
    @Autowired
    public SwiftCodeApiService(SwiftCodeInfoRepository repository) {
        this.repository = repository;
    }


    /**
     * Retrieves detailed information for a single SWIFT code.
     * If the code represents a headquarters (ends in "XXX"), it also fetches and includes
     * details of associated branch codes (codes starting with the same first 8 characters).
     * The search ignores case for the provided swiftCode.
     *
     * @param swiftCode The 8 or 11 character SWIFT/BIC code to retrieve details for.
     * @return A {@link SwiftCodeResponse} containing the details. Includes a list of branches if the code is a headquarters.
     * @throws ResourceNotFoundException if no SWIFT code matching the provided {@code swiftCode} (case-insensitive) is found.
     */
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


    /**
     * Retrieves all SWIFT codes (both headquarters and branches) associated with a specific country.
     * The country is identified by its ISO 3166-1 alpha-2 code (case-insensitive).
     *
     * @param countryISO2 The 2-letter ISO country code (case is ignored). Cannot be null.
     * @return A {@link CountrySwiftCodesResponse} containing the country details (ISO code and name derived from the first found entry)
     * and a list of {@link SwiftCodeResponse} objects for all codes in that country. Returns an empty list if no codes are found.
     * @throws IllegalArgumentException if the provided {@code countryISO2} is null.
     */
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


    /**
     * Adds a new SWIFT code entry based on the provided request data.
     * Performs several validations:
     * - Checks if a SWIFT code with the same value (case-insensitive) already exists.
     * - Validates consistency between the country code embedded in the SWIFT code (chars 5-6) and the provided countryISO2 field.
     * - Validates consistency between the provided {@code isHeadquarter} flag and the SWIFT code format (ending in "XXX").
     * Converts relevant fields (swiftCode, countryISO2, countryName) to uppercase before saving.
     *
     * @param requestDto The DTO containing the details of the SWIFT code to add. Must pass bean validation defined on {@link SwiftCodeRequest}.
     * @return A {@link MessageResponse} indicating successful addition.
     * @throws ResourceAlreadyExistsException if the SWIFT code already exists.
     * @throws InconsistentSwiftDataException if the provided data is internally inconsistent (country code mismatch or isHeadquarter flag mismatch).
     */
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

        String embeddedCountryCode = swiftCode.substring(4, 6);
        if (!embeddedCountryCode.equals(countryIso2)) {
            String errorMessage = String.format(
                    "Data consistency error: The country code from SWIFT ('%s' in '%s') does not match the provided Country ISO2 ('%s').",
                    embeddedCountryCode, swiftCode, countryIso2
            );
            log.warn("InconsistentSwiftDataException: {}", errorMessage);
            throw new InconsistentSwiftDataException(errorMessage);
        }


        boolean isHqAccordingToCode = swiftCode.endsWith("XXX");
        if (requestDto.getIsHeadquarter() != isHqAccordingToCode) {
            log.warn("Inconsistent isHeadquarter flag for SWIFT code {}. Flag was: {}, expected based on code: {}",
                    swiftCode, requestDto.getIsHeadquarter(), isHqAccordingToCode);
            throw new InconsistentSwiftDataException("Provided 'isHeadquarter' flag (" + requestDto.getIsHeadquarter()
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


    /**
     * Deletes a SWIFT code entry identified by its code.
     * The search for the code to delete ignores case.
     *
     * @param swiftCode The 8 or 11 character SWIFT/BIC code to delete.
     * @return A {@link MessageResponse} indicating successful deletion.
     * @throws ResourceNotFoundException if no SWIFT code matching the provided {@code swiftCode} (case-insensitive) is found.
     */
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


    /**
     * Maps a {@link SwiftCodeInfo} entity to a {@link SwiftCodeResponse} DTO.
     * Constructs the address string by prioritizing the {@code address} field over the {@code townName} field.
     * Optionally includes the country name in the resulting DTO.
     *
     * @param entity The source {@link SwiftCodeInfo} entity.
     * @param includeCountryName If {@code true}, the {@code countryName} from the entity will be included in the DTO; otherwise, it will be null.
     * @return The mapped {@link SwiftCodeResponse} DTO.
     */
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
