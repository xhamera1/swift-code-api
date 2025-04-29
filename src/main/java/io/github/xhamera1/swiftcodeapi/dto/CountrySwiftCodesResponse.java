package io.github.xhamera1.swiftcodeapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the response for querying SWIFT codes by country.
 */
@Data
@Builder
public class CountrySwiftCodesResponse {

    private String countryISO2;
    private String countryName;
    private List<SwiftCodeResponse> swiftCodes;
}
