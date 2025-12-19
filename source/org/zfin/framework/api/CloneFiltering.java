package org.zfin.framework.api;

import org.zfin.marker.Clone;

public class CloneFiltering extends Filtering<Clone> {

    public CloneFiltering() {
        filterFieldMap.put(FieldFilter.TYPE, typeFilter);
        filterFieldMap.put(FieldFilter.PROBE, probeFilter);
    }

    public static FilterFunction<Clone, String> typeFilter =
            (clone, value) -> {
                if (clone.getType() != null)
                    return FilterFunction.fullMatchMultiValueOR(clone.getType().name(), value);
                return false;
            };

    public static FilterFunction<Clone, String> probeFilter =
            (clone, value) -> {
                if (clone.getName() != null)
                    return FilterFunction.fullMatchMultiValueOR(clone.getName(), value);
                return false;
            };
}
