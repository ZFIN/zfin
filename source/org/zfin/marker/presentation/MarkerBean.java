package org.zfin.marker.presentation;

import org.zfin.audit.AuditLogItem;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.presentation.DiseaseDisplay;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

import java.util.List;
import java.util.Set;

/**
 */
public class MarkerBean extends PaginationBean {
    protected Marker marker ;
    protected String markerTypeDisplay ;
    private String zdbID;
    private MarkerExpression markerExpression;
    private RelatedMarkerDisplay markerRelationships ;
    private List<MarkerRelationshipPresentation> markerRelationshipPresentationList ;
    protected int numPubs;
    private SummaryDBLinkDisplay proteinProductDBLinkDisplay;
    private MappedMarkerBean mappedMarkerBean;
    private SequenceInfo sequenceInfo;
    private List<LinkDisplay> otherMarkerPages;
    private RelatedTranscriptDisplay relatedTranscriptDisplay;
    private OrthologyPresentationBean orthologyPresentationBean;
    private Set<Marker> antibodies ; // TODO: replace with presentation object?
    private Set<Marker> constructs ; // TODO: replace with presentation object?
    private MutantOnMarkerBean mutantOnMarkerBeans ; // TODO: replace with presentation object?
    private PhenotypeOnMarkerBean phenotypeOnMarkerBeans; // TODO: replace with presentation object?
    private GeneOntologyOnMarkerBean geneOntologyOnMarkerBeans ; // TODO: replace with presentation object?
    private boolean hasMarkerHistory;
    private List<PreviousNameLight> previousNames;
    private List<GeneProductsBean> geneProductsBean;

    public List<MarkerRelationshipPresentation> getRelatedMarkers() {
        return relatedMarkers;
    }

    public void setRelatedMarkers(List<MarkerRelationshipPresentation> relatedMarkers) {
        this.relatedMarkers = relatedMarkers;
    }

    private List<MarkerRelationshipPresentation> relatedAntibodies;
    private List<MarkerRelationshipPresentation> relatedMarkers;

    public List<MarkerRelationshipPresentation> getRelatedInteractions() {
        return relatedInteractions;
    }

    public void setRelatedInteractions(List<MarkerRelationshipPresentation> relatedInteractions) {
        this.relatedInteractions = relatedInteractions;
    }

    private List<MarkerRelationshipPresentation> relatedInteractions;
    private int numberOfConstructs;
    private List<DiseaseDisplay> diseaseDisplays;
    private GenericTerm zfinSoTerm;

    public String getMarkerTypeDisplay() {
        return markerTypeDisplay;
    }

    public void setMarkerTypeDisplay(String markerTypeDisplay) {
        this.markerTypeDisplay = markerTypeDisplay;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker ;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Person getUser() {
        return ProfileService.getCurrentSecurityUser();
    }

    public MarkerExpression getMarkerExpression() {
        return markerExpression;
    }

    public GenericTerm getZfinSoTerm() {
        return zfinSoTerm;
    }

    public void setZfinSoTerm(GenericTerm zfinSoTerm) {
        this.zfinSoTerm = zfinSoTerm;
    }

    public void setMarkerExpression(MarkerExpression markerExpression) {
        this.markerExpression = markerExpression;
    }

    public RelatedMarkerDisplay getMarkerRelationships() {
        return markerRelationships;
    }

    public void setMarkerRelationships(RelatedMarkerDisplay markerRelationships) {
        this.markerRelationships = markerRelationships;
    }

    public List<MarkerRelationshipPresentation> getMarkerRelationshipPresentationList() {
        return markerRelationshipPresentationList;
    }

    public void setMarkerRelationshipPresentationList(List<MarkerRelationshipPresentation> markerRelationshipPresentationList) {
        this.markerRelationshipPresentationList = markerRelationshipPresentationList;
    }

    public int getNumPubs() {
        return numPubs;
    }

    public void setNumPubs(int numPubs) {
        this.numPubs = numPubs;
    }


    public SummaryDBLinkDisplay getProteinProductDBLinkDisplay() {
        return proteinProductDBLinkDisplay;
    }

    public void setProteinProductDBLinkDisplay(SummaryDBLinkDisplay proteinProductDBLinkDisplay) {
        this.proteinProductDBLinkDisplay = proteinProductDBLinkDisplay;
    }


    public MappedMarkerBean getMappedMarkerBean() {
        return mappedMarkerBean;
    }

    public void setMappedMarkerBean(MappedMarkerBean mappedMarkerBean) {
        this.mappedMarkerBean = mappedMarkerBean;
    }

    public SequenceInfo getSequenceInfo() {
        return sequenceInfo;
    }

    public void setSequenceInfo(SequenceInfo sequenceInfo) {
        this.sequenceInfo = sequenceInfo;
    }

    public List<LinkDisplay> getOtherMarkerPages() {
        return otherMarkerPages;
    }

    public void setOtherMarkerPages(List<LinkDisplay> otherMarkerPages) {
        this.otherMarkerPages = otherMarkerPages;
    }

    public RelatedTranscriptDisplay getRelatedTranscriptDisplay() {
        return relatedTranscriptDisplay;
    }

    public void setRelatedTranscriptDisplay(RelatedTranscriptDisplay relatedTranscriptDisplay) {
        this.relatedTranscriptDisplay = relatedTranscriptDisplay;
    }

    public Set<Marker> getAntibodies() {
        return antibodies;
    }

    public void setAntibodies(Set<Marker> antibodies) {
        this.antibodies = antibodies;
    }

    public Set<Marker> getConstructs() {
        return constructs;
    }

    public void setConstructs(Set<Marker> constructs) {
        this.constructs = constructs;
    }

    public OrthologyPresentationBean getOrthologyPresentationBean() {
        return orthologyPresentationBean;
    }

    public void setOrthologyPresentationBean(OrthologyPresentationBean orthologyPresentationBean) {
        this.orthologyPresentationBean = orthologyPresentationBean;
    }

    public GeneOntologyOnMarkerBean getGeneOntologyOnMarkerBeans() {
        return geneOntologyOnMarkerBeans;
    }

    public void setGeneOntologyOnMarkerBeans(GeneOntologyOnMarkerBean geneOntologyOnMarkerBeans) {
        this.geneOntologyOnMarkerBeans = geneOntologyOnMarkerBeans;
    }

    public PhenotypeOnMarkerBean getPhenotypeOnMarkerBeans() {
        return phenotypeOnMarkerBeans;
    }

    public void setPhenotypeOnMarkerBeans(PhenotypeOnMarkerBean phenotypeOnMarkerBeans) {
        this.phenotypeOnMarkerBeans = phenotypeOnMarkerBeans;
    }

    public MutantOnMarkerBean getMutantOnMarkerBeans() {
        return mutantOnMarkerBeans;
    }

    public void setMutantOnMarkerBeans(MutantOnMarkerBean mutantOnMarkerBeans) {
        this.mutantOnMarkerBeans = mutantOnMarkerBeans;
    }

    public boolean isHasMarkerHistory() {
        return hasMarkerHistory;
    }

    public void setHasMarkerHistory(boolean hasMarkerHistory) {
        this.hasMarkerHistory = hasMarkerHistory;
    }

    public void setPreviousNames(List<PreviousNameLight> previousNames) {
        this.previousNames = previousNames;
    }

    public List<PreviousNameLight> getPreviousNames() {
        return previousNames;
    }

    public List<MarkerRelationshipPresentation> getRelatedAntibodies() {
        return relatedAntibodies;
    }

    public void setRelatedAntibodies(List<MarkerRelationshipPresentation> relatedAntibodies) {
        this.relatedAntibodies = relatedAntibodies;
    }

    public List<GeneProductsBean> getGeneProductsBean() {
        return geneProductsBean;
    }

    public void setGeneProductsBean(List<GeneProductsBean> geneProductsBean) {
        this.geneProductsBean = geneProductsBean;
    }

    public void setNumberOfConstructs(int numberOfConstructs) {
        this.numberOfConstructs = numberOfConstructs;
    }

    public int getNumberOfConstructs() {
        return numberOfConstructs;
    }

    public List<DiseaseDisplay> getDiseaseDisplays() {
        return diseaseDisplays;
    }

    public void setDiseaseDisplays(List<DiseaseDisplay> diseaseDisplays) {
        this.diseaseDisplays = diseaseDisplays;
    }
}
