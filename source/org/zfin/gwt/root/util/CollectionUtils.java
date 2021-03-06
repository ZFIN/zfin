package org.zfin.gwt.root.util;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.*;

/**
 * CollectionUtils version for GWT.
 */
public class CollectionUtils implements IsSerializable {

    /**
     * Returns a {@link Collection} containing the intersection
     * of the given {@link Collection}s.
     * <p/>
     * The cardinality of each element in the returned {@link Collection}
     * will be equal to the minimum of the cardinality of that element
     * in the two given {@link Collection}s.
     *
     * @param a the first collection, must not be null
     * @param b the second collection, must not be null
     * @return the intersection of the two collections
     * @see Collection#retainAll
     */
    @SuppressWarnings("unchecked")
    public static Collection intersection(final Collection a, final Collection b) {
        ArrayList list = new ArrayList();
        Map mapa = getCardinalityMap(a);
        Map mapb = getCardinalityMap(b);
        Set elts = new HashSet(a);
        elts.addAll(b);
        for (Object obj : elts) {
            for (int i = 0, m = Math.min(getFreq(obj, mapa), getFreq(obj, mapb)); i < m; i++) {
                list.add(obj);
            }
        }
        return list;
    }

    private static int getFreq(final Object obj, final Map freqMap) {
        Integer count = (Integer) freqMap.get(obj);
        if (count != null) {
            return count;
        }
        return 0;
    }

    public static Map getCardinalityMap(final Collection coll) {
        Map count = new HashMap();
        for (Object obj : coll) {
            Integer c = (Integer) (count.get(obj));
            if (c == null) {
                Integer INTEGER_ONE = 1;
                count.put(obj, INTEGER_ONE);
            } else {
                count.put(obj, c + 1);
            }
        }
        return count;
    }

}