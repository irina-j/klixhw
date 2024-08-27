package org.klix.client.dto.solidbank;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.NoSuchElementException;

public enum MaritalStatus {

    SINGLE("SINGLE"),
    MARRIED("MARRIED"),
    DIVORCED("DIVORCED"),
    COHABITING("COHABITING");

    private final String maritalStatus;

    MaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public static MaritalStatus getValue(String inputStatus) {
        if (inputStatus == null) {
            return null;
        }
        for (MaritalStatus status : MaritalStatus.values()) {
            if (String.valueOf(status.maritalStatus).equalsIgnoreCase(inputStatus)) {
                return status;
            }
        }
        throw new NoSuchElementException("Unsupported marital status: " + inputStatus);
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(maritalStatus);
    }
}
