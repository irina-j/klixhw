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

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(sourceSystemName);
    }
}
