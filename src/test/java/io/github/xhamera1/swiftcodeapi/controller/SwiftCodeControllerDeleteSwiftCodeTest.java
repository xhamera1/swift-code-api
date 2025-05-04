package io.github.xhamera1.swiftcodeapi.controller;

import io.github.xhamera1.swiftcodeapi.dto.MessageResponse;
import io.github.xhamera1.swiftcodeapi.exceptions.GlobalExceptionHandler;
import io.github.xhamera1.swiftcodeapi.exceptions.ResourceNotFoundException;
import io.github.xhamera1.swiftcodeapi.service.SwiftCodeApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SwiftCodeController.class)
@Import(GlobalExceptionHandler.class)
class SwiftCodeControllerDeleteSwiftCodeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SwiftCodeApiService swiftCodeApiService;


    @Test
    @DisplayName("DELETE /v1/swift-codes/{swift-code} - Should return 200 OK when code exists and service deletes successfully")
    void deleteSwiftCode_whenCodeExists_shouldReturnOk() throws Exception {
        String swiftCode = "AAISALTRXXX";
        String expectedMessage = "SWIFT code '" + swiftCode + "' deleted successfully.";
        MessageResponse successResponse = new MessageResponse(expectedMessage);

        given(swiftCodeApiService.deleteSwiftCode(swiftCode)).willReturn(successResponse);

        ResultActions response = mockMvc.perform(delete("/v1/swift-codes/{swift-code}", swiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(swiftCodeApiService).deleteSwiftCode(swiftCode);
    }


    @Test
    @DisplayName("DELETE /v1/swift-codes/{swift-code} - Should return 404 Not Found when code does not exist")
    void deleteSwiftCode_whenCodeNotFound_shouldReturnNotFound() throws Exception {
        String nonExistentSwiftCode = "NONEXISTXXX";
        String expectedErrorMessage = "SWIFT code '" + nonExistentSwiftCode + "' not found, cannot delete.";

        given(swiftCodeApiService.deleteSwiftCode(nonExistentSwiftCode))
                .willThrow(new ResourceNotFoundException(expectedErrorMessage));

        ResultActions response = mockMvc.perform(delete("/v1/swift-codes/{swift-code}", nonExistentSwiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(expectedErrorMessage)));

        verify(swiftCodeApiService).deleteSwiftCode(nonExistentSwiftCode);
    }

    @Test
    @DisplayName("DELETE /v1/swift-codes/{swift-code} - Should return 500 Internal Server Error when service throws unexpected exception")
    void deleteSwiftCode_whenServiceThrowsError_shouldReturnInternalServerError() throws Exception {
        String swiftCode = "ERRORCODE";
        String errorMessage = "Unexpected service failure!";
        given(swiftCodeApiService.deleteSwiftCode(swiftCode))
                .willThrow(new RuntimeException(errorMessage));

        ResultActions response = mockMvc.perform(delete("/v1/swift-codes/{swift-code}", swiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("An internal server error occurred. Please try again later.")));

        verify(swiftCodeApiService).deleteSwiftCode(swiftCode);
    }


    @Test
    @DisplayName("DELETE /v1/swift-codes/{swift-code} - Should handle case insensitivity in path variable")
    void deleteSwiftCode_shouldHandleCaseInsensitivity() throws Exception {
        String swiftCodeLower = "aaisaltrxxx";
        String swiftCodeUpper = "AAISALTRXXX";
        String expectedMessage = "SWIFT code '" + swiftCodeUpper + "' deleted successfully.";
        MessageResponse successResponse = new MessageResponse(expectedMessage);

        given(swiftCodeApiService.deleteSwiftCode(swiftCodeLower)).willReturn(successResponse);

        ResultActions response = mockMvc.perform(delete("/v1/swift-codes/{swift-code}", swiftCodeLower)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is(expectedMessage)));

        verify(swiftCodeApiService).deleteSwiftCode(swiftCodeLower);
    }

    @Test
    @DisplayName("DELETE /v1/swift-codes/{swift-code} - Should return 404 Not Found for invalid format variable if service throws NotFound")
    void deleteSwiftCode_whenPathVarIsInvalidFormat_shouldReturnNotFound() throws Exception {
        String invalidFormatSwiftCode = "TOOLONGCODE";
        String expectedErrorMessage = "SWIFT code '" + invalidFormatSwiftCode + "' not found, cannot delete.";
        given(swiftCodeApiService.deleteSwiftCode(invalidFormatSwiftCode))
                .willThrow(new ResourceNotFoundException(expectedErrorMessage));

        ResultActions response = mockMvc.perform(delete("/v1/swift-codes/{swift-code}", invalidFormatSwiftCode)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is(expectedErrorMessage)));


        verify(swiftCodeApiService).deleteSwiftCode(invalidFormatSwiftCode);
    }

    @Test
    @DisplayName("DELETE /v1/swift-codes-wrong/{code} - Should return 404 Not Found for wrong base path")
    void deleteSwiftCode_whenBasePathIsWrong_shouldReturnNotFound() throws Exception {
        String swiftCode = "AAISALTRXXX";
        String expectedPath = "/v1/swift-codes-wrong/" + swiftCode;

        ResultActions response = mockMvc.perform(delete(expectedPath)
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("The requested resource path '" + expectedPath + "' could not be found")));
        verify(swiftCodeApiService, never()).deleteSwiftCode(anyString());
    }

    @Test
    @DisplayName("DELETE /v1/swift-codes/ - Should return 404 Not Found when path variable is missing")
    void deleteSwiftCode_whenPathVariableIsMissing_shouldReturnNotFound() throws Exception {
        String expectedPath = "/v1/swift-codes/";
        ResultActions response = mockMvc.perform(delete(expectedPath)
                .contentType(MediaType.APPLICATION_JSON));
        response.andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", containsString("The requested resource path '" + expectedPath + "' could not be found")));

        verify(swiftCodeApiService, never()).deleteSwiftCode(anyString());
    }
}