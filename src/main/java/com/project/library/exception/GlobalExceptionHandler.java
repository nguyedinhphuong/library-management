package com.project.library.exception;

import com.project.library.dto.response.ResponseData;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MethodValidationException.class,
            ConstraintViolationException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e, WebRequest request) {

        log.warn("handleValidationException");

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        String message = e.getMessage();
        if (e instanceof MethodArgumentNotValidException
                || e instanceof MethodValidationException) {

            int start = message.lastIndexOf("[");
            int end = message.lastIndexOf("]");
            message = message.substring(start + 1, end - 1);
            errorResponse.setError("Payload invalid");

        } else if (e instanceof ConstraintViolationException) {

            message = message.substring(message.indexOf(" ") + 1);
            errorResponse.setError("Path variable invalid");
        }

        errorResponse.setMessage(message);
        return errorResponse;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerErrorException(Exception e, WebRequest request) {

        log.error("MethodArgumentTypeMismatchException", e);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));
        errorResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        errorResponse.setError("Failed to convert value of type");

        return errorResponse;
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseData<?>> handleBusinessException(BusinessException ex) {

        log.warn("BusinessException: {}", ex.getMessage());

        return ResponseEntity.badRequest()
                .body(new ResponseData<>(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request
    ) {

        log.warn("ResourceNotFoundException: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setTimestamp(new Date());
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setError("Resource not found");
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setPath(request.getDescription(false).replace("uri=", ""));

        return errorResponse;
    }

}
