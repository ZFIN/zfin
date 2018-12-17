package org.zfin.orthology.presentation;

import org.zfin.gwt.root.dto.ReferenceDatabaseDTO;

public class OrthologExternalReferenceDTO {

    private String accessionNumber;
    private ReferenceDatabaseDTO referenceDatabaseDTO;
    private String symbol;

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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
