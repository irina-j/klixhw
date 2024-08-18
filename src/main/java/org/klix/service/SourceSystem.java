package org.klix.service;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.NoSuchElementException;

public enum SourceSystem {

    FAST_BANK("Fast Bank"),
    SOLID_BANK("Solid Bank");

    private String sourceSystemName;


    SourceSystem(String sourceSystemName) {
        this.sourceSystemName = sourceSystemName;
    }

    public static SourceSystem getValue(String text) {
        if (text == null) {
            return null;
        }
        for (SourceSystem b : SourceSystem.values()) {
            if (String.valueOf(b.sourceSystemName).equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new NoSuchElementException("No such source system: " + text);
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(sourceSystemName);
    }
}
