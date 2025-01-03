package org.zfin.expression.repository;

import org.zfin.expression.ExpressionStageAnatomyContainer;
import org.zfin.marker.Gene;

public interface ExpressionSummaryRepository {
    Gene getGene(Gene gene);
}
