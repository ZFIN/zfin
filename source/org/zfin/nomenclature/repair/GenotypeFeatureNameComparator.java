package org.zfin.nomenclature.repair;

import java.util.Comparator;
import java.util.List;

public class GenotypeFeatureNameComparator implements Comparator<GenotypeFeatureName> {

    @Override
    public int compare(GenotypeFeatureName pattern1, GenotypeFeatureName pattern2) {
        if (pattern1.containsTransgenics() && !pattern2.containsTransgenics()) {
            return 1;
        } else if (pattern2.containsTransgenics() && !pattern1.containsTransgenics()) {
            return -1;
        }
        return String.CASE_INSENSITIVE_ORDER.compare(pattern1.toString(), pattern2.toString());
    }

    public static boolean listsEqual(List<GenotypeFeatureName> list1, List<GenotypeFeatureName> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for(int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }
}
