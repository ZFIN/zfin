package org.zfin.orthology.presentation;

import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;

public class OrthologExternalReferenceDTO {

    private String accessionNumber;
    private ReferenceDatabaseDTO referenceDatabaseDTO;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public ReferenceDatabaseDTO getReferenceDatabaseDTO() {
        return referenceDatabaseDTO;
    }

    public void setReferenceDatabaseDTO(ReferenceDatabaseDTO referenceDatabaseDTO) {
        this.referenceDatabaseDTO = referenceDatabaseDTO;
    }
}
