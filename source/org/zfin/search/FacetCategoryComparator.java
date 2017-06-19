package org.zfin.search;

import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.search.presentation.FacetValue;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Sort the categories using a predefined order
 */
public class FacetCategoryComparator<T extends FacetValue> implements Comparator<T> {

    private static final Map<Category, String> sortMap;

    static {


        Map<Category, String> map = new HashMap<>();
        Integer i = 1;

        map.put(Category.GENE, pad(i++));
        map.put(Category.EXPRESSIONS, pad(i++));
        map.put(Category.PHENOTYPE, pad(i++));
        map.put(Category.DISEASE, pad(i++));
        map.put(Category.FISH, pad(i++));
        map.put(Category.REPORTER_LINE, pad(i++));
        map.put(Category.MUTANT, pad(i++));
        map.put(Category.CONSTRUCT, pad(i++));
        map.put(Category.SEQUENCE_TARGETING_REAGENT, pad(i++));
        map.put(Category.ANTIBODY, pad(i++));
        map.put(Category.MARKER, pad(i++));
        map.put(Category.FIGURE, pad(i++));
        map.put(Category.ANATOMY, pad(i++));
        map.put(Category.COMMUNITY, pad(i++));
        map.put(Category.PUBLICATION, pad(i));

        sortMap = Collections.unmodifiableMap(map);
    }

    private static String pad(Integer i) {
        return String.format("%04d", i);
    }

    public int compare(T o1, T o2) {
        if (o1 == null && o2 == null)
            return -1;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return +1;

        String s1;
        String s2;

        Category category1 = Category.getCategory(o1.getLabel());
        Category category2 = Category.getCategory(o2.getLabel());
        if (sortMap.containsKey(category1))
            s1 = sortMap.get(category1) + o1.getLabel();
        else
            s1 = o1.getLabel();

        if (sortMap.containsKey(category2))
            s2 = sortMap.get(category2) + o2.getLabel();
        else
            s2 = o2.getLabel();

        return s1.compareTo(s2);
    }

    public static int compareString(String o1, String o2) {
        if (o1 == null && o2 == null)
            return -1;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return +1;

        String s1;
        String s2;

        Category category1 = Category.getCategory(o1);
        Category category2 = Category.getCategory(o2);
        if (sortMap.containsKey(category1))
            s1 = sortMap.get(category1) + o1;
        else
            s1 = o1;

        if (sortMap.containsKey(category2))
            s2 = sortMap.get(category2) + o2;
        else
            s2 = o1;

        return s1.compareTo(s2);
    }

}

