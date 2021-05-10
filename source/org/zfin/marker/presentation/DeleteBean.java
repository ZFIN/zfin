package org.zfin.marker.presentation;

import org.zfin.marker.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class DeleteBean {

    private String zdbIDToDelete;
    private Marker markerToDelete;
    private String markerToDeleteViewString;
    private List<String> errors ;

    public String getZdbIDToDelete() {
        return zdbIDToDelete;
    }

    public void setZdbIDToDelete(String zdbIDToDelete) {
        this.zdbIDToDelete = zdbIDToDelete;
    }


    public Marker getMarkerToDelete() {
        return markerToDelete;
    }

    public void setMarkerToDelete(Marker markerToDelete) {
        this.markerToDelete = markerToDelete;
        setMarkerToDeleteViewString(this.markerToDelete.getAbbreviation());
    }

    public String getMarkerToDeleteViewString() {
        return markerToDeleteViewString;
    }

    public void setMarkerToDeleteViewString(String markerToDeleteViewString) {
        this.markerToDeleteViewString = markerToDeleteViewString;
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
        sb.append(", markerToDelete=").append(markerToDelete);
        sb.append(", markerToDeleteViewString='").append(markerToDeleteViewString).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
