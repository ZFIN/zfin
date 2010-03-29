package org.zfin.infrastructure;


import org.apache.commons.collections.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.expression.ExpressionAssay;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.TermRelationship;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 * Class InfrastructureRepositoryTest.
 */

public class InfrastructureRepositoryTest {

    private static Session session;

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        session = HibernateUtil.currentSession();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    //@Test

    public void persistActiveData() {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            String testZdbID = "ZDB-GENE-123";
            ActiveData testActiveData = getInfrastructureRepository().getActiveData(testZdbID);
            assertNull("ActiveData not found prior to insert", testActiveData);
            getInfrastructureRepository().insertActiveData(testZdbID);
            testActiveData = getInfrastructureRepository().getActiveData(testZdbID);
            assertNotNull("ActiveData found after insert", testActiveData);
            getInfrastructureRepository().deleteActiveData(testActiveData);
            testActiveData = getInfrastructureRepository().getActiveData(testZdbID);
            assertNull("ActiveData found after delete", testActiveData);
        } catch (HibernateException e) {
            fail("failed");
            e.printStackTrace();
        } finally {
            tx.rollback();
        }

    }

    @Test
    public void persistRecordAttribution() {

        try {
            session.beginTransaction();
            String dataZdbID = "ZDB-DALIAS-uuiouy";
            String sourceZdbID = "ZDB-PUB-000104-1";
            getInfrastructureRepository().insertActiveData(dataZdbID);
            // should already exist in active source as a valid pub, so no need to insert
            RecordAttribution attribute = getInfrastructureRepository().getRecordAttribution(dataZdbID, sourceZdbID, null);
            assertNull("RecordAttribution not found prior to insert", attribute);
            getInfrastructureRepository().insertRecordAttribution(dataZdbID, sourceZdbID);
            attribute = getInfrastructureRepository().getRecordAttribution(dataZdbID, sourceZdbID, null);
            assertNotNull("RecordAttribution found after insert", attribute);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

    @Test
    public void allMapNames() {
        String string = "pdx";
        List<AllNamesFastSearch> all = getInfrastructureRepository().getAllNameMarkerMatches(string);
        assertNotNull(all);
    }

    @Test
    public void allMapNamesGenes() {
        String string = "pdx";
        MarkerType type = getMarkerRepository().getMarkerTypeByName(Marker.Type.GENE.toString());
        List<AllMarkerNamesFastSearch> all = getInfrastructureRepository().getAllNameMarkerMatches(string, type);
        assertNotNull(all);
    }

    @Test
    public void replacementZDB() {
        String replacedZdbID = "ZDB-ANAT-010921-497";
        ReplacementZdbID replacementZdbID = getInfrastructureRepository().getReplacementZdbId(replacedZdbID);
        assertNotNull(replacementZdbID);

        assertEquals("ZDB-ANAT-011113-37", replacementZdbID.getReplacementZdbID());
    }

    @Test
    public void dataAliasAbbrev() {
        String name = "acerebellar";
        List<String> list = getInfrastructureRepository().getDataAliasesWithAbbreviation(name);
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals("fgf8a", list.get(0));
    }

    @Test
    public void anatomyTokens() {
        String name = "presumptive";
        List<String> list = getInfrastructureRepository().getAnatomyTokens(name);
        assertNotNull(list);
        assertTrue(list.size() > 10);
    }

    @Test
    public void allAssays() {
        List<ExpressionAssay> list = getInfrastructureRepository().getAllAssays();
        assertTrue(CollectionUtils.isNotEmpty(list));
    }

    @Test
    public void getDataAliasGroup() {

        List<DataAliasGroup> groups = getInfrastructureRepository().getAllDataAliasGroups();
        assertNotNull(groups);
        assertTrue(groups.size() > 3);
    }

    @Test
    public void getGoCcTermsByQueryString() {
        String queryString = "mito";
        List<Ontology> ontologies = new ArrayList<Ontology>(1);
        ontologies.add(Ontology.GO_CC);
        List<GenericTerm> groups = getInfrastructureRepository().getTermsByName(queryString, ontologies);
        assertNotNull(groups);
        assertTrue(groups.size() > 10);
    }

    @Test
    public void getGoCcTermsSynonymByQueryString() {
        String queryString = "orga";
        List<Ontology> ontologies = new ArrayList<Ontology>(1);
        ontologies.add(Ontology.GO_CC);
        List<GenericTerm> groups = getInfrastructureRepository().getTermsByName(queryString, ontologies);
        assertNotNull(groups);
        assertTrue(groups.size() > 0);
    }

    @Test
    public void getGoCcTermSynonymsByQueryString() {
        String queryString = "mito";
        List<GenericTerm> groups = getInfrastructureRepository().getTermsBySynonymName(queryString, Ontology.GO_CC);
        assertNotNull(groups);
        assertTrue(groups.size() > 1);
    }

    @Test
    public void getGoCcTerm() {
        String queryString = "mitochondrion";
        GenericTerm term = getInfrastructureRepository().getTermByName(queryString, Ontology.GO_CC);
        assertNotNull(term);
        List<TermRelationship> relationships = term.getRelatedTerms();
        assertNotNull(relationships);
    }


    @Test
    public void getDataAliasGO() {
        //mitochondrial ATP synthesis coupled electron transport
        String alias = "organelle atp synthesis coupled electron transport";
        List<DataAlias> groups = getInfrastructureRepository().getDataAliases(alias);
        assertNotNull(groups);
        assertTrue(!groups.isEmpty());
    }

    @Test
    public void getSingleDataAliasGroup() {
        String name = DataAliasGroup.Group.ALIAS.toString();
        DataAliasGroup group = getInfrastructureRepository().getDataAliasGroupByName(name);
        assertNotNull(group);
    }

    @Test
    public void getGenesForStandardAttribution(){
        
    }

    @Test
    public void getQualityRootTerm() {
        GenericTerm rootTerm = getInfrastructureRepository().getRootTerm(Ontology.QUALITY.getOntologyName());
        assertNotNull(rootTerm);
    }

}


