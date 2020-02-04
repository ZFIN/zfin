package org.zfin.orthology.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
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

    @JsonView(View.API.class)
    @JsonProperty("accession")
    public Accession getAccession() {
        Accession accession = new Accession();
        accession.setName(referenceDatabaseDTO.getName() + ":" + accessionNumber);
        accession.setUrl(referenceDatabaseDTO.getUrl() + accessionNumber);
        return accession;
    }

    @Setter
    @Getter
    class Accession {
        @JsonView(View.API.class)
        private String name;
        @JsonView(View.API.class)
        private String url;
    }
}
