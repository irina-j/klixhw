package org.klix.error;

public enum PredefinedErrors implements ErrorType {

    INVALID_PARAMETER("A parameter is not valid"),
    INTERNAL_SERVER_ERROR("Generic Technical Error"),
    FORBIDDEN_RESOURCE("You don't have permission to access the resource"),
    INVALID_REQUEST("The request was invalid");

    private final String message;

    PredefinedErrors(String message) {
        this.message = message;
    }

    public String toString() {
        return this.getCode() + ": " + this.message;
    }

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getMessage() {
        return message;
    }

}
