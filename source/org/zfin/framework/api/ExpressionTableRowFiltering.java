package org.zfin.framework.api;

import org.zfin.figure.presentation.ExpressionTableRow;

public class ExpressionTableRowFiltering extends Filtering<ExpressionTableRow> {


    public ExpressionTableRowFiltering() {
        filterFieldMap.put(FieldFilter.GENE_ABBREVIATION, geneFilter);
    }

    public static FilterFunction<ExpressionTableRow, String> geneFilter =
            (gene, value) -> FilterFunction.contains(gene.getGene().getAbbreviation(), value);


}
