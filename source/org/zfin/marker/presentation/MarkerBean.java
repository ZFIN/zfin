package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.antibody.Antibody;
import org.zfin.expression.presentation.MarkerExpression;
import org.zfin.framework.api.View;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.mapping.presentation.MappedMarkerBean;
import org.zfin.marker.AllianceGeneDesc;
import org.zfin.marker.Marker;
import org.zfin.mutant.presentation.DiseaseModelDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.presentation.DiseaseDisplay;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;
import org.zfin.sequence.InterProProtein;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 */
@Setter
@Getter
public class MarkerBean extends PaginationBean {

    @JsonView(View.API.class)
    protected Marker marker;
    protected String markerTypeDisplay;
    private String zdbID;
    private MarkerExpression markerExpression;
    private RelatedMarkerDisplay markerRelationships;
    private List<MarkerRelationshipPresentation> markerRelationshipPresentationList;
    protected int numPubs;
    private SummaryDBLinkDisplay proteinProductDBLinkDisplay;
    private MappedMarkerBean mappedMarkerBean;
    private SequenceInfo sequenceInfo;
    private List<LinkDisplay> otherMarkerPages;
    private RelatedTranscriptDisplay relatedTranscriptDisplay;
    private OrthologyPresentationBean orthologyPresentationBean;
    private Set<Antibody> antibodies;
    private Set<Marker> constructs; // TODO: replace with presentation object?
    private MutantOnMarkerBean mutantOnMarkerBeans; // TODO: replace with presentation object?
    private PhenotypeOnMarkerBean phenotypeOnMarkerBeans; // TODO: replace with presentation object?
    private GeneOntologyOnMarkerBean geneOntologyOnMarkerBeans; // TODO: replace with presentation object?
    private boolean hasMarkerHistory;
    private List<AntibodyMarkerBean> antibodyBeans;
    private List<ConstructBean> constructBeans;
    private List<ProteinDomainBean> proteinDomainBeans;
    private ProteinDetailDomainBean proteinDetailDomainBean;
    private List<InterProProtein> ipProtein;

    private List<String> proteinType;

    private List<PreviousNameLight> previousNames;
    private List<GeneProductsBean> geneProductsBean;
    private Collection<DiseaseModelDisplay> diseaseModelDisplays;
    private AllianceGeneDesc allianceGeneDesc;

    private List<MarkerRelationshipPresentation> relatedAntibodies;
    private List<MarkerRelationshipPresentation> relatedMarkers;

    private List<MarkerRelationshipPresentation> relatedInteractions;
    private int numberOfConstructs;
    private List<DiseaseDisplay> diseaseDisplays;
    private GenericTerm zfinSoTerm;


    public Person getUser() {
        return ProfileService.getCurrentSecurityUser();
    }

}
