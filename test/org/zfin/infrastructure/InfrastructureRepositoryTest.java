package org.zfin.infrastructure;


import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.ExternalNote;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.database.UnloadInfo;
import org.zfin.datatransfer.microarray.MicroarrayWebserviceJob;
import org.zfin.expression.ExpressionAssay;
import org.zfin.expression.Figure;
import org.zfin.expression.service.ExpressionService;
import org.zfin.feature.Feature;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.profile.Company;
import org.zfin.profile.Lab;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DatabaseJdbcStatement;
import org.zfin.util.DbScriptFileParser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getAntibodyRepository;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

/**
 * Class InfrastructureRepositoryTest.
 */

public class InfrastructureRepositoryTest extends AbstractDatabaseTest {

    private InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    private Logger logger = Logger.getLogger(InfrastructureRepositoryTest.class);

    @Test
    public void persistActiveData() {
        try {
            HibernateUtil.createTransaction();

            String testZdbID = "ZDB-GENE-123";
            ActiveData testActiveData = infrastructureRepository.getActiveData(testZdbID);
            assertNull("ActiveData not found prior to insert", testActiveData);
            infrastructureRepository.insertActiveData(testZdbID);
            testActiveData = infrastructureRepository.getActiveData(testZdbID);
            assertNotNull("ActiveData found after insert", testActiveData);
            infrastructureRepository.deleteActiveData(testActiveData);
            testActiveData = infrastructureRepository.getActiveData(testZdbID);
            assertNull("ActiveData found after delete", testActiveData);
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
            infrastructureRepository.insertActiveData(dataZdbID);
            // should already exist in active source as a valid pub, so no need to insert
            RecordAttribution attribute = infrastructureRepository.getRecordAttribution(dataZdbID, sourceZdbID, null);
            assertNull("RecordAttribution not found prior to insert", attribute);
            infrastructureRepository.insertRecordAttribution(dataZdbID, sourceZdbID);
            attribute = infrastructureRepository.getRecordAttribution(dataZdbID, sourceZdbID, null);
            assertNotNull("RecordAttribution found after insert", attribute);
        } finally {
            // rollback on success or exception to leave no new records in the database
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void allMapNames() {
        String string = "pdx";
        List<AllNamesFastSearch> all = infrastructureRepository.getAllNameMarkerMatches(string);
        assertNotNull(all);
    }

    @Test
    public void allMapNamesGenes() {
        String string = "pdx";
        MarkerType type = getMarkerRepository().getMarkerTypeByName(Marker.Type.GENE.toString());
        List<AllMarkerNamesFastSearch> all = infrastructureRepository.getAllNameMarkerMatches(string, type);
        assertNotNull(all);
    }

    @Test
    public void replacementZDB() {
        String replacedZdbID = "ZDB-ANAT-010921-532";
        ReplacementZdbID replacementZdbID = infrastructureRepository.getReplacementZdbId(replacedZdbID);
        assertNotNull(replacementZdbID);

        assertEquals("ZDB-TERM-100331-100", replacementZdbID.getReplacementZdbID());
    }

    @Test
    public void dataAliasAbbrev() {
        String name = "acerebellar";
        List<String> list = infrastructureRepository.getDataAliasesWithAbbreviation(name);
        assertNotNull(list);
        assertTrue(list.size() == 1);
        assertEquals("fgf8a", list.get(0));
    }

    @Test
    public void allAssays() {
        List<ExpressionAssay> list = infrastructureRepository.getAllAssays();
        assertTrue(CollectionUtils.isNotEmpty(list));
    }

    @Test
    public void getDataAliasGroup() {

        List<DataAliasGroup> groups = infrastructureRepository.getAllDataAliasGroups();
        assertNotNull(groups);
        assertTrue(groups.size() > 3);
    }

    @Test
    public void getGoCcTermsByQueryString() {
        String queryString = "mito";
        List<Ontology> ontologies = new ArrayList<Ontology>(1);
        ontologies.add(Ontology.GO_CC);
        List<GenericTerm> groups = infrastructureRepository.getTermsByName(queryString, ontologies);
        assertNotNull(groups);
        assertTrue(groups.size() > 10);
    }

    @Test
    public void getGoCcTermsSynonymByQueryString() {
        String queryString = "orga";
        List<Ontology> ontologies = new ArrayList<Ontology>(1);
        ontologies.add(Ontology.GO_CC);
        List<GenericTerm> groups = infrastructureRepository.getTermsByName(queryString, ontologies);
        assertNotNull(groups);
        assertTrue(groups.size() > 0);
    }

    @Test
    public void getGoCcTermSynonymsByQueryString() {
        String queryString = "mito";
        List<GenericTerm> groups = infrastructureRepository.getTermsBySynonymName(queryString, Ontology.GO_CC);
        assertNotNull(groups);
        assertTrue(groups.size() > 1);
    }

    @Test
    public void getGoCcTerm() {
        String queryString = "mitochondrion";
        GenericTerm term = infrastructureRepository.getTermByName(queryString, Ontology.GO_CC);
        assertNotNull(term);
    }


    @Test
    public void getDataAliasGO() {
        //mitochondrial ATP synthesis coupled electron transport
        String alias = "organelle atp synthesis coupled electron transport";
        List<DataAlias> groups = infrastructureRepository.getDataAliases(alias);
        assertNotNull(groups);
        assertTrue(!groups.isEmpty());
    }

    @Test
    public void getSingleDataAliasGroup() {
        String name = DataAliasGroup.Group.ALIAS.toString();
        DataAliasGroup group = infrastructureRepository.getDataAliasGroupByName(name);
        assertNotNull(group);
    }


    @Test
    public void getQualityRootTerm() {
        GenericTerm rootTerm = infrastructureRepository.getRootTerm(Ontology.QUALITY.getOntologyName());
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

        RecordAttribution rec = infrastructureRepository.getRecordAttribution(data, source, RecordAttribution.SourceType.STANDARD);
        Assert.assertTrue(rec == null);

        PublicationAttribution record = new PublicationAttribution();
        record.setDataZdbID("externalNoteZdbID");
        Publication pub = new Publication();
        pub.setZdbID(pubID);
        record.setPublication(pub);
        rec = infrastructureRepository.getPublicationAttribution(record);
        Assert.assertTrue(rec == null);

    }

    @Test
    public void getExternalNote() {
        String externalNoteZdbID = "ZDB-EXTNOTE-080424-1";

        infrastructureRepository.getExternalNoteByID(externalNoteZdbID);

    }


    @Test
    public void getDataAliasesAttributions() {
        infrastructureRepository.getDataAliasesAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getOrthologRecordAttributions() {
        infrastructureRepository.getOrthologRecordAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }


    @Test
    public void getMarkerFeatureRelationshipAttributions() {
        infrastructureRepository.getMarkerFeatureRelationshipAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }


    @Test
    public void getMarkerGenotypeFeatureRelationshipAttributions() {
        infrastructureRepository.getMarkerGenotypeFeatureRelationshipAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getFeatureGenotypeAttributions() {
        infrastructureRepository.getFeatureGenotypeAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getGoRecordAttributions() {
        infrastructureRepository.getGoRecordAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getDBLinkAttributions() {
        infrastructureRepository.getDBLinkAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getDBLinkAssociatedToGeneAttributions() {
        infrastructureRepository.getDBLinkAssociatedToGeneAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getFirstMarkerRelationshipAttributions() {
        infrastructureRepository.getFirstMarkerRelationshipAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getSecondMarkerRelationshipAttributions() {
        infrastructureRepository.getSecondMarkerRelationshipAttributions("ZDB-GENE-990415-200", "ZDB-PUB-090324-13");
    }

    @Test
    public void getExpressionExperimentMarkerAttributionsForGene() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-GENE-990415-200");
        infrastructureRepository.getExpressionExperimentMarkerAttributions(m, "ZDB-PUB-090324-13");
    }

    @Test
    public void getExpressionExperimentMarkerAttributionsForAntibody() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-ATB-081002-19");
        infrastructureRepository.getExpressionExperimentMarkerAttributions(m, "ZDB-PUB-090324-13");
    }

    @Test
    public void getExpressionExperimentMarkerAttributionsForClone() {
        Marker m = getMarkerRepository().getMarkerByID("ZDB-CDNA-040425-3105");
        infrastructureRepository.getExpressionExperimentMarkerAttributions(m, "ZDB-PUB-090324-13");
    }

    @Test
    public void getGenotypeExperimentRecordAttributions() {
        infrastructureRepository.getGenotypeExperimentRecordAttributions("ZDB-GENO-000405-1", "ZDB-PUB-090324-13");
    }

    @Test
    public void getGenotypePhenotypeRecordAttributions() {
        infrastructureRepository.getGenotypePhenotypeRecordAttributions("ZDB-GENO-000405-1", "ZDB-PUB-090324-13");
    }

    @Test
    public void callJdbcStatement() {
        String query = "create temp table tmp_syndef (namespace varchar(30), type varchar(30), def varchar(100), scoper varchar(30), syntypedefs varchar(20))" +
                "with no log;";
        DatabaseJdbcStatement statement = new DatabaseJdbcStatement();
        statement.addQueryPart(query);
        infrastructureRepository.executeJdbcStatement(statement);
    }

    @Test
    public void runDbScript() {
        String fileName = "test//dbTestScript.sql";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");

        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();

        HibernateUtil.createTransaction();
        try {
            infrastructureRepository.executeJdbcStatement(queries.get(0));
        } finally {
            HibernateUtil.rollbackTransaction();
        }

    }

    @Test
    public void runDynamicDbScript() {
        String fileName = "test//dbTestScriptPerson.sqlj";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");

        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        List<List<String>> list = null;
        HibernateUtil.createTransaction();
        try {
            list = infrastructureRepository.executeNativeDynamicQuery(queries.get(0));
        } finally {
            HibernateUtil.rollbackTransaction();
        }

        assertNotNull(list);
    }

    @Test
    public void runDynamicDbScript2() {
        String fileName = "test//dbTestScript.sqlj";
        File file = new File(fileName);
        if (!file.exists())
            fail("Could not find script file");

        DbScriptFileParser parser = new DbScriptFileParser(file);
        List<DatabaseJdbcStatement> queries = parser.parseFile();
        List<List<String>> list = null;
        HibernateUtil.createTransaction();
        try {
            list = infrastructureRepository.executeNativeDynamicQuery(queries.get(0));
        } finally {
            HibernateUtil.rollbackTransaction();
        }

        assertNotNull(list);
    }

    @Test
    public void getFirst10Genotypes() {
        List<String> allGenotypes = infrastructureRepository.getAllEntities(Genotype.class, "zdbID", 10);
        assertNotNull(allGenotypes);
        assertEquals(10, allGenotypes.size());
    }

    @Test
    public void getFirst10Features() {
        List<String> allFeatures = infrastructureRepository.getAllEntities(Feature.class, "zdbID", 10);
        assertNotNull(allFeatures);
        assertEquals(10, allFeatures.size());
    }

    @Test
    public void getFirst10Figures() {
        List<String> allFeatures = infrastructureRepository.getAllEntities(Figure.class, "zdbID", 10);
        assertNotNull(allFeatures);
        assertEquals(10, allFeatures.size());
    }

    @Test
    public void getFirst10Labs() {
        List<String> allCompanies = infrastructureRepository.getAllEntities(Lab.class, "zdbID", 10);
        assertNotNull(allCompanies);
        assertEquals(10, allCompanies.size());
    }

    @Test
    public void getFirst10Companies() {
        List<String> allCompanies = infrastructureRepository.getAllEntities(Company.class, "zdbID", 10);
        assertNotNull(allCompanies);
        assertEquals(10, allCompanies.size());
    }

    @Test
    public void getFirst10Persons() {
        List<String> allPersons = infrastructureRepository.getAllEntities(Person.class, "zdbID", 10);
        assertNotNull(allPersons);
        assertEquals(10, allPersons.size());
    }

    @Test
    public void getFirst10Terms() {
        List<String> allTerms = infrastructureRepository.getAllEntities(GenericTerm.class, "oboID", 10);
        assertNotNull(allTerms);
        assertEquals(10, allTerms.size());
    }

    @Test
    public void getFirst10markerGoEvidenceCodes() {
        List<String> allMarkerGoEvidences = infrastructureRepository.getAllEntities(MarkerGoTermEvidence.class, "marker.zdbID", 10);
        assertNotNull(allMarkerGoEvidences);
        assertEquals(10, allMarkerGoEvidences.size());
    }

    @Test
    @Ignore("for performance; performs no assertions")
    public void getUpdatesFlagPerformance() {

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
//            ZdbFlag flag = infrastructureRepository.getUpdatesFlag();
            boolean flag = infrastructureRepository.getDisableUpdatesFlag();
        }

        long endTime = System.currentTimeMillis();
        logger.info("total time: " + (endTime - startTime) / 1000.0f);
    }

    @Test
    public void getExternalOrthologyNotes() {
        List<String> notes = infrastructureRepository.getExternalOrthologyNoteStrings("ZDB-GENE-030131-2333");
        assertEquals(1, notes.size());
    }

    @Test
    public void getExternalNotes() {
        Antibody antibody = getAntibodyRepository().getAntibodyByID("ZDB-ATB-081002-19");
        Set<AntibodyExternalNote> externalNotes = antibody.getExternalNotes();
        List<ExternalNote> notes = new ArrayList<>();
        notes.addAll(externalNotes);
        assertEquals(1, notes.size());

        notes.clear();
        notes.addAll(getAntibodyRepository().getAntibodyByID("ZDB-ATB-081006-1").getExternalNotes());
        assertEquals(2, notes.size());
        // these are ordered by publication date!
        assertTrue(notes.get(0).getNote().startsWith("Labels both fast and slow"));
        assertTrue(notes.get(1).getNote().startsWith("labels slow and fast"));
    }

    @Test
    public void getPublicationAttributionZdbIdsForType() {
        List<String> markerTypes = infrastructureRepository.getPublicationAttributionZdbIdsForType(ExpressionService.MICROARRAY_PUB, Marker.Type.GENEP);
        assertNotNull(markerTypes);

    }

    @Test
    public void removeAttributionsNotFound() {
        try {
            HibernateUtil.createTransaction();
            infrastructureRepository.deleteRecordAttributionForPub(MicroarrayWebserviceJob.MICROARRAY_PUB);
            Set<String> datas = new HashSet<String>();
            datas.add("ZDB-GENE-000607-47");
            datas.add("ZDB-GENE-000607-71");
            datas.add("ZDB-GENE-030131-10076");
            for (String data : datas) {
                infrastructureRepository.insertRecordAttribution(data, MicroarrayWebserviceJob.MICROARRAY_PUB);
            }
            int removed = infrastructureRepository.removeAttributionsNotFound(datas, MicroarrayWebserviceJob.MICROARRAY_PUB);
            assertEquals(3, removed);
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void addAttributionsNotFound() {
        try {
            HibernateUtil.createTransaction();
            infrastructureRepository.deleteRecordAttributionForPub(MicroarrayWebserviceJob.MICROARRAY_PUB);
            Set<String> datas = new HashSet<String>();
            datas.add("ZDB-GENE-000607-47");
            datas.add("ZDB-GENE-000607-71");
            datas.add("ZDB-GENE-030131-10076");
            int added = infrastructureRepository.addAttributionsNotFound(datas, MicroarrayWebserviceJob.MICROARRAY_PUB);
            assertEquals(datas.size(), added);
            List<String> numMarkersAttributed = infrastructureRepository.getPublicationAttributionsForPub(MicroarrayWebserviceJob.MICROARRAY_PUB);
            assertEquals(datas.size(), numMarkersAttributed.size());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }


    @Test
    public void getPublicationAttributionsForPub() {
        List<String> markersForPub = infrastructureRepository.getPublicationAttributionsForPub(MicroarrayWebserviceJob.MICROARRAY_PUB);
        assertNotNull(markersForPub);
    }

    @Test
    public void hasStandardPublicationAttribution() {
        assertFalse(infrastructureRepository.hasStandardPublicationAttribution("ZDB-SSLP-000426-106", MicroarrayWebserviceJob.MICROARRAY_PUB));
        assertTrue(infrastructureRepository.hasStandardPublicationAttribution("ZDB-GENE-030131-9286", MicroarrayWebserviceJob.MICROARRAY_PUB));
        // this keeps popping back and for as a ZGC .. .will remove
//        assertTrue(infrastructureRepository.hasStandardPublicationAttribution("ZDB-GENE-041008-244", MicroarrayWebserviceJob.MICROARRAY_PUB));

    }

    @Test
    public void hasStandardPublicationAttributionForRelatedMarkers() {
        assertFalse(infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers("ZDB-SSLP-000426-106", MicroarrayWebserviceJob.MICROARRAY_PUB));
        assertTrue(infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers("ZDB-GENE-030131-9286", MicroarrayWebserviceJob.MICROARRAY_PUB));
//        assertTrue(infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers("ZDB-GENE-041008-244", MicroarrayWebserviceJob.MICROARRAY_PUB));

        // on clone, but not the gene
        assertTrue(infrastructureRepository.hasStandardPublicationAttributionForRelatedMarkers("ZDB-GENE-031118-138", MicroarrayWebserviceJob.MICROARRAY_PUB));
    }

    @Test
    public void getCollectionOfReplacedData() {
        List<ReplacementZdbID> replacedGeneIds = RepositoryFactory.getInfrastructureRepository().getReplacedZdbIDsByType(ActiveData.Type.GENE);
        assertNotNull(replacedGeneIds);
        assertTrue(replacedGeneIds.size() > 10);
    }

    @Test
    public void getTermReferences() {
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID("DOID:5082");
        List<Publication> paginationResult = infrastructureRepository.getTermReferences(term, "author");
        assertNotNull(paginationResult);
    }

    @Test
    public void getDistinctPublicationsByData() {
        long count = infrastructureRepository.getDistinctPublicationsByData("ZDB-GENO-100511-2");
        assertThat((int) (long) count, greaterThan(1));
    }

    @Test
    public void getunloadDate() {
        UnloadInfo info = infrastructureRepository.getUnloadInfo();
        assertNotNull(info);
    }


}


