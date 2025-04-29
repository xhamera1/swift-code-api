package io.github.xhamera1.swiftcodeapi.exceptions;


/**
 * Unchecked exception thrown when a requested resource (e.g., a SWIFT code)
 * cannot be found in the system.
 */
public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
