package org.zfin.search;

import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.marker.Marker;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MarkerSearchTypeGroupComparator<T extends FacetField.Count> implements Comparator<T> {

    private static final Map<String, String> sortMap;

    static {

        Map<String, String> map = new HashMap<>();
        Integer i = 1;

        map.put(Marker.TypeGroup.SEARCHABLE_GENE.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_TRANSCRIPT.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_EFG.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_REGION.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_STR.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_CLONE.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_SMALL_SEGMENT.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_CONSTRUCT.getDisplayName(), pad(i++));
        map.put(Marker.TypeGroup.SEARCHABLE_ANTIBODY.getDisplayName(), pad(i++));

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

        String s1 = null;
        String s2 = null;
        
        if (sortMap.containsKey(o1.getName()))
            s1 = sortMap.get(o1.getName()) + o1.getName();
        else
            s1 = o1.getName();

        if (sortMap.containsKey(o2.getName()))
            s2 = sortMap.get(o2.getName()) + o2.getName();
        else
            s2 = o1.getName();

        return s1.compareTo(s2);
    }


}
