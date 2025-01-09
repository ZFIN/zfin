package org.zfin.marker;

import org.hibernate.Query;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.TestConfiguration;
import org.zfin.antibody.Antibody;
import org.zfin.expression.ExpressionAssay;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.FishExperiment;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This test needs some work or should be removed.
 */
public class MergeMarkerDBTest extends AbstractDatabaseTest {

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }

    @Test
    @Ignore
    public void mergeRealAntibodies() {

        try {
            HibernateUtil.createTransaction();
            String zdbIDToDelete = "ZDB-ATB-081002-22";
            Antibody antibodyToDelete = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbIDToDelete);
            String zdbIDToMergeInto = "ZDB-ATB-081002-19";
            Antibody antibodyToMergeInto = RepositoryFactory.getAntibodyRepository().getAntibodyByID(zdbIDToMergeInto);

            assertNotNull(antibodyToDelete);
            String zdbID1 = antibodyToDelete.getZdbID();
            assertEquals(zdbIDToDelete, zdbID1);
            assertEquals("zn-8", antibodyToDelete.getAbbreviation());
            assertEquals("zn-5", antibodyToMergeInto.getAbbreviation());

            /**
             * PRE VALIDATION
             */
            // validate some data components are there
            // * marker, of course
            assertNotNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID1));
            // * private notes
            // none for this one
            // * external notes
            int notesAB1_Pre = antibodyToDelete.getExternalNotes().size();
            assertTrue(notesAB1_Pre > 0);
            Query noteQuery = HibernateUtil.currentSession().createQuery(
                "from ExternalNote en where en.externalDataZdbID = :zdbID ");
            int noteSizeAB2_Pre = noteQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(noteSizeAB2_Pre > 0);

            // * antigen genes mostly
            int relatedMarkersAB1_Pre = antibodyToDelete.getAllRelatedMarker().size();
            assertTrue(relatedMarkersAB1_Pre > 0);
            Query relatedMarkerQuery = HibernateUtil.currentSession().createQuery(
                "from MarkerRelationship mr where mr.firstMarker.zdbID = :zdbID or mr.secondMarker.zdbID = :zdbID ");
            int relatedMarkerAB2_Pre = relatedMarkerQuery.setString("zdbID", zdbIDToMergeInto).list().size();
//            assertTrue(relatedMarkerAB2_Pre>0);

            // * dalias
            int markerAliasesAB1_Pre = antibodyToDelete.getAliases().size();
            assertTrue(markerAliasesAB1_Pre > 0);
            Query markerAliasQuery = HibernateUtil.currentSession().createQuery(
                "from MarkerAlias  ma where ma.dataZdbID = :zdbID ");
            int markerAliasAB2_Pre = markerAliasQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(markerAliasAB2_Pre > 0);


            // * suppliers
            int markerSupplierAB1_Size = antibodyToDelete.getSuppliers().size();
            assertTrue(markerSupplierAB1_Size > 0);
            Query markerSupplierQuery = HibernateUtil.currentSession().createQuery(
                "from MarkerSupplier  ms where ms.dataZdbID = :zdbID ");
            int markerSupplierAB2_Pre = markerSupplierQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(markerSupplierAB2_Pre > 0);


            // * sources
            // * record attributions
            int recordAttributionAB1_Size = antibodyToDelete.getPublications().size();
            assertTrue(recordAttributionAB1_Size > 0);
            Query recordAttributionQuery = HibernateUtil.currentSession().createQuery("from RecordAttribution ra where ra.dataZdbID = :zdbID ");
            int recordAttributionAB2_Pre = recordAttributionQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(recordAttributionAB2_Pre > 0);

            // * expression experiments
            int expressionExperimentAB1_Size = antibodyToDelete.getAntibodyLabelings().size();
            assertTrue(expressionExperimentAB1_Size > 0);
            Query expressionExperimentQuery = HibernateUtil.currentSession().createQuery("from ExpressionExperiment2 ee where ee.antibody.zdbID = :zdbID ");
            int expressionExperimentAB2_Pre = expressionExperimentQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertTrue(expressionExperimentAB2_Pre > 0);

            /**
             * ACTION HERE
             */
            assertTrue(MergeService.mergeMarker(antibodyToDelete, antibodyToMergeInto));

            /**
             * POST VALIDATION
             */
            // TODO: validate NO data components are there
            // validate no data components are there
            // * marker, of course
            assertNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbID1));
            assertNotNull(RepositoryFactory.getMarkerRepository().getMarkerByID(zdbIDToMergeInto));
            HibernateUtil.currentSession().flush();
            // * private notes
            // * external notes
            int noteSizeAB2_Post = noteQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0, noteQuery.setString("zdbID", zdbID1).list().size());
            assertEquals(notesAB1_Pre + noteSizeAB2_Pre, noteSizeAB2_Post);

            // * antigen genes
            int relatedMarkerAB2_Post = relatedMarkerQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0, relatedMarkerQuery.setString("zdbID", zdbID1).list().size());
            assertEquals(1, relatedMarkerAB2_Pre);
            assertEquals(1, relatedMarkersAB1_Pre);
            // this one actually gets merged into alcama
            assertEquals(1, relatedMarkerAB2_Post);

            // * dalias
            List<MarkerAlias> newMakerAliases = markerAliasQuery.setString("zdbID", zdbIDToMergeInto).list();
//            int markerAliasAB2_Post = markerAliasQuery.setString("zdbID", zdbID2).list().size();
            int markerAliasAB2_Post = newMakerAliases.size();
            assertEquals(0, markerAliasQuery.setString("zdbID", zdbID1).list().size());
            boolean hasPriorName = false;
            for (MarkerAlias markerAlias : newMakerAliases) {
                if (markerAlias.getAlias().equalsIgnoreCase(antibodyToDelete.getAbbreviation())) {
                    hasPriorName = true;
                }
            }
            assertTrue(hasPriorName);
            assertEquals(markerAliasAB2_Pre + markerAliasesAB1_Pre + 1, markerAliasAB2_Post);

            //* suppliers
            int markerSupplierAB2_Post = markerSupplierQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0, markerSupplierQuery.setString("zdbID", zdbID1).list().size());
            // they acutally merge the suppliers into each other
            assertEquals(1, markerSupplierAB2_Pre);
            assertEquals(2, markerSupplierAB1_Size);
            assertEquals(2, markerSupplierAB2_Post);


            // * sources
            // * record attributions
            int recordAttributionAB2_Post = recordAttributionQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0, recordAttributionQuery.setString("zdbID", zdbID1).list().size());
            assertTrue(recordAttributionAB1_Size >= 20);
            assertTrue(recordAttributionAB2_Pre > recordAttributionAB1_Size); // just a good guess, should be > 20
            assertTrue(recordAttributionAB2_Post > recordAttributionAB2_Pre);
            assertTrue(recordAttributionAB2_Pre + recordAttributionAB1_Size > recordAttributionAB2_Post);

            // * expression experiments
            int expressionExperimentAB2_Post = expressionExperimentQuery.setString("zdbID", zdbIDToMergeInto).list().size();
            assertEquals(0, expressionExperimentQuery.setString("zdbID", zdbID1).list().size());
            // TODO: any validation here?
//            assertEquals(expressionExperimentAB2_Pre+expressionExperimentAB1_Size,expressionExperimentAB2_Post);

        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @SuppressWarnings("unchecked")
    private List<Publication> createPublications() {
        return HibernateUtil.currentSession().createCriteria(Publication.class)
            .setMaxResults(2).list();
    }


    @SuppressWarnings("unchecked")
    private List<FishExperiment> createGenotypeExperiments() {
        return HibernateUtil.currentSession().createCriteria(FishExperiment.class)
            .setMaxResults(2).list();
    }

    private List<Antibody> createAntibodies(Publication pub) {

        Person p = RepositoryFactory.getProfileRepository().getPersonByName("ndunn");
        MarkerType mt = RepositoryFactory.getMarkerRepository().getMarkerTypeByName(Marker.Type.ATB.toString());

        List<Antibody> antibodies = new ArrayList<Antibody>();

        Antibody antibodyA = new Antibody();
        antibodyA.setAbbreviation("antibodya");
        antibodyA.setName("antibodya");
        antibodyA.setOwner(p);
        antibodyA.setMarkerType(mt);
//        RepositoryFactory.getMarkerRepository().createMarker(antibodyA,pub);
        HibernateUtil.currentSession().save(antibodyA);


        HibernateUtil.currentSession().flush();

        Antibody antibodyB = new Antibody();
        antibodyB.setAbbreviation("antibodyb");
        antibodyB.setName("antibodyb");
        antibodyB.setOwner(p);
        antibodyB.setMarkerType(mt);
//        RepositoryFactory.getMarkerRepository().createMarker(antibodyB,pub);
        HibernateUtil.currentSession().save(antibodyB);

        antibodies.add(antibodyA);
        antibodies.add(antibodyB);

        HibernateUtil.currentSession().flush();

        HibernateUtil.currentSession().merge(antibodyA);
        HibernateUtil.currentSession().merge(antibodyB);

        return antibodies;
    }

    @SuppressWarnings("unchecked")
    private List<ExpressionAssay> createExpressionAssays() {

        return HibernateUtil.currentSession().createCriteria(ExpressionAssay.class)
            .setMaxResults(2).list();

    }

    /**
     * /**
     * Given antibodies A and B, where A will be merged into B and then deleted
     * <p/>
     * for all expression experiments on A:  EEa
     * if EEa not contained in antibody B: then
     * update the antibody on EEa to point to antibody B
     * else
     * if EEa matches as expression_experiment B on antibody B (EEb): then
     * for all expression_results in EEa: ERa
     * if ERa not contained in expression_results on EEb (ERb): then
     * update ERa to point to EEb
     * else
     * for all expression_result_figures in ERa: ERFa
     * if ERFa not contained in expression_results_figure on ERb (ERFb): then
     * update ERFa to point to ERb
     * else
     * nada
     * <p/>
     * delete A
     * <p/>
     * INIT:
     * A(EEA1,EEA2(ERA1,ERA2(FA1,FA2))
     * B(EEB1,EEB2(ERB1,ERB2(FB1,FB2))
     * <p/>
     * TEST:
     * Antibody A has ExpressionExperiments EEA*
     * Antibody B has ExpressionExperiments: EEB*
     * EEA1 not in EEB* -> gets moved
     * EEA2 matches EEB2:
     * EEA2.ERA1 not in EEB2.ERB*  -> gets moved
     * EEA2.ERA2 matches EEB2.ERB2:
     * EEA2.ERA2.FA1 not in EEA2.ERA2.FB* -> gets moved
     * EEA2.ERA2.FA2 matches EEA2.ERA2.FB2 -> nothing happens
     * <p/>
     * FINAL:
     * A(EEA2(ERA2(FA2))) // not moved
     * B(EEA1,EEB1,EEB2(ERA1,ERB1,ERB2(FA1,FB1,FB2))) // showing merged first
     */

}
