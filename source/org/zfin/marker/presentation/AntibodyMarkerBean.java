package org.zfin.marker.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.ExternalNote;
import org.zfin.anatomy.presentation.AnatomyLabel;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionSummaryCriteria;
import org.zfin.marker.MarkerRelationship;
import org.zfin.profile.MarkerSupplier;

import java.util.List;
import java.util.Set;

@Setter
@Getter
public class AntibodyMarkerBean extends MarkerBean {

    //    private String antibodyNewPubZdbID;
    private List<ExternalNote> externalNotes ;
    private List<AnatomyLabel> antibodyDetailedLabelings;
    private List<MarkerSupplier> suppliers ;
    protected ExpressionSummaryCriteria expressionSummaryCriteria;
    private Set<MarkerRelationship> sortedAntigenRelationships;
    private Set<String> distinctAssayNames;
    private int numberOfPublications ;
    private int numberOfDistinctComposedTerms ;
    private List<MarkerRelationshipPresentation> antigenGenes;
    private String abRegistryID;
    private List<String> abRegistryIDs;
    private Antibody antibody;

    public String getDeleteURL() {
        String zdbID = marker.getZdbID();
        return "/action/infrastructure/deleteRecord/" + zdbID;
    }

    public String getMergeURL() {
        String zdbID = marker.getZdbID();
        return "/action/marker/merge?zdbIDToDelete=" + zdbID;
    }

}