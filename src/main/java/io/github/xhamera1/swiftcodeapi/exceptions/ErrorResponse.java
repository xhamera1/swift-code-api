package io.github.xhamera1.swiftcodeapi.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Simple DTO for returning error messages in API responses.
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private final String message;

}
