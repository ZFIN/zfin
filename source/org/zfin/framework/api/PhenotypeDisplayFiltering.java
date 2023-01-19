package org.zfin.framework.api;

import org.zfin.expression.ExperimentCondition;
import org.zfin.mutant.presentation.PhenotypeDisplay;
import org.zfin.ontology.GenericTerm;

import java.util.stream.Collectors;

public class PhenotypeDisplayFiltering extends Filtering<PhenotypeDisplay> {


    public PhenotypeDisplayFiltering() {
        filterFieldMap.put(FieldFilter.PHENOTYPE, phenotypeFilter);
        filterFieldMap.put(FieldFilter.CONDITION_NAME, conditionFilter);
    }

    public static FilterFunction<PhenotypeDisplay, String> phenotypeFilter =
        (phenotypeDisplay, value) -> FilterFunction.contains(phenotypeDisplay.getPhenoStatement().getDisplayName(), value);

    public static FilterFunction<PhenotypeDisplay, String> conditionFilter =
        (phenotypeDisplay, value) -> FilterFunction.contains(phenotypeDisplay.getExperiment().getExperimentConditions().stream().map(ExperimentCondition::getDisplayName).collect(Collectors.joining()), value);


}
