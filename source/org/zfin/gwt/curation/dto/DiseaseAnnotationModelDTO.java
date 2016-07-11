package org.zfin.gwt.curation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.gwt.root.dto.ExperimentDTO;
import org.zfin.gwt.root.dto.FishDTO;

public class DiseaseAnnotationModelDTO implements IsSerializable {
    private DiseaseAnnotationDTO dat;

    private FishDTO fish;
    private long damoID;

    private ExperimentDTO environment;

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

    public ExperimentDTO getEnvironment() {
        return environment;
    }

    public void setEnvironment(ExperimentDTO environment) {
        this.environment = environment;
    }


}
