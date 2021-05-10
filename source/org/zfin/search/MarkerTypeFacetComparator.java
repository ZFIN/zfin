package org.zfin.search;


import org.apache.solr.client.solrj.response.FacetField;
import org.zfin.marker.MarkerType;
import org.zfin.repository.RepositoryFactory;

import java.util.Comparator;

public class MarkerTypeFacetComparator<T extends FacetField.Count> implements Comparator<T> {

    public int compare(T o1, T o2) {
        MarkerType mt1 = RepositoryFactory.getMarkerRepository().getMarkerTypeByDisplayName(o1.getName());
        MarkerType mt2 = RepositoryFactory.getMarkerRepository().getMarkerTypeByDisplayName(o2.getName());

        if (o1 == null && o2 == null || mt1 == null && mt2 == null)
            return -1;
        if (o1 == null || mt1 == null)
            return -1;
        if (o2 == null || mt2 == null)
            return +1;

        return mt1.getSignificance().compareTo(mt2.getSignificance());
    }

}
