package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.expression.presentation.MarkerExpression;

@Setter
@Getter
public class Prioritization {
  //  @JsonView(View.API.class) private Marker marker;
    @JsonView(View.API.class) private String id;
    @JsonView(View.API.class) private String name;
    @JsonView(View.API.class)  private boolean newWithThisPaper;
    @JsonView(View.API.class) private String expressionData;
    @JsonView(View.API.class) private String phenotypeData;
    @JsonView(View.API.class) private PhenotypeOnMarkerBean phenoOnMarker;
    @JsonView(View.API.class) private  MarkerExpression markerExpression;
    @JsonView(View.API.class) private Integer associatedDiseases;
    @JsonView(View.API.class) private Boolean hasOrthology;
}
