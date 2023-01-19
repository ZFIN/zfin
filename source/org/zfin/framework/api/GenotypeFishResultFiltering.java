package org.zfin.framework.api;

import org.zfin.expression.ExperimentCondition;
import org.zfin.expression.presentation.ExpressionDisplay;
import org.zfin.marker.Marker;
import org.zfin.mutant.presentation.GenotypeFishResult;
import org.zfin.ontology.GenericTerm;

import java.util.stream.Collectors;

public class GenotypeFishResultFiltering extends Filtering<GenotypeFishResult> {


    public GenotypeFishResultFiltering() {
        filterFieldMap.put(FieldFilter.FISH_NAME, fishFilter);
        filterFieldMap.put(FieldFilter.NAME, geneNameFilter);
    }

    public static FilterFunction<GenotypeFishResult, String> fishFilter =
            (genotypeFishResult, value) -> FilterFunction.contains(genotypeFishResult.getFish().getName(), value);

    public static FilterFunction<GenotypeFishResult, String> geneNameFilter =
        (genotypeFishResult, value) -> FilterFunction.contains(genotypeFishResult.getAffectedMarkers().stream().map(Marker::getAbbreviation).collect(Collectors.joining()), value);


}
