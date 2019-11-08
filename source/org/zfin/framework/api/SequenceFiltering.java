package org.zfin.framework.api;

import org.zfin.sequence.MarkerDBLink;

public class SequenceFiltering extends Filtering<MarkerDBLink> {


    public SequenceFiltering() {
        filterFieldMap.put(FieldFilter.SEQUENCE_ACCESSION, accessionFilter);
    }

    public static FilterFunction<MarkerDBLink, String> accessionFilter =
            (dbLink, value) -> FilterFunction.contains(dbLink.getAccessionNumberDisplay(), value);

    //public static Map<FieldFilter, FilterFunction<MarkerDBLink, String>> filterFieldMap = new HashMap<>();


}
