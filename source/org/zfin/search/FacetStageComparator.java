package org.zfin.search;

import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.search.presentation.FacetValue;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/*
* Used to sort (super) stage names into chronological order
*
* Why does it use strings rather than integers for the compare value?  So that it
* can fall back to comparing by the key if the key isn't found in the map.
* */
public class FacetStageComparator  <T extends FacetValue> implements Comparator<T> {
    private static final Map<String, String> sortMap;
    static {
        Map<String, String> map = new HashMap<String, String>();

        map.put("Zygote (0 - 0.74 hpf)","001");
        map.put("Cleavage (0.75 - 2.24 hpf)","002");
        map.put("Blastula (2.25 - 5.24 hpf)","003");
        map.put("Gastrula (5.25 - 10.32 hpf)","004");
        map.put("Segmentation (10.33 - 23.99 hpf)","005");
        map.put("Pharyngula (24.00 - 47.99 hpf)", "006");
        map.put("Hatching (48.00 - 71.99 hpf)", "007");
        map.put("Larval (72 hpf - 29.99 dpf)", "008");
        map.put("Juvenile (30 - 89.99 dpf)", "009");
        map.put("Adult (90.00 - 730.00 dpf)", "010");
        map.put("Unknown", "011");

        sortMap = Collections.unmodifiableMap(map);
    }


    public int compare(T o1, T o2) {
        if (o1== null && o2== null)
            return -1;
        if (o1== null && o2!= null)
            return -1;
        if (o1!= null && o2== null)
            return +1;

        String s1;
        String s2;

        if (sortMap.containsKey(o1.getLabel()))
            s1 = sortMap.get(o1.getLabel()) + o1.getLabel();
        else
            s1 = o1.getLabel();

        if (sortMap.containsKey(o2.getLabel()))
            s2 = sortMap.get(o2.getLabel()) + o2.getLabel();
        else
            s2 = o1.getLabel();

        return s1.compareTo(s2);
    }

}
