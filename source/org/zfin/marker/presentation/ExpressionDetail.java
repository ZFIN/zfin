package org.zfin.marker.presentation;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.Figure;
import org.zfin.framework.api.View;
import org.zfin.marker.Marker;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;

import java.util.Set;

@Setter
@Getter
public class ExpressionDetail {

    private GenericTerm term;
    @JsonView(View.API.class)
    private long id;
    @JsonView(View.API.class)
    private Set<PostComposedEntity> entities;
    @JsonView(View.API.class)
    private String startStage;
    @JsonView(View.API.class)
    private String endStage;
    @JsonView(View.API.class)
    private Publication publication;
    @JsonView(View.API.class)
    private Fish fish;
    @JsonView(View.API.class)
    private Figure figure;
    @JsonView(View.API.class)
    private Marker gene;
    @JsonView(View.API.class)
    private ExpressionAssay assay;
    @JsonView(View.API.class)
    private Experiment experiment;
    @JsonView(View.API.class)
    private Antibody antibody;

    @Override
    public String toString() {
        return id + fish.getDisplayName();
    }
}
