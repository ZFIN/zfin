package org.zfin.framework.api;

import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.ontology.GenericTerm;
import org.zfin.sequence.MarkerDBLink;

import java.util.stream.Collectors;

public class ExpressionDisplayFiltering extends Filtering<ExpressionDisplay> {


    public ExpressionDisplayFiltering() {
        filterFieldMap.put(FieldFilter.NAME, expressedGeneFilter);
        filterFieldMap.put(FieldFilter.FILTER_TERM_NAME, structureFilter);
        filterFieldMap.put(FieldFilter.CONDITION_NAME, conditionFilter);
    }

    public static FilterFunction<ExpressionDisplay, String> expressedGeneFilter =
            (expressionDisplay, value) -> FilterFunction.contains(expressionDisplay.getExpressedGene().getAbbreviation(), value);

    public static FilterFunction<ExpressionDisplay, String> structureFilter =
        (expressionDisplay, value) -> FilterFunction.contains(expressionDisplay.getExpressionTerms().stream().map(GenericTerm::getTermName).collect(Collectors.joining()), value);

    public static FilterFunction<ExpressionDisplay, String> conditionFilter =
        (expressionDisplay, value) -> FilterFunction.contains(expressionDisplay.getExperiment().getExperimentConditions().stream().map(ExperimentCondition::getDisplayName).collect(Collectors.joining()), value);


}
