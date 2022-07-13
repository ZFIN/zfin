package org.zfin.alliancegenome;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiException extends RuntimeException {

    @JsonProperty("errorMessage")
    private String message;

    @JsonProperty("errorMessages")
    private String messages;

    @Override
    public String getMessage() {
        return message;
    }

    public String getMessages() {
        return messages;
    }
}
