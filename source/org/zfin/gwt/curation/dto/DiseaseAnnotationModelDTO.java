package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.EnvironmentDTO;
import org.zfin.gwt.root.dto.FishDTO;
import org.zfin.gwt.root.dto.PublicationDTO;
import org.zfin.gwt.root.dto.TermDTO;
import org.zfin.mutant.DiseaseAnnotation;

public class DiseaseAnnotationModelDTO implements IsSerializable {
    private DiseaseAnnotationDTO dat;

    private FishDTO fish;
    private long damoID;

    private EnvironmentDTO environment;

    public long getDamoID() {
        return damoID;
    }

    public void setDamoID(long damoID) {
        this.damoID = damoID;
    }

    public DiseaseAnnotationDTO getDat() {

        return dat;
    }

    public void setDat(DiseaseAnnotationDTO dat) {
        this.dat = dat;
    }

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


}
