package io.github.xhamera1.swiftcodeapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO representing the request body for creating a new SWIFT code entry.
 */
@Data
public class SwiftCodeRequest {

    @NotBlank(message = "SWIFT code cannot be blank")
    @Pattern(regexp = "^[A-Z]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?$", message = "Invalid SWIFT/BIC format. Should be an 8 to 11-character identifier (e.g., BANKPLPWXXX, BANKDEFF)")
    private String swiftCode;

    @NotBlank(message = "Bank name cannot be blank")
    private String bankName;

    @NotBlank(message = "Address cannot be blank")
    @Size(max = 512, message = "Address cannot exceed 512 characters")
    private String address;

    @NotBlank(message = "Country ISO2 code cannot be blank")
    @Size(min = 2, max = 2, message = "Country ISO2 code must be exactly 2 characters")
    @Pattern(regexp = "^[A-Za-z]{2}$", message = "Country ISO2 code must contain only letters")
    private String countryISO2;

    @NotBlank(message = "Country name cannot be blank")
    private String countryName;

    @NotNull(message = "isHeadquarter flag must be provided")
    private Boolean isHeadquarter;


}
