package org.zfin.mapping.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.repository.RepositoryFactory;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;

public class LinkageRepositoryTest {

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.setAuthenticatedUser();
        // TODO: this should load a specific database instance for testing purposes

    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }

    @Test
    public void findLinkageForMappedRNAClones(){
        Session session = currentSession() ;

        LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository() ;

        Clone clone = (Clone) session.get(Clone.class,"ZDB-CDNA-040425-812") ;
        List<String> mappedMarkerList = linkageRepository.getDirectMappedMarkers(clone) ;
        assertNotNull(mappedMarkerList) ;
        assertEquals(1,mappedMarkerList.size() );
        assertEquals( "8",mappedMarkerList.get(0)) ;

        clone = (Clone) session.get(Clone.class,"ZDB-EST-020816-240") ;
        mappedMarkerList = linkageRepository.getDirectMappedMarkers(clone) ; ;
        assertNotNull(mappedMarkerList) ;
        assertEquals(2,mappedMarkerList.size() );
        assertEquals( "16",mappedMarkerList.get(0) ) ;
        assertEquals( "17",mappedMarkerList.get(1) ) ;
    }

    @Test
    public void findLinkageForUnMappedRNAClones(){
        Session session = currentSession() ;

        LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository() ;

        Clone clone = (Clone) session.get(Clone.class,"ZDB-EST-000329-419") ;
        Set<String> lgs = linkageRepository.getLG(clone) ;
        assertNotNull(lgs) ;
        assertEquals(1,lgs.size() );
        assertEquals( "16",lgs.iterator().next() ) ;
    }


    @Test
    public void findLinkageForMappedClones(){
        Session session = currentSession() ;

        LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository() ;

        Clone clone = (Clone) session.get(Clone.class,"ZDB-BAC-030616-10") ;
        Set<String> lgs = linkageRepository.getLG(clone) ;
        assertNotNull(lgs) ;
        assertEquals(1,lgs.size() );
        assertEquals( "14",lgs.iterator().next() ) ;
    }

    @Test
    public void testGetLG() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the exisitng genes which might be merged
        try {
            LinkageRepository linkageRepository = RepositoryFactory.getLinkageRepository();
            Marker marker1 = (Marker) currentSession().get(Marker.class,"ZDB-EST-000426-1181");
            assertTrue("marker lg list contains all self panel mappings", linkageRepository.getLG(marker1).contains("13") && linkageRepository.getLG(marker1).contains("23"));
            Marker marker2 = (Marker) currentSession().get(Marker.class,"ZDB-GENE-990415-72");
            assertTrue("gene lg list contains its est's panel mapping", linkageRepository.getLG(marker2).contains("23"));
            assertFalse("gene lg list contains no bogus mapping", linkageRepository.getLG(marker2).contains("1"));
            Marker marker3 = (Marker) currentSession().get(Marker.class,"ZDB-GENE-060526-178");
            assertTrue("gene lg list contains clone's panel mapping", linkageRepository.getLG(marker3).contains("13"));

            Marker marker4 = (Marker) currentSession().get(Marker.class,"ZDB-RAPD-980526-288");
            assertTrue("marker lg list contains self linkage group mapping", linkageRepository.getLG(marker4).contains("12"));
            Marker marker5 = (Marker) currentSession().get(Marker.class,"ZDB-BAC-030616-45");
            assertTrue("marker lg list contains linkage mapping of contained marker/segment", linkageRepository.getLG(marker5).contains("9"));
            Marker marker6 = (Marker) currentSession().get(Marker.class,"ZDB-GENE-030616-611");
            assertTrue("gene lg list contains clone's linkage mapping", linkageRepository.getLG(marker6).contains("19"));

            Marker marker7 = (Marker) currentSession().get(Marker.class,"ZDB-GENE-070117-36");
            assertTrue("gene lg list contains allele's linkage group mapping", linkageRepository.getLG(marker7).contains("23"));

          /*  Marker marker8 = (Marker) currentSession().get(Marker.class,"ZDB-GENE-070117-2287");
            assertTrue("marker lg list contains allele's panel mapping", linkageRepository.getLG(marker8).contains("7"));*/

        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
