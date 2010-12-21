package org.zfin.gwt.root.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public class ValidationException extends Exception implements IsSerializable{

    public ValidationException(){}

    public ValidationException(String message){
        super(message);
    }


}
