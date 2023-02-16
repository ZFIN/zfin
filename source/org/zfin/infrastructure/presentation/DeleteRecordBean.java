package org.zfin.infrastructure.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.feature.Feature;
import org.zfin.marker.Marker;
import org.zfin.publication.Publication;

import java.util.ArrayList;
import java.util.List;

/**
 */
@Setter
@Getter
public class DeleteRecordBean {

    private String zdbIDToDelete;
    private String recordToDeleteViewString;
    private List<String> errors ;
    private boolean removedFromTracking;
    private Publication publicationCurated;
    private String publicationID;
    private String comment;

    public void addError(String error){
        if(errors==null){
            errors = new ArrayList<String>();
        }
        errors.add(error);
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

}
