package org.zfin.infrastructure.presentation;

import org.zfin.feature.Feature;
import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DeleteRecordBean {

    private String zdbIDToDelete;
    private String recordToDeleteViewString;
    private List<String> errors ;
    private boolean removedFromTracking;

    public String getZdbIDToDelete() {
        return zdbIDToDelete;
    }

    public void setZdbIDToDelete(String zdbIDToDelete) {
        this.zdbIDToDelete = zdbIDToDelete;
    }

    public String getRecordToDeleteViewString() {
        return recordToDeleteViewString;
    }

    public void setRecordToDeleteViewString(String recordToDeleteViewString) {
        this.recordToDeleteViewString = recordToDeleteViewString;
    }

    public void addError(String error){
        if(errors==null){
            errors = new ArrayList<String>();
        }
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DeleteBean");
        sb.append("{zdbIDToDelete='").append(zdbIDToDelete).append('\'');
        sb.append(", recordToDeleteViewString='").append(recordToDeleteViewString).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean isRemovedFromTracking() {
        return removedFromTracking;
    }

    public void setRemovedFromTracking(boolean removedFromTracking) {
        this.removedFromTracking = removedFromTracking;
    }
}
