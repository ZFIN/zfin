package org.zfin.marker.presentation;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Prioritization {

    private String id;
    private String name;
    private boolean newWithThisPaper;
    private String expressionData;
    private String phenotypeData;
    private Integer associatedDiseases;
    private Boolean hasOrthology;
}
