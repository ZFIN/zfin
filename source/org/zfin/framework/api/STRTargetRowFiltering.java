package org.zfin.framework.api;

import org.apache.commons.lang3.StringUtils;
import org.zfin.antibody.Antibody;
import org.zfin.marker.presentation.STRTargetRow;

import java.util.stream.Collectors;

public class STRTargetRowFiltering extends Filtering<STRTargetRow> {


    public STRTargetRowFiltering() {
        filterFieldMap.put(FieldFilter.STR_TYPE, typeFilter);
        filterFieldMap.put(FieldFilter.STR_NAME, reagentFilter);
        filterFieldMap.put(FieldFilter.TARGET_GENE, targetGeneFilter);
    }

    public static FilterFunction<STRTargetRow, String> typeFilter =
        (str, value) -> FilterFunction.fullMatchMultiValueOR(str.getStr().getType().name(), value);

    public static FilterFunction<STRTargetRow, String> reagentFilter =
        (str, value) -> FilterFunction.fullMatchMultiValueOR(str.getStr().getAbbreviation(), value);

    public static FilterFunction<STRTargetRow, String> targetGeneFilter =
        (str, value) -> FilterFunction.fullMatchMultiValueOR(str.getTarget().getAbbreviation(), value);

}
