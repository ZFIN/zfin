package org.zfin.framework.api;

import org.zfin.expression.ExperimentCondition;
import org.zfin.mutant.presentation.ChebiFishModelDisplay;
import org.zfin.ontology.GenericTerm;

import java.util.stream.Collectors;

public class ChebiFishModelDisplayFiltering extends Filtering<ChebiFishModelDisplay> {


    public ChebiFishModelDisplayFiltering() {
        filterFieldMap.put(FieldFilter.FISH_NAME, fishFilter);
        filterFieldMap.put(FieldFilter.DISEASE_NAME, diseaseFilter);
        filterFieldMap.put(FieldFilter.FILTER_TERM_NAME, chebiFilter);
        filterFieldMap.put(FieldFilter.CONDITION_NAME, conditionFilter);
        filterFieldMap.put(FieldFilter.FILTER_EVIDENCE, evidenceFilter);
        filterFieldMap.put(FieldFilter.FILTER_REF, referenceFilter);
    }

    public static FilterFunction<ChebiFishModelDisplay, String> fishFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getFish().getDisplayName(), value);

    public static FilterFunction<ChebiFishModelDisplay, String> diseaseFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getDisease().getTermName(), value);

    public static FilterFunction<ChebiFishModelDisplay, String> referenceFilter =
        (display, value) -> {
            if (display.getFishModelDisplay().getSinglePublication() == null)
                return false;
            return FilterFunction.contains(display.getFishModelDisplay().getSinglePublication().getShortAuthorList(), value);
        };

    public static FilterFunction<ChebiFishModelDisplay, String> evidenceFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getEvidenceCodes().stream().map(GenericTerm::getAbbreviation).collect(Collectors.joining()), value);

    public static FilterFunction<ChebiFishModelDisplay, String> chebiFilter =
        (display, value) -> FilterFunction.contains(display.getChebi().getTermName(), value);

    public static FilterFunction<ChebiFishModelDisplay, String> conditionFilter =
        (display, value) -> FilterFunction.contains(display.getFishModelDisplay().getExperiment().getExperimentConditions().stream().map(ExperimentCondition::getDisplayName).collect(Collectors.joining()), value);


}
