package org.zfin.framework.api;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@JsonPropertyOrder({"errors", "statusCode", "statusName"})
public class RestErrorMessage {

    private int statusCode;
    private String statusName;
    private List<String> errors = new ArrayList<>();

    public RestErrorMessage() {
        setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        setStatusName(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }


    public RestErrorMessage(int statusCode) {
        switch (statusCode) {
            case 404:
                setStatusCode(Response.Status.NOT_FOUND.getStatusCode());
                setStatusName(Response.Status.NOT_FOUND.getReasonPhrase());
                break;
            case 400:
                setStatusCode(Response.Status.BAD_REQUEST.getStatusCode());
                setStatusName(Response.Status.BAD_REQUEST.getReasonPhrase());
                break;
            case 500:
                setStatusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
                setStatusName(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
                break;
        }
    }


    public RestErrorMessage(String errorMessage) {
        this.errors.add(errorMessage);
    }

    public void addErrorMessage(String message) {
        errors.add(message);
    }
}