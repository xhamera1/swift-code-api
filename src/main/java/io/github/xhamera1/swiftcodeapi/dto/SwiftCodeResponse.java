package io.github.xhamera1.swiftcodeapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO representing the details of a SWIFT code (both headquarters and branches)
 * for API responses.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // omits null fields from json output (useful when country name or branches is null)
public class SwiftCodeResponse {

    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;
    private boolean isHeadquarter;
    private String swiftCode;
    private List<SwiftCodeResponse> branches;
}
