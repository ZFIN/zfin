package org.zfin.uniprot.diff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UniProtDiffSetSummary {

    private int added;
    private int removed;
    private int changed;
    private int total;

    @JsonProperty("changed RefSeq")
    private int changedRefSeq;

    @JsonProperty("changed ZFIN")
    private int changedZFIN;

    @JsonProperty("changed GeneID")
    private int changedGeneID;

    @JsonProperty("latest update in set 1")
    private String latestUpdateFromSequence1;
    @JsonProperty("latest update in set 2")
    private String latestUpdateFromSequence2;


}
