package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.presentation.OrganizationLink;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;

import java.util.List;

@Setter
@Getter
public class FigureExpressionSummary {

    @JsonView(View.API.class)
    private List<Marker> genes;
    @JsonView(View.API.class)
    private List<Marker> antibodies;
    @JsonView(View.API.class)
    private List<Fish> fish;
    @JsonView(View.API.class)
    private List<SequenceTargetingReagent> sequenceTargetingReagents;
    @JsonView(View.API.class)
    private List<Experiment> experiments;
    @JsonView(View.API.class)
    private List<PostComposedEntity> entities;
    @JsonView(View.API.class)
    private DevelopmentStage startStage;
    @JsonView(View.API.class)
    private DevelopmentStage endStage;
    @JsonView(View.API.class)
    private Clone probe;
    private List<OrganizationLink> probeSuppliers;

    public boolean isNotEmpty() {
        return startStage != null;
    }
}
