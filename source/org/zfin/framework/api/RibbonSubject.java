package org.zfin.framework.api;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RibbonSubject {

    private String id;
    private String label;
    private int numberOfClasses;
    private int numberOfAnnotations;
    private Map<String, Map<String, RibbonSubjectGroupCounts>> groups;

}
