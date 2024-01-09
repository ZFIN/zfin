package org.zfin.marker.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.marker.TranscriptTypeStatusDefinition;
import org.zfin.mutant.Genotype;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.Sequence;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

@Setter
@Getter
public class TranscriptBean extends MarkerBean {


    private Set<RelatedMarker> relatedGenes;
    private List<RelatedTranscriptDisplay> relatedTranscriptDisplayList;
    private Set<RelatedMarker> microRNARelatedTranscripts;
    private RelatedTranscriptDisplay relatedTranscriptDisplay;
    private SummaryDBLinkDisplay summaryDBLinkDisplay;
    private TranscriptTargets transcriptTargets;
    private SortedSet<Genotype> nonReferenceStrains;
    private List<Sequence> nucleotideSequences;
    private List<Sequence> proteinSequences;
    private List<TranscriptTypeStatusDefinition> transcriptTypeStatusDefinitionList;
    private List<TranscriptType> transcriptTypeList;
    private String vegaID;
    private List<DBLink> unableToFindDBLinks;
    private Genotype strain; // genotype strain pulled from the clone library
    private List<LinkDisplay> rnaCentralLink;


    public Transcript getTranscript() {
        return (Transcript) marker;
    }

    public void setTranscript(Transcript transcript) {
        this.marker = transcript;
    }

    public String toString() {
        String returnString = "";

        returnString += numPubs;
        returnString += "asdfadfasF";


        return returnString;
    }

}
