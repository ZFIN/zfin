package org.zfin.publication.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.zfin.antibody.Antibody;
import org.zfin.feature.Feature;
import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.figure.presentation.PhenotypeTableRow;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.marker.presentation.STRTargetRow;
import org.zfin.mutant.DiseaseAnnotationModel;
import org.zfin.mutant.Fish;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class PublicationCombinedMetrics {

    @JsonView(View.API.class)
    private Collection<String> directAttributions;

    @JsonView(View.API.class)
    private Collection<PublicationAPIController.ChromosomeLinkage> chromosomeLinkages;

    @JsonView(View.API.class)
    private Collection<GeneBean> orthologs;

    @JsonView(View.API.class)
    private Collection<Antibody> antibodies;

    @JsonView(View.API.class)
    private Collection<Fish> fish;

    @JsonView(View.API.class)
    private Collection<STRTargetRow> strs;

    @JsonView(View.API.class)
    private Collection<DiseaseAnnotationModel> diseases;

    @JsonView(View.API.class)
    private Collection<Feature> features;

    @JsonView(View.API.class)
    private Collection<PhenotypeTableRow> phenotypes;

    @JsonView(View.API.class)
    private Collection<Marker> efgs;

    @JsonView(View.API.class)
    private Collection<String> expressionRowIDs;

    @JsonView(View.API.class)
    private Collection<Marker> markers;

    @JsonView(View.API.class)
    private Map<String, Integer> countsByType = new HashMap<>();

    @JsonView(View.API.class)
    private String publicationID;

    public void setCount(String type, int count) {
        countsByType.put(type, count);
    }

    public void setExpressionRows(Collection<ExpressionTableRow> expressionRows) {
        this.expressionRowIDs = expressionRows.stream()
            .map( etr -> {
                String etrString = "[" +
                        "subterm=" + (etr.getSubterm() == null ? "" : etr.getSubterm().getZdbID()) + "," +
                        "superterm=" + (etr.getSuperterm() == null ? "" : etr.getSuperterm().getZdbID()) + "," +
                        "gene=" + (etr.getGene() == null ? "" : etr.getGene().getZdbID()) + "," +
                        "antibody=" + (etr.getAntibody() == null ? "" : etr.getAntibody().getZdbID()) + "," +
                        "fish=" + (etr.getFish() == null ? "" : etr.getFish().getZdbID()) + "," +
                        "experiment=" + (etr.getExperiment() == null ? "" : etr.getExperiment().getZdbID()) ;
                etrString += "...]";
                return etrString;
            })
            .collect(Collectors.toList());
    }

}
