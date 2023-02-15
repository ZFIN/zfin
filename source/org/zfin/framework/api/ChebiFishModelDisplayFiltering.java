package org.zfin.framework.api;

import org.zfin.expression.ExperimentCondition;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.mutant.presentation.PhenotypeDisplay;

import java.util.stream.Collectors;

public class ChebiFishModelDisplayFiltering extends Filtering<ChebiFishModelDisplay> {


    public ChebiFishModelDisplayFiltering() {
        filterFieldMap.put(FieldFilter.FISH_NAME, fishFilter);
        filterFieldMap.put(FieldFilter.DISEASE_NAME, diseaseFilter);
        filterFieldMap.put(FieldFilter.FILTER_TERM_NAME, chebiFilter);
        filterFieldMap.put(FieldFilter.CONDITION_NAME, conditionFilter);
    }

    public static FilterFunction<ChebiFishModelDisplay, String> fishFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getFish().getDisplayName(), value);

    public static FilterFunction<ChebiFishModelDisplay, String> diseaseFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getDisease().getTermName(), value);

    public static FilterFunction<ChebiFishModelDisplay, String> chebiFilter =
        (display, value) -> FilterFunction.contains(display.getChebi().getTermName(), value);

    public static FilterFunction<ChebiFishModelDisplay, String> conditionFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getExperiment().getExperimentConditions().stream().map(ExperimentCondition::getDisplayName).collect(Collectors.joining()), value);


}
