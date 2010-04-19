package org.zfin.gbrowse;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.zfin.framework.GBrowseHibernateUtil;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.gbrowse.repository.GBrowseRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.TestConfiguration;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Set;
import java.util.Map;

public class GBrowseRepositoryTest {

    private Logger logger = Logger.getLogger(GBrowseRepositoryTest.class);

    static SessionFactory gbrowseSessionFactory = GBrowseHibernateUtil.getSessionFactory();
    static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
    static GBrowseRepository gbrowseRepository = RepositoryFactory.getGBrowseRepository();
    static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();

    static {
        if (gbrowseSessionFactory == null) {
            TestConfiguration.initApplicationProperties();            
            GBrowseHibernateUtil.initForTest();
        }
        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.setAuthenticatedUser();
    }

    @After
    public void closeSession() {
        GBrowseHibernateUtil.closeSession();        
    }


   @Test
    public void getGBrowseFeaturesForMarkerTest() {
        Marker pax6a = markerRepository.getMarkerByAbbreviation("pax6a");
        GBrowseFeature feature = gbrowseRepository.getGBrowseFeaturesForMarker(pax6a).iterator().next();
        assertNotNull("can get a GBrowseFeature for pax6a", feature);
        assertNotNull("The one feature has a start", feature.getStart());
        assertNotNull("The one feature has an end", feature.getEnd());
        assertNotNull("The one feature has a contig", feature.getContig().getName());
        assertNotNull("The one feature has a type",feature.getType().getTag());

    }

    @Test
    public void isMarkerInGBrowseTest() {
        Marker pax6a = markerRepository.getMarkerByAbbreviation("pax6a");
        assertTrue("pax6a is in the GBrowse database.", gbrowseRepository.isMarkerInGBrowse(pax6a));

        Marker marker = new Marker();
        marker.setZdbID("ZDB-FOO-000000-0");
        marker.setName("humuhumunukunukuapua-001");
        assertFalse("humuhumunukunukuapua-001 is not in the GBrowse database",gbrowseRepository.isMarkerInGBrowse(marker));
    }

    @Test
    public void featureMapTest() {

        Marker pax6a = markerRepository.getMarkerByAbbreviation("pax6a");

        Map<GBrowseContig,Set<GBrowseFeature>> featureMap = GBrowseService.getGBrowseFeaturesGroupedByContig(pax6a);
        assertNotNull("featureMap for marker is not null", featureMap);

        assertTrue("pax6a has one contig", featureMap.keySet().size() == 1);
        GBrowseContig pax6acontig = featureMap.keySet().iterator().next();

        for (GBrowseFeature feature : featureMap.get(pax6acontig)) {
            assertNotNull("pax6a's gbrowse features have names", feature.getName());
        }

    }

}