package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.infrastructure.ZdbID;

@Setter
@Getter
public class DiseaseAnnotationModel implements ZdbID {

    @JsonView(View.API.class)
    private long ID;
    @JsonView(View.API.class)
    private DiseaseAnnotation diseaseAnnotation;
    @JsonView(View.API.class)
    private FishExperiment fishExperiment;

    @Override
    public String getZdbID() {
        return String.valueOf(ID);
    }

    @Override
    public void setZdbID(String zdbID) {

    }
}