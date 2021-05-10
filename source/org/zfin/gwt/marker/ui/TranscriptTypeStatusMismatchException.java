package org.zfin.gwt.marker.ui;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 */
public class TranscriptTypeStatusMismatchException extends Exception implements IsSerializable{

    private List<String> allowableStatuses ;

    public TranscriptTypeStatusMismatchException(){
        super() ;
    }

    public TranscriptTypeStatusMismatchException(TranscriptTypeStatusMismatchException t){
        super() ;
        this.allowableStatuses = t.getAllowableStatuses();
    }

    public TranscriptTypeStatusMismatchException(List<String> statuses) {
        this.allowableStatuses = statuses ;
    }

    public List<String> getAllowableStatuses() {
        return allowableStatuses;
    }
}
