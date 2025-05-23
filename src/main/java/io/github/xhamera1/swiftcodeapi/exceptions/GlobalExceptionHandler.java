package io.github.xhamera1.swiftcodeapi.exceptions;


import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.util.stream.Collectors;


/**
 * Global exception handler for the REST API using @ControllerAdvice.
 * Catches specified exceptions thrown from any controller and returns
 * standardized ResponseEntity objects with appropriate HTTP status codes
 * and an ErrorResponse body.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles standard JPA EntityNotFoundException, typically thrown by JpaRepository methods like findById.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        ErrorResponse error = new ErrorResponse("The requested resource was not found in the database.");
        log.warn("Handling EntityNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error); // 404
    }

    /**
     * Handles custom ResourceNotFoundException thrown from service layer.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        log.info("Handling ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error); // 404
    }

    /**
     * Handles custom ResourceAlreadyExistsException thrown from service layer.
     */
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        log.warn("Handling ResourceAlreadyExistsException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error); // 409
    }


    /**
     * Handles custom InconsistentSwiftDataException thrown from service layer
     * when input data has logical inconsistencies (e.g., isHeadquarter flag mismatch).
     */
    @ExceptionHandler(InconsistentSwiftDataException.class)
    public ResponseEntity<ErrorResponse> handleInconsistentSwiftDataException(InconsistentSwiftDataException ex) {
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        log.warn("Handling InconsistentSwiftDataException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error); // 400
    }


    /**
     * Handles validation exceptions triggered by @Valid on @RequestBody.
     * Provides a more user-friendly message summarizing validation failures.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("'%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        ErrorResponse error = new ErrorResponse("Validation failed: " + errors);
        log.warn("Handling MethodArgumentNotValidException: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error); // Status 400 Bad Request
    }



    /**
     * Handles exceptions when the request body is missing or cannot be parsed
     * (e.g., malformed JSON, empty body for required object).
     */

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Handling HttpMessageNotReadableException: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Request body is missing or malformed.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);   // 400
    }

    /**
     * Handles exceptions when the client sends a request with an unsupported Content-Type.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Handling HttpMediaTypeNotSupportedException: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse("Unsupported Media Type.");
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(error); // 415
    }

    /**
     * Handles NoHandlerFoundException which occurs when the DispatcherServlet
     * cannot find a handler for a request path (e.g., incorrect URL).
     * Returns 404 Not Found.
     * Requires 'spring.mvc.throw-exception-if-no-handler-found=true' in application.properties.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.warn("Handling NoHandlerFoundException: No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        ErrorResponse error = new ErrorResponse("The requested resource path '" + ex.getRequestURL() + "' could not be found on this server.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    /**
     * Catch-all handler for any other unexpected exceptions.
     * Logs the full error stack trace for debugging and returns a generic 500 error to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred processing the request", ex);
        ErrorResponse error = new ErrorResponse("An internal server error occurred. Please try again later.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error); // 500
    }


}
