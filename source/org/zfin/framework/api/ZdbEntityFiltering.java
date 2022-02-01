package org.zfin.framework.api;

import org.zfin.infrastructure.ZdbEntity;

public class ZdbEntityFiltering extends Filtering<ZdbEntity> {


    public ZdbEntityFiltering() {
        filterFieldMap.put(FieldFilter.ZDB_ENTITY_TYPE, zdbIdTypeFilter);
    }

    public static FilterFunction<ZdbEntity, String> zdbIdTypeFilter =
            (entity, value) -> FilterFunction.contains(entity.getZdbType(), value);

}
