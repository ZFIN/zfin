package org.zfin.anatomy.presentation;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Sorting of relationships is going to be such that the parent relationship
 * comes first and then the child relationship.
 * <p/>
 * develops from, develops into
 * is part of, has parts
 * is a type of, has subtype
 *
 */
public class RelationshipSorting implements Comparator<String> {

    public static final String HAS_PARTS = "has parts";
    public static final String IS_PART_OF = "is part of";
    public static final String DEVELOPS_INTO = "develops into";
    public static final String DEVELOPS_FROM = "develops from";
    public static final String HAS_SUBTYPE = "has subtype";
    public static final String IS_A_TYPE_OF = "is a type of";
    public static final String START = "start";
    public static final String END = "end";

    public static final HashMap<String, Integer> order = new HashMap<String, Integer>(6);
    {
            order.put(DEVELOPS_FROM, 1);
            order.put(DEVELOPS_INTO, 2);
            order.put(IS_PART_OF, 3);
            order.put(HAS_PARTS, 4);
            order.put(IS_A_TYPE_OF, 5);
            order.put(HAS_SUBTYPE, 6);
    }

    public int compare(String relationTypeOne, String relationTypeTwo) {
        if(!order.containsKey(relationTypeOne) && order.containsKey(relationTypeTwo))
            return +1;
        if(order.containsKey(relationTypeOne) && !order.containsKey(relationTypeTwo))
            return -1;
        if(!order.containsKey(relationTypeOne) && !order.containsKey(relationTypeTwo))
            return relationTypeOne.compareTo(relationTypeTwo);

        Integer one = order.get(relationTypeOne);
        Integer two = order.get(relationTypeTwo);
        if(one < two)
            return -1;
        if(one > two)
            return +1;
        return 0;
    }
}
