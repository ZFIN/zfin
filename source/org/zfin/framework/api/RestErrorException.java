package org.zfin.framework.api;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestErrorException extends RuntimeException {

    private RestErrorMessage error;

    public RestErrorException(String message) {
        super();
        error = new RestErrorMessage(message);
    }

    public RestErrorException(RestErrorMessage error) {
        super();
        this.error = error;
    }


}
