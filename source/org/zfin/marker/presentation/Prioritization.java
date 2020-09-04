package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;

@Setter
@Getter
public class Prioritization {

    @JsonView(View.API.class)
    private String id;
    @JsonView(View.API.class)
    private String name;
    @JsonView(View.API.class)
    private boolean newWithThisPaper;
    @JsonView(View.API.class)
    private String phenotypeData;
    @JsonView(View.API.class)
    private Integer associatedDiseases;
    @JsonView(View.API.class)
    private Boolean hasOrthology;
    @JsonView(View.API.class)
    private Integer phenotypeFigures;
    @JsonView(View.API.class)
    private Integer phenotypePublication;
    @JsonView(View.API.class)
    private Integer expressionFigures = 0;
    @JsonView(View.API.class)
    private Integer expressionPublication = 0;
    @JsonView(View.API.class)
    private Integer expressionInSitu = 0;
}
