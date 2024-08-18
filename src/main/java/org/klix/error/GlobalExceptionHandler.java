package org.klix.error;

import jakarta.servlet.http.HttpServletRequest;
import org.klix.error.exception.KlixExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Map<ErrorType, HttpStatus> HTTP_CODE_BY_PREDEFINED_ERROR = Map.of(
            PredefinedErrors.INVALID_REQUEST, HttpStatus.BAD_REQUEST,
            PredefinedErrors.INVALID_PARAMETER, HttpStatus.BAD_REQUEST,
            PredefinedErrors.FORBIDDEN_RESOURCE, HttpStatus.FORBIDDEN,
            PredefinedErrors.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR
    );

    @ExceptionHandler(value = KlixExecutionException.class)
    public ResponseEntity<ErrorResponse> handelExecutionException(final HttpServletRequest httpServletRequest,
                                                                  final KlixExecutionException klixExecutionException) {
        HttpStatus httpStatus = HTTP_CODE_BY_PREDEFINED_ERROR.getOrDefault(klixExecutionException.getErrorType(), HttpStatus.INTERNAL_SERVER_ERROR);
        klixExecutionException.logError(httpServletRequest);
        return new ResponseEntity<>(buildErrorResponse(klixExecutionException, httpStatus), httpStatus);
    }

    ErrorResponse buildErrorResponse(KlixExecutionException klixExecutionException, HttpStatus httpStatus) {
        return ErrorResponse.builder(klixExecutionException, httpStatus, klixExecutionException.getBusinessMessage()).build();
    }

}
