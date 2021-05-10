package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RibbonSubjectGroupCounts {

    @JsonProperty("nb_classes")
    private int numberOfClasses;

    @JsonProperty("nb_annotations")
    private int numberOfAnnotations;

}
