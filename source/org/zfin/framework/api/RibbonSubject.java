package org.zfin.framework.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RibbonSubject {

    private String id;
    private String label;
    @JsonProperty("nb_classes") private int numberOfClasses;
    @JsonProperty("nb_annotations") private int numberOfAnnotations;
    private Map<String, Map<String, RibbonSubjectGroupCounts>> groups;

}
