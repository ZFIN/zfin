package org.zfin.mapping;

import java.util.Comparator;

public class ChromosomeComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        Integer num1 = Integer.parseInt(o1);
        Integer num2 = Integer.parseInt(o2);
        return num1.compareTo(num2);
    }

}
