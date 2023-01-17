package org.zfin.framework.api;

import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.presentation.ProteinExpressionDisplay;
import org.zfin.ontology.GenericTerm;

import java.util.stream.Collectors;

public class ProteinExpressionDisplayFiltering extends Filtering<ProteinExpressionDisplay> {


    public ProteinExpressionDisplayFiltering() {
        filterFieldMap.put(FieldFilter.ANTIBODY_NAME, antibodyFilter);
        filterFieldMap.put(FieldFilter.NAME, antigenGeneFilter);
        filterFieldMap.put(FieldFilter.FILTER_TERM_NAME, structureFilter);
        filterFieldMap.put(FieldFilter.CONDITION_NAME, conditionFilter);
    }

    public static FilterFunction<ProteinExpressionDisplay, String> antibodyFilter =
        (expressionDisplay, value) -> FilterFunction.contains(expressionDisplay.getAntibody().getAbbreviation(), value);

    public static FilterFunction<ProteinExpressionDisplay, String> antigenGeneFilter =
        (expressionDisplay, value) -> {
            if (expressionDisplay.getAntiGene() == null)
                return false;
            return FilterFunction.contains(expressionDisplay.getAntiGene().getAbbreviation(), value);
        };

    public static FilterFunction<ProteinExpressionDisplay, String> structureFilter =
        (expressionDisplay, value) -> FilterFunction.contains(expressionDisplay.getExpressionTerms().stream().map(GenericTerm::getTermName).collect(Collectors.joining()), value);

    public static FilterFunction<ProteinExpressionDisplay, String> conditionFilter =
        (expressionDisplay, value) -> FilterFunction.contains(expressionDisplay.getExperiment().getExperimentConditions().stream().map(ExperimentCondition::getDisplayName).collect(Collectors.joining()), value);


}
