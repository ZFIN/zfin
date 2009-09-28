package org.zfin.marker.presentation;

import org.zfin.marker.*;
import org.zfin.sequence.Sequence;
import org.zfin.properties.ZfinProperties;
import org.zfin.audit.repository.AuditLogRepository;
import org.zfin.audit.AuditLogItem;
import org.zfin.repository.RepositoryFactory;
import org.zfin.mutant.Genotype;

import java.util.List;
import java.util.TreeSet;
import java.util.SortedSet;

/**
 */
public class TranscriptBean extends MarkerBean {


    private RelatedMarkerDisplay transcriptRelationships ;
    private TreeSet<RelatedMarker> relatedGenes;
    private List<RelatedTranscriptDisplay> relatedTranscriptDisplayList;
    private TreeSet<RelatedMarker> microRNARelatedTranscripts;
    private RelatedTranscriptDisplay relatedTranscriptDisplay;
    private SequenceInfo sequenceInfo ;
    private SummaryDBLinkDisplay summaryDBLinkDisplay;
    private SummaryDBLinkDisplay proteinProductDBLinkDisplay;
    private TranscriptTargets transcriptTargets;
    private SortedSet<Genotype> nonReferenceStrains;
    private int numPubs ;
    private List<Sequence> nucleotideSequences ;
    private List<Sequence> proteinSequences ;
    private List<TranscriptTypeStatusDefinition> transcriptTypeStatusDefinitionList;
    private List<TranscriptType> transcriptTypeList;
    private String vegaID;


    public RelatedMarkerDisplay getTranscriptRelationships() {
        return transcriptRelationships;
    }

    public void setRelationships(RelatedMarkerDisplay transcriptRelationships) {
        this.transcriptRelationships = transcriptRelationships;
    }

    public TreeSet<RelatedMarker> getRelatedGenes() {
        return relatedGenes;
    }

    public void setRelatedGenes(TreeSet<RelatedMarker> relatedGenes) {
        this.relatedGenes = relatedGenes;
    }

    public List<RelatedTranscriptDisplay> getRelatedTranscriptDisplayList() {
        return relatedTranscriptDisplayList;
    }

    public void setRelatedTranscriptDisplayList(List<RelatedTranscriptDisplay> relatedTranscriptDisplayList) {
        this.relatedTranscriptDisplayList = relatedTranscriptDisplayList;
    }

    public TreeSet<RelatedMarker> getMicroRNARelatedTranscripts() {
        return microRNARelatedTranscripts;
    }

    public void setMicroRNARelatedTranscripts(TreeSet<RelatedMarker> microRNARelatedTranscripts) {
        this.microRNARelatedTranscripts = microRNARelatedTranscripts;
    }

    public SequenceInfo getSequenceInfo() {
        return sequenceInfo;
    }

    public void setSequenceInfo(SequenceInfo sequenceInfo) {
        this.sequenceInfo = sequenceInfo;
    }

    public SummaryDBLinkDisplay getSummaryDBLinkDisplay() {
        return summaryDBLinkDisplay;
    }

    public void setSummaryDBLinkDisplay(SummaryDBLinkDisplay summaryDBLinkDisplay) {
        this.summaryDBLinkDisplay = summaryDBLinkDisplay;
    }

    public SummaryDBLinkDisplay getProteinProductDBLinkDisplay() {
        return proteinProductDBLinkDisplay;
    }

    public void setProteinProductDBLinkDisplay(SummaryDBLinkDisplay proteinProductDBLinkDisplay) {
        this.proteinProductDBLinkDisplay = proteinProductDBLinkDisplay;
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

    public int getNumPubs() {
        return numPubs;
    }

    public void setNumPubs(int numPubs) {
        this.numPubs = numPubs;
    }

    public Transcript getTranscript() {
        return (Transcript) marker ;
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

    public String toString(){
        String returnString = "";

        returnString += numPubs ;
        returnString += "asdfadfasF" ;


        return returnString ;
    }


    public String getDeleteURL() {
        String zdbID = getTranscript().getZdbID();
        return "/" + ZfinProperties.getWebDriver() + "?MIval=aa-delete_record.apg&rtype=marker&OID=" + zdbID;
    }

    public String getEditURL() {
        String zdbID = getTranscript().getZdbID();
        return "/action/marker/marker-edit?zdbID=" + zdbID;
    }

    public AuditLogItem getLatestUpdate() {
        AuditLogRepository alr = RepositoryFactory.getAuditLogRepository();
        return alr.getLatestAuditLogItem(marker.getZdbID());
    }
}
