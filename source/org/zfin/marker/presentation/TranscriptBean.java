package org.zfin.marker.presentation;

import org.zfin.marker.Transcript;
import org.zfin.marker.TranscriptType;
import org.zfin.marker.TranscriptTypeStatusDefinition;
import org.zfin.mutant.Genotype;
import org.zfin.sequence.DBLink;
import org.zfin.sequence.Sequence;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 */
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


    public Set<RelatedMarker> getRelatedGenes() {
        return relatedGenes;
    }

    public void setRelatedGenes(Set<RelatedMarker> relatedGenes) {
        this.relatedGenes = relatedGenes;
    }

    public List<RelatedTranscriptDisplay> getRelatedTranscriptDisplayList() {
        return relatedTranscriptDisplayList;
    }

    public void setRelatedTranscriptDisplayList(List<RelatedTranscriptDisplay> relatedTranscriptDisplayList) {
        this.relatedTranscriptDisplayList = relatedTranscriptDisplayList;
    }

    public List<LinkDisplay> getRnaCentralLink() {
        return rnaCentralLink;
    }

    public void setRnaCentralLink(List<LinkDisplay> rnaCentralLink) {
        this.rnaCentralLink = rnaCentralLink;
    }

    public Set<RelatedMarker> getMicroRNARelatedTranscripts() {
        return microRNARelatedTranscripts;
    }

    public void setMicroRNARelatedTranscripts(Set<RelatedMarker> microRNARelatedTranscripts) {
        this.microRNARelatedTranscripts = microRNARelatedTranscripts;
    }

    public SummaryDBLinkDisplay getSummaryDBLinkDisplay() {
        return summaryDBLinkDisplay;
    }

    public void setSummaryDBLinkDisplay(SummaryDBLinkDisplay summaryDBLinkDisplay) {
        this.summaryDBLinkDisplay = summaryDBLinkDisplay;
    }

    public TranscriptTargets getTranscriptTargets() {
        return transcriptTargets;
    }

    public void setTranscriptTargets(TranscriptTargets transcriptTarges) {
        this.transcriptTargets = transcriptTarges;
    }

    public SortedSet<Genotype> getNonReferenceStrains() {
        return nonReferenceStrains;
    }

    public void setNonReferenceStrains(SortedSet<Genotype> nonReferenceStrains) {
        this.nonReferenceStrains = nonReferenceStrains;
    }

    public Transcript getTranscript() {
        return (Transcript) marker;
    }

    public void setTranscript(Transcript transcript) {
        this.marker = transcript;
    }

    public List<Sequence> getNucleotideSequences() {
        return nucleotideSequences;
    }

    public void setNucleotideSequences(List<Sequence> nucleotideSequences) {
        this.nucleotideSequences = nucleotideSequences;
    }

    public List<Sequence> getProteinSequences() {
        return proteinSequences;
    }

    public void setProteinSequences(List<Sequence> proteinSequences) {
        this.proteinSequences = proteinSequences;
    }

    public List<TranscriptTypeStatusDefinition> getTranscriptTypeStatusDefinitionList() {
        return transcriptTypeStatusDefinitionList;
    }

    public void setTranscriptTypeStatusDefinitionList(List<TranscriptTypeStatusDefinition> transcriptStatusList) {
        this.transcriptTypeStatusDefinitionList = transcriptStatusList;
    }

    public RelatedTranscriptDisplay getRelatedTranscriptDisplay() {
        return relatedTranscriptDisplay;
    }

    public void setRelatedTranscriptDisplay(RelatedTranscriptDisplay relatedTranscriptDisplay) {
        this.relatedTranscriptDisplay = relatedTranscriptDisplay;
    }

    public List<TranscriptType> getTranscriptTypeList() {
        return transcriptTypeList;
    }

    public void setTranscriptTypeList(List<TranscriptType> transcriptTypeList) {
        this.transcriptTypeList = transcriptTypeList;
    }

    public String getVegaID() {
        return vegaID;
    }

    public void setVegaID(String vegaID) {
        this.vegaID = vegaID;
    }

    public List<DBLink> getUnableToFindDBLinks() {
        return unableToFindDBLinks;
    }

    public void setUnableToFindDBLinks(List<DBLink> unableToFindDBLinks) {
        this.unableToFindDBLinks = unableToFindDBLinks;
    }

    public Genotype getStrain() {
        return strain;
    }

    public void setStrain(Genotype strain) {
        this.strain = strain;
    }

    public String toString() {
        String returnString = "";

        returnString += numPubs;
        returnString += "asdfadfasF";


        return returnString;
    }

    public String getDeleteURL() {
        return "none";
    }

    public String getEditURL() {
        String zdbID = getTranscript().getZdbID();
        return "/action/marker/marker-edit?zdbID=" + zdbID;
    }
}
