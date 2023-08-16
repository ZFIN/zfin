package org.zfin.framework.api;

import org.apache.commons.lang3.StringUtils;
import org.zfin.antibody.Antibody;
import org.zfin.marker.presentation.STRTargetRow;

import java.util.stream.Collectors;

public class STRTargetRowFiltering extends Filtering<STRTargetRow> {


    public STRTargetRowFiltering() {
        filterFieldMap.put(FieldFilter.STR_TYPE, typeFilter);
    }

    public static FilterFunction<STRTargetRow, String> typeFilter =
        (str, value) -> FilterFunction.contains(str.getStr().getType().name(), value);

}
