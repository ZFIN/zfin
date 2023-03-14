package org.zfin.database.transform;

import org.hibernate.transform.ResultTransformer;

import java.util.List;

/**
 * This transformer returns the first element of the result tuple.
 * This is useful for queries that return a single column.
 */
public class FirstElementResultTransformer implements ResultTransformer {

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        return tuple[0];
    }

    @Override
    public List transformList(List collection) {
        return collection;
    }
}
