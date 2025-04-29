package io.github.xhamera1.swiftcodeapi.exceptions;

/**
 * Unchecked exception thrown when data provided for a SWIFT code entry
 * is internally inconsistent (e.g., the isHeadquarter flag contradicts
 * the SWIFT code format). Indicates a client request data error.
 */
public class InconsistentSwiftDataException extends RuntimeException {

    public InconsistentSwiftDataException(String message) {
        super(message);
    }
}