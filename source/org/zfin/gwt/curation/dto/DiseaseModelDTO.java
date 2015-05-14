package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.*;

public class DiseaseModelDTO implements IsSerializable {

    private FishDTO fish;
    private TermDTO disease;
    private EnvironmentDTO environment;
    private PublicationDTO publication;
    private String evidenceCode;

    public FishDTO getFish() {
        return fish;
    }

    public void setFish(FishDTO fish) {
        this.fish = fish;
    }

    public EnvironmentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(EnvironmentDTO environment) {
        this.environment = environment;
    }

    public TermDTO getDisease() {
        return disease;
    }

    public void setDisease(TermDTO disease) {
        this.disease = disease;
    }

    public PublicationDTO getPublication() {
        return publication;
    }

    public void setPublication(PublicationDTO publication) {
        this.publication = publication;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }
}
