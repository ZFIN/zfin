package org.zfin.expression.repository;

import org.zfin.expression.ExpressionStageAnatomyContainer;
import org.zfin.marker.Gene;

public interface ExpressionSummaryRepository {
    ExpressionStageAnatomyContainer getExpressionStages(Gene gene);

    Gene getGene(Gene gene);
}
