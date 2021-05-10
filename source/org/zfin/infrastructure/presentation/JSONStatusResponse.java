package org.zfin.infrastructure.presentation;

/**
 * Simple class designed to be serialized as JSON and sent back in
 * response to an ajax request.
 */
public class JSONStatusResponse {

    private final String status;
    private final String error;

    public JSONStatusResponse(String status, String error) {
        this.status = status;
        this.error = error;
    }

    public String getStatus() {
        return this.status;
    }

    public String getError() {
        return this.error;
    }

}
