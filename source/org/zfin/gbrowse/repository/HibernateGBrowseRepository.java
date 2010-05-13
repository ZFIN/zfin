package org.zfin.gbrowse.repository;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.gbrowse.GBrowseFeature;
import org.zfin.marker.Marker;

import java.util.Set;
import java.util.TreeSet;

public class HibernateGBrowseRepository implements GBrowseRepository {

    public Set<GBrowseFeature> getGBrowseFeaturesForMarker(Marker marker) {
        Session session = GBrowseHibernateUtil.currentSession();
        TreeSet<GBrowseFeature> features = new TreeSet<GBrowseFeature>();
        Criteria criteria = session.createCriteria(GBrowseFeature.class)
                .createCriteria("attributes")
                .add(Restrictions.eq("value", marker.getZdbID()));
        features.addAll(criteria.list());
        return features;
    }

    public Boolean isMarkerInGBrowse(Marker marker) {
        Session session = GBrowseHibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(GBrowseFeature.class)
                .createCriteria("attributes").add(Restrictions.eq("value", marker.getZdbID()));
        if (criteria.list().size() > 0)
            return true;
        else
            return false;

    }

}
