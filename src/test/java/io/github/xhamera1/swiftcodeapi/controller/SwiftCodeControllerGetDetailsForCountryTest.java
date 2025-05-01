package io.github.xhamera1.swiftcodeapi.controller;

import io.github.xhamera1.swiftcodeapi.dto.CountrySwiftCodesResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.GlobalExceptionHandler;
import io.github.xhamera1.swiftcodeapi.service.SwiftCodeApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// MockBean is deprecated but it's still best way to mock the service for this @WebMvcTest.
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SwiftCodeController.class)
@Import(GlobalExceptionHandler.class)
class SwiftCodeControllerGetDetailsForCountryTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SwiftCodeApiService swiftCodeApiService;

    private CountrySwiftCodesResponse responseWithCodes;
    private CountrySwiftCodesResponse responseWithoutCodes;

    @BeforeEach
    void setUp() {
        responseWithCodes = CountrySwiftCodesResponse.builder()
                .countryISO2("PL")
                .countryName("POLAND")
                .swiftCodes(Arrays.asList(
                        SwiftCodeResponse.builder()
                                .swiftCode("AIPOPLP1XXX")
                                .bankName("SANTANDER CONSUMER BANK SPOLKA AKCYJNA")
                                .address("STRZEGOMSKA 42C  WROCLAW, DOLNOSLASKIE, 53-611")
                                .countryISO2("PL")
                                .isHeadquarter(true)
                                .countryName(null)
                                .build(),
                        SwiftCodeResponse.builder()
                                .swiftCode("DEUTPLPX")
                                .bankName("DEUTSCHE BANK POLSKA S.A.")
                                .address("FOCUS AL. ARMII LUDOWEJ 26")
                                .countryISO2("PL")
                                .isHeadquarter(false)
                                .countryName(null)
                                .build()
                ))
                .build();

        responseWithoutCodes = CountrySwiftCodesResponse.builder()
                .countryISO2("XX")
                .countryName("")
                .swiftCodes(Collections.emptyList())
                .build();
    }


    @Test
    @DisplayName("GET /v1/swift-codes/country/{code} - Should return 200 OK with country details when codes exist")
    void getDetailsForCountry_whenCodesExist_shouldReturnOkWithDetails() throws Exception {
        String countryCode = "PL";
        given(swiftCodeApiService.getSwiftCodesByCountry(countryCode)).willReturn(responseWithCodes);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/country/{countryISO2code}", countryCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.countryName", is("POLAND")))
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)))
                .andExpect(jsonPath("$.swiftCodes[0].swiftCode", is("AIPOPLP1XXX")))
                .andExpect(jsonPath("$.swiftCodes[0].isHeadquarter", is(true)))
                .andExpect(jsonPath("$.swiftCodes[0].countryName").doesNotExist())
                .andExpect(jsonPath("$.swiftCodes[1].swiftCode", is("DEUTPLPX")))
                .andExpect(jsonPath("$.swiftCodes[1].isHeadquarter", is(false)));

        verify(swiftCodeApiService).getSwiftCodesByCountry(countryCode);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/country/{code} - Should return 200 OK with empty list when no codes exist")
    void getDetailsForCountry_whenNoCodesExist_shouldReturnOkWithEmptyList() throws Exception {
        String countryCode = "XX";
        given(swiftCodeApiService.getSwiftCodesByCountry(countryCode)).willReturn(responseWithoutCodes);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/country/{countryISO2code}", countryCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2", is("XX")))
                .andExpect(jsonPath("$.countryName", is("")))
                .andExpect(jsonPath("$.swiftCodes", is(empty())));

        verify(swiftCodeApiService).getSwiftCodesByCountry(countryCode);
    }


    @Test
    @DisplayName("GET /v1/swift-codes/country/{code} - Should handle case insensitivity in path variable")
    void getDetailsForCountry_shouldHandleCaseInsensitivity() throws Exception {
        String countryCodeLower = "pl";
        given(swiftCodeApiService.getSwiftCodesByCountry(countryCodeLower)).willReturn(responseWithCodes);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/country/{countryISO2code}", countryCodeLower)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)));

        verify(swiftCodeApiService).getSwiftCodesByCountry(countryCodeLower);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/country/{code} - Should return 200 OK with empty list for non-2-letter code")
    void getDetailsForCountry_whenPathVarIsNonStandardFormat_shouldReturnOkWithEmptyList() throws Exception {
        String nonStandardCountryCode = "POL";
        CountrySwiftCodesResponse emptyResponse = CountrySwiftCodesResponse.builder()
                .countryISO2(nonStandardCountryCode)
                .countryName("")
                .swiftCodes(Collections.emptyList())
                .build();

        given(swiftCodeApiService.getSwiftCodesByCountry(nonStandardCountryCode)).willReturn(emptyResponse);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/country/{countryISO2code}", nonStandardCountryCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.countryISO2", is(nonStandardCountryCode)))
                .andExpect(jsonPath("$.countryName", is("")))
                .andExpect(jsonPath("$.swiftCodes", is(empty())));

        verify(swiftCodeApiService).getSwiftCodesByCountry(nonStandardCountryCode);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/co/{code} - Should return 500 for completely wrong path due to test env/handler interaction")
    void getDetailsForCountry_whenBasePathIsWrong_shouldReturnInternalServerError() throws Exception {
        String countryCode = "PL";

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/co/{countryISO2code}", countryCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("An internal server error occurred. Please try again later.")));


        verify(swiftCodeApiService, never()).getSwiftCodesByCountry(anyString());
    }


    @Test
    @DisplayName("GET /v1/swift-codes/country/{code} - Should return 500 Internal Server Error when service throws unexpected exception")
    void getDetailsForCountry_whenServiceThrowsError_shouldReturnInternalServerError() throws Exception {
        String countryCode = "ER";
        String errorMessage = "Unexpected service failure!";
        given(swiftCodeApiService.getSwiftCodesByCountry(countryCode))
                .willThrow(new RuntimeException(errorMessage));


        ResultActions response = mockMvc.perform(get("/v1/swift-codes/country/{countryISO2code}", countryCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("An internal server error occurred. Please try again later.")));

        verify(swiftCodeApiService).getSwiftCodesByCountry(countryCode);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/country/ - Should return 500 when path variable is missing due to test env/handler interaction")
    void getDetailsForCountry_whenPathVariableIsMissing_shouldReturnInternalServerError() throws Exception {
        ResultActions response = mockMvc.perform(get("/v1/swift-codes/country/")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("An internal server error occurred. Please try again later.")));

        verify(swiftCodeApiService, never()).getSwiftCodesByCountry(anyString());
    }
}