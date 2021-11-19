package org.zfin.nomenclature.repair;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;

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

}
