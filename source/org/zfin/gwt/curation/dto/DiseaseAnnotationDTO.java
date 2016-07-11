package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.*;

import java.util.List;

public class DiseaseAnnotationDTO implements IsSerializable {
    private String zdbID;

    private FishDTO fish;
    private List<DiseaseAnnotationModelDTO> damoDTO;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public List<DiseaseAnnotationModelDTO> getDamoDTO() {
        return damoDTO;
    }

    public void setDamoDTO(List<DiseaseAnnotationModelDTO> damoDTO) {
        this.damoDTO = damoDTO;
    }

    private TermDTO disease;
    private ExperimentDTO environment;
    private PublicationDTO publication;
    private String evidenceCode;


    public FishDTO getFish() {
        return fish;
    }

    public void setFish(FishDTO fish) {
        this.fish = fish;
    }

    public ExperimentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(ExperimentDTO environment) {
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
