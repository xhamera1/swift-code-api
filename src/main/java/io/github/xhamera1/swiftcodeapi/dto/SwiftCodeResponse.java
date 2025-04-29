package io.github.xhamera1.swiftcodeapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "address",
        "bankName",
        "countryISO2",
        "countryName",
        "isHeadquarter",
        "swiftCode",
        "branches"
})
public class SwiftCodeResponse {

    private String address;
    private String bankName;
    private String countryISO2;
    private String countryName;

    @JsonProperty("isHeadquarter")
    private boolean isHeadquarter;
    private String swiftCode;
    private List<SwiftCodeResponse> branches;
}
