package org.zfin.mapping;


import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class GenomeVersionComparator <T extends GenomeLocation> implements Comparator<T> {

    private static final Map<String, String> sortMap;

    static {

        Map<String, String> map = new HashMap<>();
        Integer i = 1;

        map.put(GenomeLocation.GRCZ11, pad(i++));
        map.put(GenomeLocation.GRCZ10, pad(i++));
        map.put(GenomeLocation.ZV9, pad(i++));

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
        String assembly1 = (o1.getAssembly());
        String assembly2 = (o2.getAssembly());
        if (sortMap.containsKey(assembly1))
            s1 = sortMap.get(assembly1);
        else
            s1 = "zzz" + o1.getAssembly();

        if (sortMap.containsKey(assembly2))
            s2 = sortMap.get(assembly2);
        else
            s2 = "zzz" + o2.getAssembly();

        return s1.compareTo(s2);
    }

}
