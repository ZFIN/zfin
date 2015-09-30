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


    private EnvironmentDTO environment;
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
