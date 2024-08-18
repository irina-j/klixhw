package org.klix.error.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.klix.error.ErrorDetail;
import org.klix.error.ErrorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public abstract class KlixBaseException extends RuntimeException {

    private static final Logger LOGGER = LoggerFactory.getLogger(KlixBaseException.class);

    private enum MdcKeys {
        ERROR_ID("ERROR_ID"),
        ERROR_TYPE("ERROR_TYPE"),
        ERROR_DETAIL("ERROR_DETAIL"),
        BUSINESS_MESSAGE("BUSINESS_MESSAGE"),
        REQUEST_INFO("REQUEST_INFO"),
        TIMESTAMP("TIMESTAMP"),
        CAUSE("CAUSE"),
        ERRORS("ERRORS");

        private final String key;

        MdcKeys(final String key) {
            this.key = key;
        }

    }

    private String errorId;
    private String timestamp;
    private List<ErrorDetail> errors;
    private ErrorType errorType;
    private String detail;
    private String businessMessage;

    /**
     * Base class that can hold all the fields of the error payload
     *
     * @param errorId
     * @param errorType       Error code and message reported back to the calling application.
     * @param detail          Detail of the exception reported back to the calling application.
     * @param businessMessage The specific business error message.
     * @param errors
     * @param timestamp
     */
    public KlixBaseException(String errorId,
                             ErrorType errorType,
                             String detail,
                             String businessMessage,
                             Throwable cause,
                             List<ErrorDetail> errors,
                             String timestamp) {
        super(errorType.getMessage(), cause);
        this.errorType = errorType;
        this.detail = detail;
        this.businessMessage = businessMessage;
        this.errorId = errorId;
        this.timestamp = timestamp;
        this.errors = errors;
        logTrace();
    }

    private void logTrace() {
        loadMdcWithException(null);
        LOGGER.error("An Klix Exception has been created: ", this);
        clearMdc();
    }

    public void logError(final HttpServletRequest httpServletRequest) {
        loadMdcWithException(httpServletRequest);
        LOGGER.error("An Klix Exception has been triggered: ", this);
        clearMdc();
    }

    /**
     * Update the MDC prior to send to the LOGGER
     */
    private void loadMdcWithException(HttpServletRequest httpServletRequest) {
        if (httpServletRequest != null) {
            putIntoMdc(MdcKeys.REQUEST_INFO, getRequestInfo(httpServletRequest));
        }
        putIntoMdc(MdcKeys.ERROR_ID, errorId);
        putIntoMdc(MdcKeys.ERROR_TYPE, errorType.getCode());
        putIntoMdc(MdcKeys.ERROR_DETAIL, detail);
        putIntoMdc(MdcKeys.BUSINESS_MESSAGE, businessMessage);
        putIntoMdc(MdcKeys.TIMESTAMP, timestamp);
        putIntoMdc(MdcKeys.CAUSE, getCause() != null ? getCause().toString() : "N/A");
        putIntoMdc(MdcKeys.ERRORS, errors != null ? errors.toString() : "[]");
    }

    private static void putIntoMdc(MdcKeys key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            MDC.put(key.key, value);
        }
    }

    private static void clearMdc() {
        Arrays.stream(MdcKeys.values())
                .map(key -> key.key)
                .forEach(MDC::remove);
    }

    private static String getRequestInfo(HttpServletRequest httpServletRequest) {
        return Optional.ofNullable(httpServletRequest)
                .map(request -> {
                    String info = httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI();
                    if (httpServletRequest.getQueryString() != null) {
                        info += "?" + httpServletRequest.getQueryString();
                    }
                    return info;
                })
                .orElse(null);
    }

    public String getCode() {
        return errorType.getCode();
    }

}
