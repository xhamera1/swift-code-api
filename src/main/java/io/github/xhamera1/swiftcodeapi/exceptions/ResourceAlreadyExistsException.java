package io.github.xhamera1.swiftcodeapi.exceptions;


/**
 * Unchecked exception thrown when attempting to create a resource
 * that already exists (e.g., due to a unique constraint violation like duplicate SWIFT code).
 */
public class ResourceAlreadyExistsException extends RuntimeException{

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

}
