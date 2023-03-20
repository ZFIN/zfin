package org.zfin.framework.api;

import org.zfin.figure.presentation.ExpressionTableRow;

public class ExpressionTableRowFiltering extends Filtering<ExpressionTableRow> {


    public ExpressionTableRowFiltering() {
        filterFieldMap.put(FieldFilter.GENE_ABBREVIATION, geneFilter);
        filterFieldMap.put(FieldFilter.ANATOMY, anatomyFilter);
        filterFieldMap.put(FieldFilter.ANTIBODY_NAME, antibodyFilter);
        filterFieldMap.put(FieldFilter.STAGE, stageFilter);
        filterFieldMap.put(FieldFilter.ASSAY, assayFilter);
        filterFieldMap.put(FieldFilter.FISH_NAME, fishFilter);
        filterFieldMap.put(FieldFilter.EXPERIMENT, experimentFilter);
    }

    public static FilterFunction<ExpressionTableRow, String> geneFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getGene().getAbbreviation(), value);

    public static FilterFunction<ExpressionTableRow, String> antibodyFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getAntibody().getName(), value);

    public static FilterFunction<ExpressionTableRow, String> anatomyFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getSuperterm().getTermName(), value);

    public static FilterFunction<ExpressionTableRow, String> assayFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getAssay().getAbbreviation(), value);
    public static FilterFunction<ExpressionTableRow, String> fishFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getFish().getDisplayName(), value);

    public static FilterFunction<ExpressionTableRow, String> experimentFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getExperiment().getDisplayAllConditions(), value);

    public static FilterFunction<ExpressionTableRow, String> stageFilter =
            (expressionTableRow, value) -> FilterFunction.contains(expressionTableRow.getStart().getAbbreviation(), value) ||
                FilterFunction.contains(expressionTableRow.getEnd().getAbbreviation(), value);


}
