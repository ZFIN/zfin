package org.zfin.framework.api;

import org.zfin.figure.presentation.ExpressionTableRow;
import org.zfin.figure.presentation.PhenotypeTableRow;

public class PhenotypeTableRowFiltering extends Filtering<PhenotypeTableRow> {


    public PhenotypeTableRowFiltering() {
        filterFieldMap.put(FieldFilter.PHENOTYPE, phenotypeFilter);
        filterFieldMap.put(FieldFilter.STAGE, stageFilter);
        filterFieldMap.put(FieldFilter.FISH_NAME, fishFilter);
        filterFieldMap.put(FieldFilter.EXPERIMENT, experimentFilter);
    }

    public static FilterFunction<PhenotypeTableRow, String> phenotypeFilter =
            (phenotypeTableRow, value) -> FilterFunction.contains(phenotypeTableRow.getPhenotypeStatement().getDisplayName(), value);

    public static FilterFunction<PhenotypeTableRow, String> fishFilter =
            (phenotypeTableRow, value) -> FilterFunction.contains(phenotypeTableRow.getFish().getDisplayName(), value);

    public static FilterFunction<PhenotypeTableRow, String> experimentFilter =
            (phenotypeTableRow, value) -> FilterFunction.contains(phenotypeTableRow.getExperiment().getDisplayAllConditions(), value);

    public static FilterFunction<PhenotypeTableRow, String> stageFilter =
            (phenotypeTableRow, value) -> FilterFunction.contains(phenotypeTableRow.getStart().getAbbreviation(), value) ||
                FilterFunction.contains(phenotypeTableRow.getEnd().getAbbreviation(), value);


}
