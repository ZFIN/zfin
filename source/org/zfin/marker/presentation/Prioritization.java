package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.presentation.MarkerExpression;
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
    private String expressionData;
    @JsonView(View.API.class)
    private String phenotypeData;
    @JsonView(View.API.class)
    private Integer associatedDiseases;
    @JsonView(View.API.class)
    private Boolean hasOrthology;

    private PhenotypeOnMarkerBean phenoOnMarker;
    private MarkerExpression markerExpression;
}
