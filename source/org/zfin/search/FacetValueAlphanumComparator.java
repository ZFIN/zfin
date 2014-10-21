package org.zfin.search;

import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.util.AlphanumComparator;

import java.util.Comparator;

/**
 * Sort Solr Facet.Count objects with 'human sort' so that 9 comes before 10
 */
public class FacetValueAlphanumComparator<T extends FacetField.Count> implements Comparator<T> {

    public int compare(T o1, T o2) {
        if (o1 == null && o2 == null)
            return -1;
        if (o1 == null)
            return -1;
        if (o2 == null)
            return +1;

        AlphanumComparator<String> alphanumComparator = new AlphanumComparator<>();

        return alphanumComparator.compare(o1.getName().toLowerCase(), o2.getName().toLowerCase());

    }


}
