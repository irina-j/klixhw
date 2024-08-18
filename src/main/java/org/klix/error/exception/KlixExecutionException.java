package org.klix.error.exception;

import lombok.NoArgsConstructor;
import org.klix.error.ErrorDetail;
import org.klix.error.ErrorType;
import org.klix.error.PredefinedErrors;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
public class KlixExecutionException extends KlixBaseException {
    /**
     * Base class that can hold all the fields of the error payload
     *
     * @param errorId
     * @param errorType       Error code and message reported back to the calling application.
     * @param detail          Detail of the exception reported back to the calling application.
     * @param businessMessage The specific business error message.
     * @param cause
     * @param errors
     * @param timestamp
     */
    public KlixExecutionException(String errorId, ErrorType errorType, String detail, String businessMessage, Throwable cause, List<ErrorDetail> errors, String timestamp) {
        super(errorId, errorType, detail, businessMessage, cause, errors, timestamp);
    }

    public KlixExecutionException(String errorId, PredefinedErrors internalServerError, String message, String businessMessage, Throwable cause, List<ErrorDetail> errors, String timestamp) {
        super(errorId, internalServerError, message, businessMessage, cause, errors, timestamp);
    }

    public KlixExecutionException(String businessMessage, String message, Throwable cause) {
        super(null, PredefinedErrors.INTERNAL_SERVER_ERROR, message, businessMessage, cause, Collections.emptyList(), null);
    }

    public KlixExecutionException(String errorId, PredefinedErrors internalServerError, String message, Throwable exception) {
        super(errorId, internalServerError, message, null, exception, Collections.emptyList(), null);
    }

}
