package org.zfin.marker.agr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HtpIDDTO {

    private String primaryId;
    private List<String> secondaryId;
    private CrossReferenceDTO crossReference;

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public List<String> getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(List<String> secondaryId) {
        this.secondaryId = secondaryId;
    }

    public CrossReferenceDTO getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(CrossReferenceDTO crossReference) {
        this.crossReference = crossReference;
    }

}
