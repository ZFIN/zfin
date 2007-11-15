package org.zfin.expression.repository;

import org.zfin.expression.*;
import org.zfin.marker.Gene;

import java.util.List;

public interface ExpressionSummaryRepository {
    ExpressionStageAnatomyContainer getExpressionStages(Gene gene);
    Gene getGene(Gene gene);
}
