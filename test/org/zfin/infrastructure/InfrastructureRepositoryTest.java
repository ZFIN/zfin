package org.zfin.infrastructure;


import org.apache.commons.collections.CollectionUtils;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.ExternalNote;
import org.zfin.expression.ExpressionAssay;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;

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

public class InfrastructureRepositoryTest extends AbstractDatabaseTest {



    //@Test
    public void persistActiveData() {
        try {
            HibernateUtil.createTransaction();

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
            HibernateUtil.rollbackTransaction();
        }

    }

    @Test
    public void persistRecordAttribution() {

        try {
            HibernateUtil.createTransaction();
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
            HibernateUtil.rollbackTransaction();
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

    @Test
    public void externalNoteRecordAttribution() {

        String externalNoteZdbID = "ZDB-EXTNOTE-080424-1";
        ActiveData data = new ActiveData();
        data.setZdbID(externalNoteZdbID);

        String pubID = "ZDB-PUB-070210-20";
        ActiveSource source = new ActiveSource();
        source.setZdbID(pubID);

        RecordAttribution rec = getInfrastructureRepository().getRecordAttribution(data, source, RecordAttribution.SourceType.STANDARD);
        Assert.assertTrue(rec == null);

        PublicationAttribution record = new PublicationAttribution();
        record.setDataZdbID("externalNoteZdbID");
        Publication pub = new Publication();
        pub.setZdbID(pubID);
        record.setPublication(pub);
        rec = getInfrastructureRepository().getPublicationAttribution(record);
        Assert.assertTrue(rec == null);

    }

    @Test
    public void getExternalNote() {
        String externalNoteZdbID = "ZDB-EXTNOTE-080424-1";

        ExternalNote note = getInfrastructureRepository().getExternalNoteByID(externalNoteZdbID);

    }


    @Test
    public void getDataAliasesAttributions(){
        RepositoryFactory.getInfrastructureRepository().getDataAliasesAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ; 
    }

    @Test
    public void getOrthologueRecordAttributions(){
        RepositoryFactory.getInfrastructureRepository().getOrthologueRecordAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }


    @Test
    public void getMarkerFeatureRelationshipAttributions(){
        RepositoryFactory.getInfrastructureRepository().getMarkerFeatureRelationshipAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }


    @Test
    public void getMarkerGenotypeFeatureRelationshipAttributions(){
        RepositoryFactory.getInfrastructureRepository().getMarkerGenotypeFeatureRelationshipAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getFeatureGenotypeAttributions(){
        RepositoryFactory.getInfrastructureRepository().getFeatureGenotypeAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getGoRecordAttributions(){
        RepositoryFactory.getInfrastructureRepository().getGoRecordAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getDBLinkAttributions(){
        RepositoryFactory.getInfrastructureRepository().getDBLinkAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getDBLinkAssociatedToGeneAttributions(){
        RepositoryFactory.getInfrastructureRepository().getDBLinkAssociatedToGeneAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getFirstMarkerRelationshipAttributions(){
        RepositoryFactory.getInfrastructureRepository().getFirstMarkerRelationshipAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getSecondMarkerRelationshipAttributions(){
        RepositoryFactory.getInfrastructureRepository().getSecondMarkerRelationshipAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getMorpholinoRelatedMarkerAttributions(){
        RepositoryFactory.getInfrastructureRepository().getMorpholinoRelatedMarkerAttributions( "ZDB-GENE-990415-200" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getExpressionExperimentMarkerAttributionsForGene(){
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-990415-200") ;
        RepositoryFactory.getInfrastructureRepository().getExpressionExperimentMarkerAttributions( m , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getExpressionExperimentMarkerAttributionsForAntibody(){
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-ATB-081002-19") ;
        RepositoryFactory.getInfrastructureRepository().getExpressionExperimentMarkerAttributions( m , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getExpressionExperimentMarkerAttributionsForClone(){
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-CDNA-040425-3105") ;
        RepositoryFactory.getInfrastructureRepository().getExpressionExperimentMarkerAttributions( m , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getGenotypeExperimentRecordAttributions(){
        RepositoryFactory.getInfrastructureRepository().getGenotypeExperimentRecordAttributions( "ZDB-GENO-000405-1" , "ZDB-PUB-090324-13") ;
    }

    @Test
    public void getGenotypePhenotypeRecordAttributions(){
        RepositoryFactory.getInfrastructureRepository().getGenotypePhenotypeRecordAttributions( "ZDB-GENO-000405-1" , "ZDB-PUB-090324-13") ;
    }
}


