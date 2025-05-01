package io.github.xhamera1.swiftcodeapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeRequest;
import io.github.xhamera1.swiftcodeapi.exceptions.GlobalExceptionHandler;
import io.github.xhamera1.swiftcodeapi.exceptions.InconsistentSwiftDataException;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceAlreadyExistsException;
import io.github.xhamera1.swiftcodeapi.service.SwiftCodeApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import java.util.stream.Stream;


import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SwiftCodeController.class)
@Import(GlobalExceptionHandler.class)
class SwiftCodeControllerAddSwiftCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SwiftCodeApiService swiftCodeApiService;

    @Autowired
    private ObjectMapper objectMapper;

    private SwiftCodeRequest validRequestDto;
    private MessageResponse successResponse;

    @BeforeEach
    void setUp() {
        validRequestDto = new SwiftCodeRequest();
        validRequestDto.setSwiftCode("AAISALTRXXX");
        validRequestDto.setBankName("UNITED BANK OF ALBANIA SH.A");
        validRequestDto.setAddress("Valid HQ Address 123");
        validRequestDto.setCountryISO2("AL");
        validRequestDto.setCountryName("ALBANIA");
        validRequestDto.setIsHeadquarter(true);

        successResponse = new MessageResponse("SWIFT code 'AAISALTRXXX' added successfully.");
    }



    @Test
    @DisplayName("POST /v1/swift-codes - Should return 201 Created when request is valid and service succeeds")
    void addSwiftCode_whenRequestValid_shouldReturnCreated() throws Exception {
        given(swiftCodeApiService.addSwiftCode(any(SwiftCodeRequest.class))).willReturn(successResponse);

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDto)));

        response.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(successResponse.getMessage())));

        verify(swiftCodeApiService).addSwiftCode(any(SwiftCodeRequest.class));
    }


    @Test
    @DisplayName("POST /v1/swift-codes - Should return 409 Conflict when service throws ResourceAlreadyExistsException")
    void addSwiftCode_whenCodeAlreadyExists_shouldReturnConflict() throws Exception {
        String errorMessage = "SWIFT code 'VALIDPLPWXXX' already exists.";
        given(swiftCodeApiService.addSwiftCode(any(SwiftCodeRequest.class)))
                .willThrow(new ResourceAlreadyExistsException(errorMessage));

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDto)));

        response.andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(swiftCodeApiService).addSwiftCode(any(SwiftCodeRequest.class));
    }

    @Test
    @DisplayName("POST /v1/swift-codes - Should return 400 Bad Request when service throws InconsistentSwiftDataException")
    void addSwiftCode_whenDataIsInconsistent_shouldReturnBadRequest() throws Exception {
        String errorMessage = "Data consistency error: Country code mismatch.";
        given(swiftCodeApiService.addSwiftCode(any(SwiftCodeRequest.class)))
                .willThrow(new InconsistentSwiftDataException(errorMessage));

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDto)));

        response.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(errorMessage)));

        verify(swiftCodeApiService).addSwiftCode(any(SwiftCodeRequest.class));
    }


    @Test
    @DisplayName("POST /v1/swift-codes - Should return 415 Unsupported Media Type when Content-Type is wrong")
    void addSwiftCode_whenContentTypeIsWrong_shouldReturnUnsupportedMediaType() throws Exception {
        String requestBody = objectMapper.writeValueAsString(validRequestDto);

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.TEXT_PLAIN)
                .content(requestBody));

        response.andExpect(status().isUnsupportedMediaType());

        verify(swiftCodeApiService, never()).addSwiftCode(any(SwiftCodeRequest.class));
    }

    @Test
    @DisplayName("POST /v1/swift-codes - Should return 400 Bad Request when JSON body is malformed")
    void addSwiftCode_whenJsonIsMalformed_shouldReturnBadRequest() throws Exception {
        String malformedJson = "{\"swiftCode\":\"MALFORMEDPLX\", \"bankName\":\"Test\"";

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson));

        response.andExpect(status().isBadRequest());

        verify(swiftCodeApiService, never()).addSwiftCode(any(SwiftCodeRequest.class));
    }

    @Test
    @DisplayName("POST /v1/swift-codes - Should return 400 Bad Request when request body is empty")
    void addSwiftCode_whenBodyIsEmpty_shouldReturnBadRequest() throws Exception {
        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""));
        response.andExpect(status().isBadRequest());
        verify(swiftCodeApiService, never()).addSwiftCode(any(SwiftCodeRequest.class));
    }

    @Test
    @DisplayName("POST /v1/swift-codes - Should return 500 Internal Server Error when service throws unexpected exception")
    void addSwiftCode_whenServiceThrowsError_shouldReturnInternalServerError() throws Exception {
        given(swiftCodeApiService.addSwiftCode(any(SwiftCodeRequest.class)))
                .willThrow(new RuntimeException("Unexpected service failure!"));

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequestDto)));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("An internal server error occurred. Please try again later.")));

        verify(swiftCodeApiService).addSwiftCode(any(SwiftCodeRequest.class));
    }


    private static Stream<SwiftCodeRequest> invalidRequestProvider() {
        SwiftCodeRequest baseRequest = new SwiftCodeRequest();
        baseRequest.setSwiftCode("VALIDPLPWXXX");
        baseRequest.setBankName("Valid Bank HQ");
        baseRequest.setAddress("Valid HQ Address 123");
        baseRequest.setCountryISO2("PL");
        baseRequest.setCountryName("POLAND");
        baseRequest.setIsHeadquarter(true);

        return Stream.of(
                createModifiedRequest(baseRequest, req -> req.setSwiftCode(null)),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("")),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("   ")),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("INVALID")),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("BANKPLPWW")),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("BANKPLPWXXX1")),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("bankplpwxxx")),
                createModifiedRequest(baseRequest, req -> req.setSwiftCode("1234PLPXXXX")),

                createModifiedRequest(baseRequest, req -> req.setBankName(null)),
                createModifiedRequest(baseRequest, req -> req.setBankName("  ")),

                createModifiedRequest(baseRequest, req -> req.setAddress(null)),
                createModifiedRequest(baseRequest, req -> req.setAddress("")),
                createModifiedRequest(baseRequest, req -> req.setAddress("a".repeat(513))),

                createModifiedRequest(baseRequest, req -> req.setCountryISO2(null)),
                createModifiedRequest(baseRequest, req -> req.setCountryISO2(" ")),
                createModifiedRequest(baseRequest, req -> req.setCountryISO2("P")),
                createModifiedRequest(baseRequest, req -> req.setCountryISO2("POL")),

                createModifiedRequest(baseRequest, req -> req.setCountryISO2("P1")),
                createModifiedRequest(baseRequest, req -> req.setCountryName(null)),
                createModifiedRequest(baseRequest, req -> req.setCountryName("")),

                createModifiedRequest(baseRequest, req -> req.setIsHeadquarter(null))
        );
    }

    private static SwiftCodeRequest createModifiedRequest(SwiftCodeRequest base, java.util.function.Consumer<SwiftCodeRequest> modifier) {
        SwiftCodeRequest modified = new SwiftCodeRequest();
        modified.setSwiftCode(base.getSwiftCode());
        modified.setBankName(base.getBankName());
        modified.setAddress(base.getAddress());
        modified.setCountryISO2(base.getCountryISO2());
        modified.setCountryName(base.getCountryName());
        modified.setIsHeadquarter(base.getIsHeadquarter());
        modifier.accept(modified);
        return modified;
    }


    @ParameterizedTest
    @MethodSource("invalidRequestProvider")
    @DisplayName("POST /v1/swift-codes - Should return 400 Bad Request for invalid request body fields")
    void addSwiftCode_whenRequestBodyInvalid_shouldReturnBadRequest(SwiftCodeRequest invalidRequest) throws Exception {

        ResultActions response = mockMvc.perform(post("/v1/swift-codes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        response.andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("Validation failed:")));

        verify(swiftCodeApiService, never()).addSwiftCode(any(SwiftCodeRequest.class));
    }
}