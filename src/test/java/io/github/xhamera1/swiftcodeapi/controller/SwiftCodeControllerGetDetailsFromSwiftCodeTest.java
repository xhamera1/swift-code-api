package io.github.xhamera1.swiftcodeapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.GlobalExceptionHandler;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceNotFoundException;
import io.github.xhamera1.swiftcodeapi.service.SwiftCodeApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// MockBean is deprecated, but it's still best way to mock the service for this @WebMvcTest.
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SwiftCodeController.class)
@Import(GlobalExceptionHandler.class)
class SwiftCodeControllerGetDetailsFromSwiftCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SwiftCodeApiService swiftCodeApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private SwiftCodeResponse hqResponseDto;
    private SwiftCodeResponse branchResponseDto;

    @BeforeEach
    void setUp() {
        hqResponseDto = SwiftCodeResponse.builder()
                .swiftCode("AAISALTRXXX")
                .bankName("UNITED BANK OF ALBANIA SH.A")
                .address("HYRJA 3 RR. DRITAN HOXHA ND. 11 TIRANA, TIRANA, 1023")
                .countryISO2("AL")
                .countryName("ALBANIA")
                .isHeadquarter(true)
                .branches(Collections.singletonList(
                        SwiftCodeResponse.builder()
                                .swiftCode("AAISALTRB01")
                                .bankName("UNITED BANK OF ALBANIA SH.A - BRANCH 1")
                                .address("Branch Address 1")
                                .countryISO2("AL")
                                .isHeadquarter(false)
                                .countryName(null)
                                .build()
                ))
                .build();

        branchResponseDto = SwiftCodeResponse.builder()
                .swiftCode("DEUTPLPX")
                .bankName("DEUTSCHE BANK POLSKA S.A.")
                .address("FOCUS AL. ARMII LUDOWEJ 26")
                .countryISO2("PL")
                .countryName("POLAND")
                .isHeadquarter(false)
                .branches(null)
                .build();
    }


    @Test
    @DisplayName("GET /v1/swift-codes/{swift-code} - Should return 200 OK with HQ details when code exists")
    void getDetailsFromSwiftCode_whenHqCodeExists_shouldReturnOkWithDetails() throws Exception {
        String swiftCode = hqResponseDto.getSwiftCode();
        given(swiftCodeApiService.getSwiftCodeDetails(swiftCode)).willReturn(hqResponseDto);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/{swift-code}", swiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.swiftCode", is(hqResponseDto.getSwiftCode())))
                .andExpect(jsonPath("$.bankName", is(hqResponseDto.getBankName())))
                .andExpect(jsonPath("$.address", is(hqResponseDto.getAddress())))
                .andExpect(jsonPath("$.countryISO2", is(hqResponseDto.getCountryISO2())))
                .andExpect(jsonPath("$.countryName", is(hqResponseDto.getCountryName())))
                .andExpect(jsonPath("$.isHeadquarter", is(true)))
                .andExpect(jsonPath("$.branches").exists())
                .andExpect(jsonPath("$.branches.length()", is(1)))
                .andExpect(jsonPath("$.branches[0].swiftCode", is("AAISALTRB01")))
                .andExpect(jsonPath("$.branches[0].countryName").doesNotExist());

        verify(swiftCodeApiService).getSwiftCodeDetails(swiftCode);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/{swift-code} - Should return 200 OK with Branch details when code exists")
    void getDetailsFromSwiftCode_whenBranchCodeExists_shouldReturnOkWithDetails() throws Exception {
        String swiftCode = branchResponseDto.getSwiftCode();
        given(swiftCodeApiService.getSwiftCodeDetails(swiftCode)).willReturn(branchResponseDto);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/{swift-code}", swiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.swiftCode", is(branchResponseDto.getSwiftCode())))
                .andExpect(jsonPath("$.bankName", is(branchResponseDto.getBankName())))
                .andExpect(jsonPath("$.countryISO2", is(branchResponseDto.getCountryISO2())))
                .andExpect(jsonPath("$.countryName", is(branchResponseDto.getCountryName())))
                .andExpect(jsonPath("$.isHeadquarter", is(false)))
                .andExpect(jsonPath("$.branches").doesNotExist());

        verify(swiftCodeApiService).getSwiftCodeDetails(swiftCode);
    }


    @Test
    @DisplayName("GET /v1/swift-codes/{swift-code} - Should return 404 Not Found when code does not exist")
    void getDetailsFromSwiftCode_whenCodeNotFound_shouldReturnNotFound() throws Exception {
        String nonExistentSwiftCode = "XXXXXXXXXXX";
        String expectedErrorMessage = "SWIFT code '" + nonExistentSwiftCode + "' not found.";
        given(swiftCodeApiService.getSwiftCodeDetails(nonExistentSwiftCode))
                .willThrow(new ResourceNotFoundException(expectedErrorMessage));

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/{swift-code}", nonExistentSwiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(expectedErrorMessage)));

        verify(swiftCodeApiService).getSwiftCodeDetails(nonExistentSwiftCode);
    }


    @Test
    @DisplayName("GET /v1/swift-codes/{swift-code} - Should handle case insensitivity in path variable")
    void getDetailsFromSwiftCode_shouldHandleCaseInsensitivity() throws Exception {
        String swiftCodeLower = "deutplpx";
        given(swiftCodeApiService.getSwiftCodeDetails(swiftCodeLower)).willReturn(branchResponseDto);

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/{swift-code}", swiftCodeLower)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode", is(branchResponseDto.getSwiftCode())));

        verify(swiftCodeApiService).getSwiftCodeDetails(swiftCodeLower);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/{swift-code} - Should return 500 Internal Server Error when service throws unexpected exception")
    void getDetailsFromSwiftCode_whenServiceThrowsError_shouldReturnInternalServerError() throws Exception {
        String swiftCode = "ERRORCODE";
        given(swiftCodeApiService.getSwiftCodeDetails(swiftCode))
                .willThrow(new RuntimeException("Unexpected service failure!"));

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/{swift-code}", swiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("An internal server error occurred. Please try again later.")));

        verify(swiftCodeApiService).getSwiftCodeDetails(swiftCode);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/{swift-code} - Should return 404 Not Found for invalid format variable if service throws NotFound")
    void getDetailsFromSwiftCode_whenPathVarIsInvalidFormat_shouldReturnNotFound() throws Exception {
        String invalidFormatSwiftCode = "TOOLONGCODE";
        String expectedErrorMessage = "SWIFT code '" + invalidFormatSwiftCode + "' not found.";
        given(swiftCodeApiService.getSwiftCodeDetails(invalidFormatSwiftCode))
                .willThrow(new ResourceNotFoundException(expectedErrorMessage));

        ResultActions response = mockMvc.perform(get("/v1/swift-codes/{swift-code}", invalidFormatSwiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(expectedErrorMessage)));

        verify(swiftCodeApiService).getSwiftCodeDetails(invalidFormatSwiftCode);
    }

    @Test
    @DisplayName("GET /v1/swift-codes/co/{code} - Should return 404 Not Found for wrong base path when getting details by SWIFT code")
    void getDetailsFromSwiftCode_whenBasePathIsWrong_shouldReturnNotFound() throws Exception {
        String swiftCode = "ANYCODE";
        String expectedPath = "/v1/swift-codes/co/" + swiftCode;
        ResultActions response = mockMvc.perform(get(expectedPath)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("The requested resource path '" + expectedPath + "' could not be found")));

        verify(swiftCodeApiService, never()).getSwiftCodeDetails(anyString());
        verify(swiftCodeApiService, never()).getSwiftCodesByCountry(anyString());
    }
}