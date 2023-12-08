package org.zfin.uniprot.diff;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public record UniProtDiffSetSummary(
        int added,
        int removed,
        int changed,
        int total,
        @JsonProperty("changed RefSeq") int changedRefSeq,
        @JsonProperty("changed ZFIN") int changedZFIN,
        @JsonProperty("changed GeneID") int changedGeneID,
        @JsonProperty("changed InterPro") int changedInterPro,
        @JsonProperty("changed EC") int changedEC,
        @JsonProperty("changed Pfam") int changedPfam,
        @JsonProperty("changed PROSITE") int changedPROSITE,

        @JsonProperty("latest update in set 1") String latestUpdateFromSequence1,
        @JsonProperty("latest update in set 2") String latestUpdateFromSequence2
) {}