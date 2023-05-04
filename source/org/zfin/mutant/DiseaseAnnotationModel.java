package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

@Setter
@Getter
public class DiseaseAnnotationModel {

    @JsonView(View.API.class)
    private long ID;
    @JsonView(View.API.class)
    private DiseaseAnnotation diseaseAnnotation;
    @JsonView(View.API.class)
    private FishExperiment fishExperiment;

}