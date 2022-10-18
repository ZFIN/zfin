package org.zfin.figure.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.expression.Experiment;
import org.zfin.framework.api.View;
import org.zfin.mutant.Fish;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.PostComposedEntity;

import java.util.List;

@Setter
@Getter
public class FigurePhenotypeSummary {

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

    public boolean isNotEmpty() {
        return startStage != null;
    }
}
