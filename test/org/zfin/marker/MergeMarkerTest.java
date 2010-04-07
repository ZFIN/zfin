package org.zfin.marker;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.antibody.Antibody;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.PublicationAttribution;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 */
public class MergeMarkerTest {
    private Logger logger = Logger.getLogger(MergeMarkerTest.class) ;

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
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    @Test
    public void mergeRealAntibodies(){

        try {
            HibernateUtil.createTransaction();
            String zdbIDToDelete = "ZDB-ATB-081002-22" ;
            Antibody antibodyToDelete = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbIDToDelete) ;
            String zdbIDToMergeInto = "ZDB-ATB-081002-19" ;
            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbIDToMergeInto) ;

            assertNotNull(antibodyToDelete) ;
            String zdbID1 = antibodyToDelete.getZdbID();
            assertEquals(zdbIDToDelete, zdbID1);
            assertEquals("zn-8",antibodyToDelete.getAbbreviation());
            assertEquals("zn-5",antibodyToMergeInto.getAbbreviation());

            /**
             * PRE VALIDATION
             */
            // validate some data components are there
            // * marker, of course
            assertNotNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID1)) ;
            // * private notes
            // none for this one
            // * external notes
            int notesAB1_Pre = antibodyToDelete.getExternalNotes().size();
            assertTrue(notesAB1_Pre>0);
            Query noteQuery = HibernateUtil.currentSession().createQuery(
                    "from ExternalNote en where en.externalDataZdbID = :zdbID ") ;
            int noteSizeAB2_Pre = noteQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(noteSizeAB2_Pre>0);

            // * antigen genes mostly
            int relatedMarkersAB1_Pre = antibodyToDelete.getAllRelatedMarker().size();
            assertTrue(relatedMarkersAB1_Pre>0);
            Query relatedMarkerQuery = HibernateUtil.currentSession().createQuery(
                    "from MarkerRelationship mr where mr.firstMarker.zdbID = :zdbID or mr.secondMarker.zdbID = :zdbID ");
            int relatedMarkerAB2_Pre = relatedMarkerQuery.setString("zdbID", zdbIDToMergeInto).list().size();
//            assertTrue(relatedMarkerAB2_Pre>0);

            // * dalias
            int markerAliasesAB1_Pre = antibodyToDelete.getAliases().size();
            assertTrue(markerAliasesAB1_Pre>0);
            Query markerAliasQuery = HibernateUtil.currentSession().createQuery(
                    "from MarkerAlias  ma where ma.dataZdbID = :zdbID ");
            int markerAliasAB2_Pre = markerAliasQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(markerAliasAB2_Pre>0);


            // * suppliers
            int markerSupplierAB1_Size = antibodyToDelete.getSuppliers().size();
            assertTrue(markerSupplierAB1_Size>0);
            Query markerSupplierQuery = HibernateUtil.currentSession().createQuery(
                    "from MarkerSupplier  ms where ms.dataZdbID = :zdbID ");
            int markerSupplierAB2_Pre = markerSupplierQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(markerSupplierAB2_Pre>0) ;


            // * sources
            // * record attributions
            int recordAttributionAB1_Size = antibodyToDelete.getPublications().size() ;
            assertTrue(recordAttributionAB1_Size>0);
            Query recordAttributionQuery =  HibernateUtil.currentSession().createQuery( "from RecordAttribution ra where ra.dataZdbID = :zdbID ");
            int recordAttributionAB2_Pre = recordAttributionQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(recordAttributionAB2_Pre>0);

            // * expression experiments
            int expressionExperimentAB1_Size = antibodyToDelete.getAntibodyLabelings().size();
            assertTrue(expressionExperimentAB1_Size>0);
            Query expressionExperimentQuery = HibernateUtil.currentSession().createQuery( "from ExpressionExperiment ee where ee.antibody.zdbID = :zdbID ");
            int expressionExperimentAB2_Pre = expressionExperimentQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(expressionExperimentAB2_Pre>0);

            /**
             * ACTION HERE
             */
            assertTrue(MergeService.mergeMarker(antibodyToDelete,antibodyToMergeInto)) ;

            /**
             * POST VALIDATION
             */
            // TODO: validate NO data components are there
            // validate no data components are there
            // * marker, of course
            assertNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID1)) ;
            assertNotNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbIDToMergeInto)) ;
            HibernateUtil.currentSession().flush();
            // * private notes
            // * external notes
            int noteSizeAB2_Post = noteQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0,noteQuery.setString("zdbID",zdbID1).list().size());
            assertEquals( notesAB1_Pre+noteSizeAB2_Pre,noteSizeAB2_Post );

            // * antigen genes
            int relatedMarkerAB2_Post = relatedMarkerQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0,relatedMarkerQuery.setString("zdbID",zdbID1).list().size());
            assertEquals(1,relatedMarkerAB2_Pre);
            assertEquals(1,relatedMarkersAB1_Pre);
            // this one actually gets merged into alcama
            assertEquals(1,relatedMarkerAB2_Post);

            // * dalias
            List<MarkerAlias> newMakerAliases = markerAliasQuery.setString("zdbID", zdbIDToMergeInto).list() ;
//            int markerAliasAB2_Post = markerAliasQuery.setString("zdbID", zdbID2).list().size();
            int markerAliasAB2_Post = newMakerAliases.size();
            assertEquals(0,markerAliasQuery.setString("zdbID",zdbID1).list().size());
            boolean hasPriorName = false ;
            for(MarkerAlias markerAlias: newMakerAliases){
                if(markerAlias.getAlias().equalsIgnoreCase(antibodyToDelete.getAbbreviation())){
                    hasPriorName = true ;
                }
            }
            assertTrue(hasPriorName) ;
            assertEquals(markerAliasAB2_Pre+markerAliasesAB1_Pre+1,markerAliasAB2_Post);

            //* suppliers
            int markerSupplierAB2_Post = markerSupplierQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0,markerSupplierQuery.setString("zdbID",zdbID1).list().size());
            // they acutally merge the suppliers into each other
            assertEquals(1,markerSupplierAB2_Pre);
            assertEquals(2,markerSupplierAB1_Size);
            assertEquals(2,markerSupplierAB2_Post);


            // * sources
            // * record attributions
            int recordAttributionAB2_Post = recordAttributionQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0,recordAttributionQuery.setString("zdbID",zdbID1).list().size());
            assertTrue(recordAttributionAB1_Size>=20);
            assertTrue(recordAttributionAB2_Pre>recordAttributionAB1_Size); // just a good guess, should be > 20
            assertTrue(recordAttributionAB2_Post > recordAttributionAB2_Pre);
            assertTrue(recordAttributionAB2_Pre+recordAttributionAB1_Size>recordAttributionAB2_Post);

            // * expression experiments
            int expressionExperimentAB2_Post = expressionExperimentQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0,expressionExperimentQuery.setString("zdbID",zdbID1).list().size());
            // TODO: any validation here?
//            assertEquals(expressionExperimentAB2_Pre+expressionExperimentAB1_Size,expressionExperimentAB2_Post);

        } catch (Exception e) {
            fail(e.toString()) ;
        }
        finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void deleteRealAntibodies(){

        try {
            HibernateUtil.createTransaction();
            String origZdbID = "ZDB-ATB-081002-22" ;
            Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(origZdbID) ;
            assertNotNull(antibody) ;
            String zdbID = antibody.getZdbID();
            assertEquals(origZdbID,zdbID);
            assertEquals("zn-8",antibody.getAbbreviation());

            // validate some data components are there
            // * marker, of course
            assertNotNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID)) ;
            // * private notes
            // none for this one
            // * external notes
            assertTrue(antibody.getExternalNotes().size()>0);
            // * antigen genes
            assertTrue(antibody.getAllRelatedMarker().size()>0);
            // * dalias
            assertTrue(antibody.getAliases().size()>0);
            // * suppliers
            assertTrue(antibody.getSuppliers().size()>0);
            // * sources
            // * record attributions
            assertTrue(antibody.getPublications().size()>0);
            // * expression experiments
            assertTrue(antibody.getAntibodyLabelings().size()>0);

            // DO DELETE
            assertTrue(MergeService.deleteMarker(antibody)) ;

            // validate no data components are there
            // * marker, of course
            assertNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID)) ;
            HibernateUtil.currentSession().flush();
            // * private notes
            // * external notes
            assertEquals(0,
                    HibernateUtil.currentSession().createQuery(
                            "from ExternalNote en where en.externalDataZdbID = :zdbID ")
                            .setString("zdbID",zdbID)
                            .list().size()
            );
            // * antigen genes
            assertEquals(0,
                    HibernateUtil.currentSession().createQuery(
                            "from MarkerRelationship mr where mr.firstMarker.zdbID = :zdbID or mr.secondMarker.zdbID = :zdbID ")
                            .setString("zdbID",zdbID)
                            .list().size()
            );
            // * dalias
            assertEquals(0,
                    HibernateUtil.currentSession().createQuery(
                            "from MarkerAlias  ma where ma.dataZdbID = :zdbID ")
                            .setString("zdbID",zdbID)
                            .list().size()
            );
            // * suppliers
            assertEquals(0,
                    HibernateUtil.currentSession().createQuery(
                            "from MarkerSupplier  ms where ms.dataZdbID = :zdbID ")
                            .setString("zdbID",zdbID)
                            .list().size()
            );
            // * sources
            // * record attributions
            assertEquals(0,
                    HibernateUtil.currentSession().createQuery(
                            "from RecordAttribution ra where ra.dataZdbID = :zdbID ")
                            .setString("zdbID",zdbID)
                            .list().size()
            );
            // * expression experiments
            assertEquals(0,
                    HibernateUtil.currentSession().createQuery(
                            "from ExpressionExperiment ee where ee.antibody.zdbID = :zdbID ")
                            .setString("zdbID",zdbID)
                            .list().size()
            );




        } catch (Exception e) {
            fail(e.toString()) ;
        }
        finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    // 2. need to add an alias to this markerToMergeInto
    // A - no overlap, create new alias from markertodelete and move all aliases over, add marker history for new alias
    // B - overlap in aliases only, create new alias from markertodelete and and combine attributions of matching alias, use old alias for marker history
    // C - markertomergeinto already has alias of markertodelete name, do not create new alias (no attribution to add), but move rest over
    // D - markertodelete has alias that is name of markertomergeinto, I think that this is case A
    @Test
    public void mergeAliasesNoOverlap(){
        // we have one alias, here
        Marker markerToDelete = new Marker();
        markerToDelete.setAbbreviation("dogz");

        MarkerAlias alias1 = new MarkerAlias();
        alias1.setMarker(markerToDelete);
        alias1.setAlias("mammal");
        PublicationAttribution publicationAttribution = new PublicationAttribution();
        Publication pub1 = new Publication() ;
        pub1.setZdbID("A");
        publicationAttribution.setPublication(pub1);
        alias1.addPublication(publicationAttribution);
        Set<MarkerAlias> markerToDeleteAliases = new HashSet<MarkerAlias>() ;
        markerToDeleteAliases.add(alias1) ;
        markerToDelete.setAliases(markerToDeleteAliases);


        // also has mammal alias, but with different pub
        Marker markerToMergeInto = new Marker() ;
        markerToMergeInto.setAbbreviation("catz");
        Set<MarkerAlias> markerToMergeIntoAliases = new HashSet<MarkerAlias>() ;

        MarkerAlias alias2 = new MarkerAlias();
        alias2.setMarker(markerToMergeInto);
        alias2.setAlias("mammal");
        PublicationAttribution publicationAttribution2 = new PublicationAttribution();
        Publication pub2 = new Publication() ;
        pub2.setZdbID("B");
        publicationAttribution2.setPublication(pub2);
        alias2.addPublication(publicationAttribution2);
        markerToMergeIntoAliases.add(alias2) ;

        MarkerAlias alias3 = new MarkerAlias();
        alias3.setMarker(markerToMergeInto);
        alias3.setAlias("dogz");
        markerToMergeIntoAliases.add(alias3) ;

        markerToMergeInto.setAliases(markerToMergeIntoAliases);


        // thing to test
        MarkerAlias markerAlias = MergeService.mergeAliases(markerToDelete,markerToMergeInto);


        assertNotNull(markerAlias) ;
        assertEquals("dogz",markerAlias.getAlias());
        Set<MarkerAlias> markerAliasSet = markerToMergeInto.getAliases() ;
        assertEquals(2,markerAliasSet.size()) ;
        for(MarkerAlias aMarkerAlias : markerAliasSet){
            if(aMarkerAlias.getAlias().equals("dogz")){
                assertEquals(0,aMarkerAlias.getPublications().size());
            }
            else
            if(aMarkerAlias.getAlias().equals("catz")){
                assertEquals(1,aMarkerAlias.getPublications().size());
            }
            else
            if(aMarkerAlias.getAlias().equals("mammal")){
                assertEquals(2,aMarkerAlias.getPublications().size());
            }
            else{
                fail("Alias undefined: "+ markerAlias);
            }
        }
    }
}
