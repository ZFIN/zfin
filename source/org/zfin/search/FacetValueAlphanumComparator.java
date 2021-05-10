package org.zfin.search;

import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.search.presentation.FacetValue;
import org.zfin.util.AlphanumComparator;

import java.util.Comparator;

/**
 * Sort Solr FacetValue objects with 'human sort' so that 9 comes before 10
 */
public class FacetValueAlphanumComparator<T extends FacetValue> implements Comparator<T> {

    public int compare(T o1, T o2) {
        if (o1 == null && o2 == null)
            return -1;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return +1;

        AlphanumComparator<String> alphanumComparator = new AlphanumComparator<>();

        return alphanumComparator.compare(o1.getLabel().toLowerCase(), o2.getLabel().toLowerCase());

    }


}
