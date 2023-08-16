package org.zfin.framework.api;

import org.zfin.mutant.Fish;

public class FishFiltering extends Filtering<Fish> {


    public FishFiltering() {
        filterFieldMap.put(FieldFilter.ANTIBODY_NAME, fishNameFilter);
        filterFieldMap.put(FieldFilter.ISOTYPE, wildTypeFilter);
    }

    public static FilterFunction<Fish, String> fishNameFilter =
            (fish, value) -> FilterFunction.contains(fish.getName(), value);

    public static FilterFunction<Fish, String> wildTypeFilter =
            (fish, value) -> FilterFunction.contains(String.valueOf(fish.isWildtype()), value);

}
