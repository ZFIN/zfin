package org.zfin.marker.presentation;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class SequenceTargetingReagentAddBean {

    private String publicationID;
    private String name;
    private String publicNote;
    private String alias;
    private String curatorNote;
    private String strType;
    private String targetGeneSymbol;
    private List<String> targetGeneSymbols;
    private String sequence;
    private String sequence2;
    private String reportedSequence;
    private String reportedSequence2;
    private boolean reversed;
    private boolean reversed2;
    private boolean complemented;
    private boolean complemented2;
    private String supplier;
    private Map<String, String> strTypes;

}



