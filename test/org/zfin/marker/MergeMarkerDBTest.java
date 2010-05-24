package org.zfin.marker;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.expression.*;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.GenotypeExperiment;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.people.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.junit.Assert.*;

/**
 */
public class MergeMarkerDBTest {
    private Logger logger = Logger.getLogger(MergeMarkerDBTest.class) ;

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

    @SuppressWarnings("unchecked")
    private List<Publication> createPublications() {
        return HibernateUtil.currentSession().createCriteria(Publication.class)
                .setMaxResults(2).list();
    }


    @SuppressWarnings("unchecked")
    private List<GenotypeExperiment> createGenotypeExperiments() {
        return HibernateUtil.currentSession().createCriteria(GenotypeExperiment.class)
                .setMaxResults(2).list();
    }

    private List<Antibody> createAntibodies(Publication pub) {

        Person p = RepositoryFactory.getProfileRepository().getPersonByName("ndunn") ;
        MarkerType mt = RepositoryFactory.getMarkerRepository().getMarkerTypeByName(Marker.Type.ATB.toString());

        List<Antibody> antibodies = new ArrayList<Antibody>() ;

        Antibody antibodyA = new Antibody() ;
        antibodyA.setAbbreviation("antibodya");
        antibodyA.setName("antibodya");
        antibodyA.setOwner(p);
        antibodyA.setMarkerType(mt);
//        RepositoryFactory.getMarkerRepository().createMarker(antibodyA,pub);
        HibernateUtil.currentSession().save(antibodyA) ;


        HibernateUtil.currentSession().flush();

        Antibody antibodyB = new Antibody() ;
        antibodyB.setAbbreviation("antibodyb");
        antibodyB.setName("antibodyb");
        antibodyB.setOwner(p);
        antibodyB.setMarkerType(mt);
//        RepositoryFactory.getMarkerRepository().createMarker(antibodyB,pub);
        HibernateUtil.currentSession().save(antibodyB) ;

        antibodies.add(antibodyA) ;
        antibodies.add(antibodyB) ;

        HibernateUtil.currentSession().flush();

        HibernateUtil.currentSession().merge(antibodyA) ;
        HibernateUtil.currentSession().merge(antibodyB) ;

        return antibodies ;
    }

    private Figure createFigure(String figa1ZdbID, Publication pub1,ExpressionResult expressionResult) {
        HibernateUtil.currentSession().createSQLQuery(" " +
                " insert into zdb_active_data(zactvd_zdb_id) values (:fig1ZdbID) " +
                "")
                .setParameter("fig1ZdbID",figa1ZdbID)
                .executeUpdate() ;

        HibernateUtil.currentSession().createSQLQuery(" " +
                " insert into figure(fig_zdb_id,fig_source_zdb_id) values (:fig1ZdbID,:pubID) " +
                "")
                .setParameter("fig1ZdbID",figa1ZdbID)
                .setParameter("pubID",pub1.getZdbID())
                .executeUpdate() ;

        HibernateUtil.currentSession().createSQLQuery(" "+
                " insert into expression_pattern_figure (xpatfig_fig_zdb_id,xpatfig_xpatres_zdb_id)  " +
                " values ( :fig1ZdbID , :expatResultZdbID )  " +
                "")
                .setParameter("fig1ZdbID",figa1ZdbID)
                .setParameter("expatResultZdbID",expressionResult.getZdbID())
                .executeUpdate();

        HibernateUtil.currentSession().flush();
        return  (Figure) HibernateUtil.currentSession().createCriteria(Figure.class)
                .add(Restrictions.eq("zdbID",figa1ZdbID)).uniqueResult();

    }

    @SuppressWarnings("unchecked")
    private List<ExpressionAssay> createExpressionAssays() {

        return HibernateUtil.currentSession().createCriteria(ExpressionAssay.class)
                .setMaxResults(2).list();

    }

    /**
     *     /**
     * Given antibodies A and B, where A will be merged into B and then deleted

     for all expression experiments on A:  EEa
     if EEa not contained in antibody B: then
     update the antibody on EEa to point to antibody B
     else
     if EEa matches as expression_experiment B on antibody B (EEb): then
     for all expression_results in EEa: ERa
     if ERa not contained in expression_results on EEb (ERb): then
     update ERa to point to EEb
     else
     for all expression_result_figures in ERa: ERFa
     if ERFa not contained in expression_results_figure on ERb (ERFb): then
     update ERFa to point to ERb
     else
     nada

     delete A

     INIT:
     A(EEA1,EEA2(ERA1,ERA2(FA1,FA2))
     B(EEB1,EEB2(ERB1,ERB2(FB1,FB2))

     TEST:
     Antibody A has ExpressionExperiments EEA*
     Antibody B has ExpressionExperiments: EEB*
     EEA1 not in EEB* -> gets moved
     EEA2 matches EEB2:
     EEA2.ERA1 not in EEB2.ERB*  -> gets moved
     EEA2.ERA2 matches EEB2.ERB2:
     EEA2.ERA2.FA1 not in EEA2.ERA2.FB* -> gets moved
     EEA2.ERA2.FA2 matches EEA2.ERA2.FB2 -> nothing happens

     FINAL:
     A(EEA2(ERA2(FA2))) // not moved
     B(EEA1,EEB1,EEB2(ERA1,ERB1,ERB2(FA1,FB1,FB2))) // showing merged first

     */
    @Test
    public void createAndMergeExpressionResultFiguresOver(){

        try{
            HibernateUtil.createTransaction();
            List<Publication> publications = createPublications();
            Publication pub1 = publications.get(0) ;
            Publication pub2 = publications.get(1) ;

            List<GenotypeExperiment> genotypeExperiments = createGenotypeExperiments();
            GenotypeExperiment genox1 = genotypeExperiments.get(0) ;
            GenotypeExperiment genox2 = genotypeExperiments.get(1) ;

            List<ExpressionAssay> expressionAssays = createExpressionAssays();
            ExpressionAssay assay1 = expressionAssays.get(0) ;
            ExpressionAssay assay2 = expressionAssays.get(1) ;

            AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository() ;

            Term anatomy1 = new GenericTerm();
            anatomy1.setID("ZDB-TERM-100331-1144");
            anatomy1.setTermName("pelvic fin");
            Term anatomy2 = new GenericTerm();
            anatomy2.setID("ZDB-TERM-100331-141");
            anatomy2.setTermName("pelvic fin");

            DevelopmentStage stage1 = anatomyRepository.getStageByID("ZDB-STAGE-010723-11") ;
            DevelopmentStage stage2 = anatomyRepository.getStageByID("ZDB-STAGE-010723-39") ;

            List<Antibody> antibodies = createAntibodies(pub1) ;


            Antibody antibodyA = antibodies.get(0) ;

            ExpressionExperiment eea1 = new ExpressionExperiment() ;
//            eea1.setZdbID("eea1");
            eea1.setAntibody(antibodyA);
            eea1.setPublication(pub1);
            eea1.setGenotypeExperiment(genox1);
            eea1.setAssay(assay1);
            HibernateUtil.currentSession().save(eea1) ;

            // same as eeb2
            ExpressionExperiment eea2 = new ExpressionExperiment() ;
//            eea2.setZdbID(eeSameZdbID);
            eea2.setAntibody(antibodyA);
            eea2.setPublication(pub2);
            eea2.setGenotypeExperiment(genox2);
            eea2.setAssay(assay2);
            HibernateUtil.currentSession().save(eea2) ;
            String eeSameZdbID = eea2.getZdbID();

            Set<ExpressionExperiment> expressionExperimentsA = new HashSet<ExpressionExperiment>() ;
            expressionExperimentsA.add(eea1);
            expressionExperimentsA.add(eea2);
            antibodyA.setAntibodyLabelings(expressionExperimentsA);

            ExpressionResult era1 = new ExpressionResult() ;
//            era1.setZdbID("era1");
            era1.setExpressionExperiment(eea2);
            era1.setSuperterm(anatomy1);
            era1.setEndStage(stage1);
            era1.setStartStage(stage1);
            era1.setExpressionFound(false);
            HibernateUtil.currentSession().save(era1) ;

            // matches erb2
            ExpressionResult era2 = new ExpressionResult() ;
//            era2.setZdbID("ermatchesZdbID");
            era2.setExpressionExperiment(eea2);
            era2.setSuperterm(anatomy2);
            era2.setEndStage(stage2);
            era2.setStartStage(stage2);
            era2.setExpressionFound(true);
            HibernateUtil.currentSession().save(era2) ;

            HibernateUtil.currentSession().flush();

            Set<ExpressionResult> expressionResultsA = new HashSet<ExpressionResult>() ;
            expressionResultsA.add(era1);
            expressionResultsA.add(era2);
            eea2.setExpressionResults(expressionResultsA);

            HibernateUtil.currentSession().flush();


            String figa1ZdbID = "ZDB-FIG-000000-99" ;
            Figure fa1 = createFigure(figa1ZdbID,pub1,era2) ;

            String figa2ZdbID = "ZDB-FIG-000000-100" ;
            Figure fa2 = createFigure(figa2ZdbID,pub1,era2) ;
            HibernateUtil.currentSession().evict(era2); 
            era2 = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,era2.getZdbID()) ;
            HibernateUtil.currentSession().flush();
            assertEquals(2,era2.getFigures().size()) ;


            Antibody antibodyB = antibodies.get(1) ;
            // this should be different from eea1
            ExpressionExperiment eeb1 = new ExpressionExperiment() ;
//            eeb1.setZdbID("eeb1");
            eeb1.setAntibody(antibodyB);
            eeb1.setPublication(pub1);
            eeb1.setGenotypeExperiment(genox2); // THIS IS DIFFERENT
            eeb1.setAssay(assay1);
            HibernateUtil.currentSession().save(eeb1) ;

            // this should be the same as eea2
            ExpressionExperiment eeb2 = new ExpressionExperiment();
////            eeb2.setZdbID(eeSameZdbID);
            eeb2.setAntibody(antibodyB);
            eeb2.setPublication(pub2);
            eeb2.setGenotypeExperiment(genox2);
            eeb2.setAssay(assay2);
            HibernateUtil.currentSession().save(eeb2) ;

            // different from era1
            ExpressionResult erb1 = new ExpressionResult() ;
//            erb1.setZdbID("erb1");
            erb1.setExpressionExperiment(eeb2);
            erb1.setSuperterm(anatomy1); // THIS IS NOW DIFFERENT
            erb1.setEndStage(stage2);
            erb1.setStartStage(stage2);
            erb1.setExpressionFound(true);
            HibernateUtil.currentSession().save(erb1) ;

            // matches era2
            ExpressionResult erb2 = new ExpressionResult() ;
//            erb2.setZdbID("ermatchesZdbID");
            erb2.setExpressionExperiment(eeb2);
            erb2.setSuperterm(anatomy2);
            erb2.setEndStage(stage2);
            erb2.setStartStage(stage2);
            erb2.setExpressionFound(true);
            HibernateUtil.currentSession().save(erb2) ;

            Set<ExpressionResult> expressionResultB = new HashSet<ExpressionResult>() ;
            expressionResultB.add(erb1);
            expressionResultB.add(erb2);
            eeb2.setExpressionResults(expressionResultB);

            Set<ExpressionExperiment> expressionExperimentsB = new HashSet<ExpressionExperiment>() ;
            expressionExperimentsB.add(eeb1);
            expressionExperimentsB.add(eeb2);
            antibodyB.setAntibodyLabelings(expressionExperimentsB);

            HibernateUtil.currentSession().flush();

            String figb1ZdbID = "ZDB-FIG-000000-101" ;
            Figure fb1 = createFigure(figb1ZdbID,pub1,erb2) ;

//            String figb2ZdbID = "ZDB-FIG-000000-202" ;

            // fb2 and fa2 are the same except that fb2 is linked to expressionResultB2.
            Figure fb2 = fa2 ;
            HibernateUtil.currentSession().createSQLQuery(" "+
                    " insert into expression_pattern_figure (xpatfig_fig_zdb_id,xpatfig_xpatres_zdb_id)  " +
                    " values ( :fig1ZdbID , :expatResultZdbID )  " +
                    "")
                    .setParameter("fig1ZdbID",fb2.getZdbID())
                    .setParameter("expatResultZdbID",erb2.getZdbID())
                    .executeUpdate();


            HibernateUtil.currentSession().evict(erb2);
            erb2 = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,erb2.getZdbID()) ;
            HibernateUtil.currentSession().flush();
            assertEquals(2,erb2.getFigures().size()) ;


            // BEGIN TESTS

            assertEquals(2, antibodyA.getAntibodyLabelings().size()) ;
            for( Iterator<ExpressionExperiment> iter = antibodyA.getAntibodyLabelings().iterator() ; iter.hasNext() ;){
                ExpressionExperiment ee = iter.next() ;
                if(ee.getZdbID().equals(eea1.getZdbID())){
                    assertNull(ee.getExpressionResults());
                }
                else
                if(ee.getZdbID().equals(eeSameZdbID)){
                    assertEquals(2,ee.getExpressionResults().size()) ;
                    for(Iterator<ExpressionResult> iterER = ee.getExpressionResults().iterator() ; iterER.hasNext() ;){
                        ExpressionResult er = iterER.next();
                        if(er.getZdbID().equals(era1.getZdbID())){
                            assertNull(er.getFigures());
                        }
                        else
                        if(er.getZdbID().equals(era2.getZdbID())){
                            
//                            if(er.getZdbID().equals("ermatchesZdbID")){
                            HibernateUtil.currentSession().evict(er);
                            er = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,er.getZdbID()) ;
                            assertEquals(2,er.getFigures().size());
                            for(Iterator<Figure> iterFig = er.getFigures().iterator(); iterFig.hasNext() ;){
                                Figure fa = iterFig.next();
                                if(!fa.getZdbID().equals(fa1.getZdbID()) && !fa.getZdbID().equals(fa2.getZdbID())){
                                    fail("Did not contain a known figure");
                                }
                            }
                        }
                        else{
                            fail("Failed to find an expression result.");
                        }
                    }
                }
                else{
                    fail("Failed to find an antibody labeling.");
                }
            }

            assertEquals(2, antibodyB.getAntibodyLabelings().size()) ;
            for( Iterator<ExpressionExperiment> iter = antibodyB.getAntibodyLabelings().iterator() ; iter.hasNext() ;){
                ExpressionExperiment ee = iter.next() ;
                if(ee.getZdbID().equals(eeb1.getZdbID())){
                    assertNull(ee.getExpressionResults()) ;
                }
                else
                if(ee.getZdbID().equals(eeb2.getZdbID())){
                    assertEquals(2,ee.getExpressionResults().size()) ;
                    for(Iterator<ExpressionResult> iterER = ee.getExpressionResults().iterator() ; iterER.hasNext() ;){
                        ExpressionResult er = iterER.next();
                        if(er.getZdbID().equals(erb1.getZdbID())){
                            assertNull(er.getFigures());
                        }
                        else
                        if(er.getZdbID().equals(erb2.getZdbID())){
                            HibernateUtil.currentSession().evict(er);
                            er = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,er.getZdbID()) ;
                            assertEquals(2,er.getFigures().size());
                            for(Iterator<Figure> iterFig = er.getFigures().iterator(); iterFig.hasNext() ;){
                                Figure fa = iterFig.next();
                                if(!fa.getZdbID().equals(fb1.getZdbID()) && !fa.getZdbID().equals(fb2.getZdbID())){
                                    fail("Did not contain a known figure: "+ fa.getZdbID());
                                }
                            }
                        }
                        else{
                            fail("failed to find expression result:"+ er.getZdbID()) ;
                        }
                    }
                }
                else{
                    fail("Failed to find antibody labeling: "+ ee.getZdbID()) ;
                }
            }


            assertNull(antibodyB.getMatchingAntibodyLabeling(eea1)) ;
            assertNotNull(antibodyB.getMatchingAntibodyLabeling(eea2)) ;


//        INIT:
//        A(EEA1,EEA2(ERA1,ERA2(FA1,FA2))
//        B(EEB1,EEB2(ERB1,ERB2(FB1,FB2))

            MergeService.mergeAntibodyLabeling(antibodyA, antibodyB);

//        FINAL:
//        A(EEA2(ERA2(FA2))) // not moved

            assertEquals(1, antibodyA.getAntibodyLabelings().size()) ;
            ExpressionExperiment ea2_final = antibodyA.getAntibodyLabelings().iterator().next() ;
            assertEquals(eeSameZdbID,ea2_final.getZdbID()) ;
            assertEquals(1,ea2_final.getExpressionResults().size()) ;
            ExpressionResult er2_final = ea2_final.getExpressionResults().iterator().next() ;
            assertEquals(era2.getZdbID(),er2_final.getZdbID()) ;
            HibernateUtil.currentSession().evict(er2_final);
            er2_final = (ExpressionResult) HibernateUtil.currentSession().get(ExpressionResult.class,er2_final.getZdbID()) ;
            assertEquals(1,er2_final.getFigures().size()) ;
            Figure fa2_final = er2_final.getFigures().iterator().next();
            assertEquals(fa2.getZdbID(),fa2_final.getZdbID()) ;

//        B(EEA1,EEB1,EEB2(ERA1,ERB1,ERB2(FA1,FB1,FB2))) // showing merged first
            assertEquals(3, antibodyB.getAntibodyLabelings().size()) ;
            for(Iterator<ExpressionExperiment> iterEE = antibodyB.getAntibodyLabelings().iterator() ; iterEE.hasNext() ;){
                ExpressionExperiment expressionExperiment = iterEE.next();

                if(expressionExperiment.getZdbID().equals(eeb2.getZdbID())){
                    assertEquals(3,expressionExperiment.getExpressionResults().size()) ;
                    for(Iterator<ExpressionResult> iterER = expressionExperiment.getExpressionResults().iterator()  ; iterER.hasNext();){
                        ExpressionResult expressionResult = iterER.next();
                        if(expressionResult.getZdbID().equals(erb2.getZdbID())){
                            HibernateUtil.currentSession().evict(expressionResult);
                            expressionResult = (ExpressionResult) HibernateUtil.currentSession()
                                    .get(ExpressionResult.class,expressionResult.getZdbID()) ;
                            assertEquals(3,expressionResult.getFigures().size()) ;
                            for(Iterator<Figure> iterFig = expressionResult.getFigures().iterator() ; iterFig.hasNext() ;){
                                Figure figure = iterFig.next();
                                if( !figure.getZdbID().equals(fb2.getZdbID())
                                        && !figure.getZdbID().equals(fa1.getZdbID())
                                        && !figure.getZdbID().equals(fb1.getZdbID())
                                        ){
                                    fail("fig not found:" + figure.getZdbID()) ;
                                }
                            }
                        }
                        else
                        if( !expressionResult.getZdbID().equals(era1.getZdbID()) &&
                                !expressionResult.getZdbID().equals(erb1.getZdbID()) ){
                            fail("er not found!") ;
                        }
                    }
                }
                else
                if( !expressionExperiment.getZdbID().equals(eea1.getZdbID()) &&
                        !expressionExperiment.getZdbID().equals(eeb1.getZdbID()) ){
                    fail("ee not found!" + expressionExperiment.getZdbID()) ;
                }
            }
        } catch (Exception e) {
            fail(e.toString()) ;
        }
        finally {
            HibernateUtil.rollbackTransaction();
        }

    }


}
