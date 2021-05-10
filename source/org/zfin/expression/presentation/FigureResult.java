package org.zfin.expression.presentation;

import lombok.Getter;
import lombok.Setter;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.mutant.Fish;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.publication.Publication;

import java.util.Collection;

@Setter
@Getter
public class FigureResult extends ExpressionSearchResult {

    private Publication publication;
    private Figure figure;
    private Collection<Fish> fish;
    private Collection<PostComposedEntity> anatomy;
    private Collection<Experiment> experiments;
}
