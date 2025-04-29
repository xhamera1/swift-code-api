package io.github.xhamera1.swiftcodeapi.controller;

import io.github.xhamera1.swiftcodeapi.dto.CountrySwiftCodesResponse;
import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeRequest;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.service.SwiftCodeApiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller handling HTTP requests related to SWIFT/BIC codes.
 * Exposes endpoints for retrieving, adding, and deleting SWIFT code information.
 * Delegates all business logic to the {@link SwiftCodeApiService}.
 * Exceptions are handled globally by {@link io.github.xhamera1.swiftcodeapi.exceptions.GlobalExceptionHandler}.
 */
@RestController()
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {

    public static final Logger log = LoggerFactory.getLogger(SwiftCodeController.class);
    public final SwiftCodeApiService swiftCodeApiService;

    /**
     * Constructs the controller and injects the required service dependency.
     * @param swiftCodeApiService The service responsible for SWIFT code business logic.
     */
    @Autowired
    public SwiftCodeController(SwiftCodeApiService swiftCodeApiService) {
        this.swiftCodeApiService = swiftCodeApiService;
    }

    /**
     * Handles GET requests to retrieve details for a specific SWIFT code.
     * Corresponds to Endpoint 1. Supports both headquarters (including branches) and individual branches.
     *
     * Path: GET /v1/swift-codes/{swift-code}
     *
     * @param swiftCode The 8 or 11-character SWIFT/BIC code requested in the path.
     * @return A {@link ResponseEntity} containing the {@link SwiftCodeResponse} with code details
     * (status 200 OK) if found. Potential error responses (e.g., 404 Not Found if the code
     * doesn't exist) are handled by the GlobalExceptionHandler.
     */
    @GetMapping("{swift-code}")
    public ResponseEntity<SwiftCodeResponse> getDetailsFromSwiftCode(@PathVariable(name = "swift-code") String swiftCode) {
        log.info("Received request to get details for SWIFT code: {}", swiftCode);
        SwiftCodeResponse swiftCodeResponse = swiftCodeApiService.getSwiftCodeDetails(swiftCode);
        log.info("Returning details for SWIFT code: {}", swiftCode);
        return ResponseEntity.ok(swiftCodeResponse);
    }

    /**
     * Handles GET requests to retrieve all SWIFT codes associated with a specific country.
     * Corresponds to Endpoint 2.
     *
     * Path: GET /v1/swift-codes/country/{countryISO2code}
     *
     * @param countryISO2code The 2-letter ISO country code requested in the path.
     * @return A {@link ResponseEntity} containing the {@link CountrySwiftCodesResponse}
     * with country details and a list of associated SWIFT codes (status 200 OK).
     * Returns an empty list if no codes are found for the country.
     */
    @GetMapping("/country/{countryISO2code}")
    public ResponseEntity<CountrySwiftCodesResponse> getDetailsForCountry(@PathVariable(name = "countryISO2code") String countryISO2code) {
        log.info("Received request to get details by country with ISO2code {}", countryISO2code);
        CountrySwiftCodesResponse countrySwiftCodesResponse = swiftCodeApiService.getSwiftCodesByCountry(countryISO2code);
        log.info("Returning {} codes for country {}", countrySwiftCodesResponse.getSwiftCodes().size(), countryISO2code);
        return ResponseEntity.ok(countrySwiftCodesResponse);
    }

    /**
     * Handles POST requests to add a new SWIFT code entry to the database.
     * Corresponds to Endpoint 3. Input data is validated based on annotations in {@link SwiftCodeRequest}.
     *
     * Path: POST /v1/swift-codes
     *
     * @param requestDto The request body containing the details of the SWIFT code to add.
     * Must conform to the {@link SwiftCodeRequest} structure and validation rules.
     * @return A {@link ResponseEntity} containing a {@link MessageResponse} indicating success
     * (status 201 Created). Potential error responses (e.g., 400 Bad Request for validation errors,
     * 409 Conflict if the code already exists) are handled by the GlobalExceptionHandler.
     */
    @PostMapping
    public ResponseEntity<MessageResponse> addSwiftCode(@Valid @RequestBody SwiftCodeRequest requestDto) {
        log.info("Received POST request to add SWIFT code: {}", requestDto.getSwiftCode());
        MessageResponse messageResponse = swiftCodeApiService.addSwiftCode(requestDto);
        log.info("Successfully processed POST request for SWIFT code: {}", requestDto.getSwiftCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }

    /**
     * Handles DELETE requests to remove a specific SWIFT code entry from the database.
     * Corresponds to Endpoint 4.
     *
     * Path: DELETE /v1/swift-codes/{swift-code}
     *
     * @param swiftCode The 8 or 11-character SWIFT/BIC code to delete, specified in the path.
     * @return A {@link ResponseEntity} containing a {@link MessageResponse} indicating success
     * (status 200 OK). Potential error responses (e.g., 404 Not Found if the code
     * doesn't exist) are handled by the GlobalExceptionHandler.
     */
    @DeleteMapping("/{swift-code}")
    public ResponseEntity<MessageResponse> deleteSwiftCode(@PathVariable(name = "swift-code") String swiftCode) {
        log.info("Received DELETE request for SWIFT code: {}", swiftCode);
        MessageResponse messageResponse = swiftCodeApiService.deleteSwiftCode(swiftCode);
        log.info("Successfully processed DELETE request for SWIFT code: {}", swiftCode);
        return ResponseEntity.ok(messageResponse);
    }
}
