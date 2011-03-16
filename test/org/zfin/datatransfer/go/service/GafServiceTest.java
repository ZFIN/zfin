package org.zfin.datatransfer.go.service;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.*;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.mutant.GafOrganization;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Tests GafService methods
 */
public class GafServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(GafServiceTest.class);
    private GafService gafService = new GafService();
    private GafParser gafParser = new GafParser();

    // this ia pub with no go evidence annotations and is closed, so none will be added
    private final String DEFAULT_TEST_ACCESSION = "PMID:10630700"; // "ZDB-PUB-000118-16"
    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();


    @Test
    public void loadSmallOntologyTest() {
        Term t = ontologyRepository.getTermByName("cilium", Ontology.GO_CC);
        assertNotNull(t);
    }

    /**
     * All annotations should have this pub so subsequent loads to break the code.
     *
     * @param gafEntries
     */
    private void makeTestPub(List<GafEntry> gafEntries) {
        for (GafEntry gafEntry : gafEntries) {
            gafEntry.setPubmedId(DEFAULT_TEST_ACCESSION);
        }
    }

    @Test
    public void gafServiceTestInferences() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_inferencetest");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        assertEquals(0, gafJobData.getErrors().size());
        assertEquals(0, gafJobData.getExistingEntries().size());
        assertEquals(1, gafJobData.getNewEntries().size());
        assertEquals(0, gafJobData.getRemovedEntries().size());

        assertTrue(gafJobData.getNewEntries().iterator().next().getInferredFrom().iterator().next().getInferredFrom().equals("UniProtKB:Q9NXR7"));
    }


    @Test
    public void gafServiceBadAdd() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_badadd");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(3, gafEntries.size());
        makeTestPub(gafEntries);


        GafJobData gafJobData = new GafJobData();
        gafService.processGoaGafEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries());
        logger.debug("errors: " + gafJobData.getErrors());

        assertEquals(0, gafJobData.getRemovedEntries().size());
        assertEquals(3, gafJobData.getNewEntries().size());
        assertEquals(0, gafJobData.getErrors().size());

        Iterator<MarkerGoTermEvidence> iter = gafJobData.getNewEntries().iterator();

        assertTrue(iter.next().getInferredFrom().iterator().next().getInferredFrom().equals(("UniProtKB:Q9UBQ5")));
        assertTrue(iter.next().getInferredFrom().iterator().next().getInferredFrom().equals(("UniProtKB:Q9UBQ5")));
        assertTrue(iter.next().getInferredFrom().iterator().next().getInferredFrom().equals(("UniProtKB:Q9UBQ5")));
    }

    @Test
    public void badGafEntry() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_badentry");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(13, gafEntries.size()); // 17 - 4 = 13
        makeTestPub(gafEntries);


        GafJobData gafJobData = new GafJobData();
        gafService.processGoaGafEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());
        logger.debug("removed: " + gafJobData.getRemovedEntries().size());

        assertEquals(0, gafJobData.getRemovedEntries().size());
        assertEquals(13, gafJobData.getNewEntries().size());
        assertEquals(0, gafJobData.getErrors().size());
    }

    @Test
    public void alreadyRanOnce() throws Exception {
//        File file = new File("test/goa_go/gene_association.goa_zebrafish_full");
        File file = new File("test/goa_go/gene_association.goa_zebrafish_inferencetest");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafReport1);

            gafReport2 = new GafJobData();
            gafService.processGoaGafEntries(gafEntries, gafReport2);
        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }

        assertEquals(1, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        assertEquals("UniProtKB:Q9NXR7", gafReport1.getNewEntries().iterator().next().getInferredFrom().iterator().next().getInferredFrom());

        assertEquals(0, gafReport2.getNewEntries().size());
        assertEquals(0, gafReport2.getErrors().size());
        assertEquals(1, gafReport2.getExistingEntries().size());
        assertEquals(0, gafReport2.getRemovedEntries().size());
    }

    @Test
    public void alreadyRanOnce_2() throws Exception {
//        File file = new File("test/goa_go/gene_association.goa_zebrafish_full");
        File file = new File("test/goa_go/gene_association.goa_zebrafish_badadd");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(3, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = new GafJobData();
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafReport1);

            gafService.processGoaGafEntries(gafEntries, gafReport2);
        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }


        assertEquals(3, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        assertEquals(0, gafReport2.getNewEntries().size());
        assertEquals(3, gafReport2.getExistingEntries().size());
        assertEquals(0, gafReport2.getErrors().size());
        assertEquals(0, gafReport2.getRemovedEntries().size());
    }

    // tests null inferences and redundant entries
    @Test
    public void findDupeInferences() throws Exception {
//        File file = new File("test/goa_go/gene_association.goa_zebrafish_full");
        File file = new File("test/goa_go/gene_association.goa_zebrafish_dupeinference");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(3, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafReport1);

            gafReport2 = new GafJobData();
            gafService.processGoaGafEntries(gafEntries, gafReport2);

            logger.debug("summary: " + gafReport2.toString());
            logger.debug("entries: " + gafReport2.getNewEntries());
            logger.debug("existing: " + gafReport2.getExistingEntries());
            logger.debug("errors: " + gafReport2.getErrors());
        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }


        assertEquals(3, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        assertEquals(0, gafReport2.getNewEntries().size());
        assertEquals(3, gafReport2.getExistingEntries().size());
        assertEquals(0, gafReport2.getErrors().size());
        assertEquals(0, gafReport2.getRemovedEntries().size());
    }

    // valid additions, but duplicate within the gaf file
    @Test
    public void knowDupesWithAnAdd() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_duplicateentries");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(15, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        for (MarkerGoTermEvidence markerGoTermEvidence : gafReport1.getNewEntries()) {
            logger.debug(markerGoTermEvidence.hashCode()
                    + " - "
                    + " " + markerGoTermEvidence.getZdbID()
                    + " " + markerGoTermEvidence.getMarker().getZdbID()
                    + " " + markerGoTermEvidence.getEvidenceCode().getCode()
                    + " " + markerGoTermEvidence.getFlag()
                    + " " + markerGoTermEvidence.getSource().getZdbID()
                    + " " + markerGoTermEvidence.getGoTerm().getOboID()
                    + " " + markerGoTermEvidence.getInferencesAsString().iterator().next()
            );
        }

        assertEquals(6, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(9, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

    }


    // tests null inferences and redundant entries
    @Test
    public void dontAddDupes() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_redundantadd");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(3, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafReport1);

            gafReport2 = new GafJobData();
            gafService.processGoaGafEntries(gafEntries, gafReport2);

            logger.debug("summary: " + gafReport2.toString());
            logger.debug("entries: " + gafReport2.getNewEntries());
            logger.debug("existing: " + gafReport2.getExistingEntries());
            logger.debug("errors: " + gafReport2.getErrors());
        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }


        assertEquals(2, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(1, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        assertEquals(0, gafReport2.getNewEntries().size());
        assertEquals(3, gafReport2.getExistingEntries().size());
        assertEquals(0, gafReport2.getErrors().size());
        assertEquals(0, gafReport2.getRemovedEntries().size());
    }

    @Test
    public void mapQualifiers() throws Exception {
//        File file = new File("test/goa_go/gene_association.goa_zebrafish_full");
        File file = new File("test/goa_go/gene_association.goa_zebrafish_qualifiers");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(4, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(4, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

    }

    @Test
    public void colocalizeOnGoCC() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_gocc_colocalize");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(2, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();
        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertEquals(1, gafReport1.getErrors().size());
        assertEquals(1, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());
    }

    @Test
    public void igiRemap() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_igi_remap");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(2, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();
        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());


        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(2, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        Iterator<MarkerGoTermEvidence> iter = gafReport1.getNewEntries().iterator();

        assertEquals("ZFIN:ZDB-GENE-000201-18", iter.next().getInferredFrom().iterator().next().getInferredFrom());
        assertEquals("ZFIN:ZDB-GENE-980526-290", iter.next().getInferredFrom().iterator().next().getInferredFrom());
    }

    @Test
    public void multipleAddExists() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_otherexists");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(8, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafReport1);

            gafReport2 = new GafJobData();
            gafService.processGoaGafEntries(gafEntries, gafReport2);

            logger.debug("summary: " + gafReport2.toString());
            logger.debug("entries: " + gafReport2.getNewEntries());
            logger.debug("existing: " + gafReport2.getExistingEntries());
            logger.debug("errors: " + gafReport2.getErrors());
        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }

        assertEquals(8, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        assertEquals(0, gafReport2.getNewEntries().size());
        assertEquals(8, gafReport2.getExistingEntries().size());
        assertEquals(0, gafReport2.getErrors().size());
        assertEquals(0, gafReport2.getRemovedEntries().size());

    }


    @Test
    public void ndReplace() {
        String hql = " " +
                " select ev from MarkerGoTermEvidence ev  " +
                " where ev.evidenceCode.code = :code  " +
                "  ";
        MarkerGoTermEvidence existingEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery(hql)
                .setString("code", GoEvidenceCodeEnum.ND.name())
                .setMaxResults(1)
                .uniqueResult();

        MarkerGoTermEvidence evidence = new MarkerGoTermEvidence();
        evidence.setMarker(existingEvidence.getMarker());
        evidence.setSource(existingEvidence.getSource());
        evidence.setFlag(existingEvidence.getFlag());
        evidence.setGafOrganization(existingEvidence.getGafOrganization());
        evidence.setGoTerm(existingEvidence.getGoTerm());
        evidence.setCreatedBy(existingEvidence.getCreatedBy());
        evidence.setCreatedWhen(existingEvidence.getCreatedWhen());
        evidence.setEvidenceCode(existingEvidence.getEvidenceCode());
        evidence.setModifiedBy(existingEvidence.getModifiedBy());
        evidence.setModifiedWhen(existingEvidence.getModifiedWhen());
        evidence.setOrganizationCreatedBy(existingEvidence.getOrganizationCreatedBy());
        evidence.setExternalLoadDate(existingEvidence.getExternalLoadDate());


        GafJobData gafJobData = new GafJobData();
        gafJobData.addNewEntry(evidence);
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafJobData);
        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }

        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries());
        logger.debug("existing: " + gafJobData.getExistingEntries());
        logger.debug("errors: " + gafJobData.getErrors());

        assertEquals(1, gafJobData.getNewEntries().size());
        assertEquals(0, gafJobData.getExistingEntries().size());
        assertEquals(0, gafJobData.getErrors().size());
        assertEquals(1, gafJobData.getRemovedEntries().size());

    }


    @Test
    public void doNotDeleteAdded() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_deleteadded");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(1, gafEntries.size());
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertEquals(1, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

        GafJobData gafReport2 = null;
        try {
            HibernateUtil.createTransaction();
            gafService.addAnnotations(gafReport1);

            gafReport2 = new GafJobData();
            gafService.processGoaGafEntries(gafEntries, gafReport2);
            GafOrganization gafOrganization = RepositoryFactory.getMarkerGoTermEvidenceRepository().getGafOrganization(GafOrganization.OrganizationEnum.GOA);
            Set<String> existingZfinZdbIDs = new TreeSet<String>(RepositoryFactory.getMarkerGoTermEvidenceRepository().getEvidencesForGafOrganization(gafOrganization));

            // is it in the database currently
            String newZdbID = gafReport1.getNewEntries().iterator().next().getZdbID();
            assertTrue(existingZfinZdbIDs.contains(newZdbID));

            // is it one of the processed existing entries
            assertTrue(gafReport2.getExistingEntries().contains(new GafJobEntry(newZdbID)));


            Collection<String> outdatedEntries = gafService.findOutdatedEntries(gafReport2, gafOrganization);

            assertFalse(outdatedEntries.contains(newZdbID));


        } catch (GafValidationError gafValidationError) {
            fail(gafValidationError.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }

        logger.debug("summary: " + gafReport2.toString());
        logger.debug("entries: " + gafReport2.getNewEntries());
        logger.debug("existing: " + gafReport2.getExistingEntries());
        logger.debug("errors: " + gafReport2.getErrors());

        assertEquals(0, gafReport2.getNewEntries().size());
        assertEquals(1, gafReport2.getExistingEntries().size());
        assertEquals(0, gafReport2.getErrors().size());
        // never processed this
//        assertEquals(0, gafReport2.getRemovedEntries().size());
    }

    @Test
    public void alreadyExistsComparesInference() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_betterinference");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(2, gafEntries.size());

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertEquals(0, gafReport1.getNewEntries().size());
        assertEquals(2, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());

    }

    @Test
    public void evaluateNewIeaGoRefs() throws Exception {

        File file = new File("test/goa_go/gene_association.goa_zebrafish_new_iea");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(4, gafEntries.size());
        // they need to be IEA pubs, so we don't want them to be a test pub
//        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertEquals(3, gafReport1.getNewEntries().size());
        assertEquals(0, gafReport1.getExistingEntries().size());
        assertEquals(1, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void isMoreSpecificGo() throws Exception {

        MarkerGoTermEvidence existingEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession()
                .createCriteria(MarkerGoTermEvidence.class)
                .setMaxResults(1)
                .uniqueResult();

        Term t = ontologyRepository.getTermByName("cilium assembly", Ontology.GO_BP);
        assertNotNull(t);
        existingEvidence.setGoTerm((GenericTerm) t);

        MarkerGoTermEvidence newEvidence = new MarkerGoTermEvidence();

        newEvidence.setMarker(existingEvidence.getMarker());
        newEvidence.setSource(existingEvidence.getSource());
        newEvidence.setFlag(existingEvidence.getFlag());
        newEvidence.setGafOrganization(existingEvidence.getGafOrganization());
        newEvidence.setCreatedBy(existingEvidence.getCreatedBy());
        newEvidence.setCreatedWhen(existingEvidence.getCreatedWhen());
        newEvidence.setEvidenceCode(existingEvidence.getEvidenceCode());
        newEvidence.setModifiedBy(existingEvidence.getModifiedBy());
        newEvidence.setModifiedWhen(existingEvidence.getModifiedWhen());
        newEvidence.setOrganizationCreatedBy(existingEvidence.getOrganizationCreatedBy());
        newEvidence.setInferredFrom(existingEvidence.getInferredFrom());


        newEvidence.setGoTerm(existingEvidence.getGoTerm());
        List<GenericTerm> parentTerms;
        parentTerms = ontologyRepository.getParentDirectTerms(existingEvidence.getGoTerm());
        assertTrue(parentTerms.size() > 0);

        assertEquals(existingEvidence, newEvidence);
        assertTrue(existingEvidence.equals(newEvidence));

        parentTerms = ontologyRepository.getParentDirectTerms(existingEvidence.getGoTerm());
        newEvidence.setGoTerm((GenericTerm) parentTerms.iterator().next());

        assertFalse(existingEvidence.equals(newEvidence));
        assertTrue(gafService.isMoreSpecificAnnotation(existingEvidence, newEvidence));
        parentTerms = ontologyRepository.getParentDirectTerms(existingEvidence.getGoTerm());

        newEvidence.setGoTerm((GenericTerm) parentTerms.iterator().next());

        assertFalse(existingEvidence.equals(newEvidence));
        assertTrue(gafService.isMoreSpecificAnnotation(existingEvidence, newEvidence));

        List<GenericTerm> childTerms = ontologyRepository.getChildDirectTerms(existingEvidence.getGoTerm());
        newEvidence.setGoTerm((GenericTerm) childTerms.iterator().next());

        assertFalse(existingEvidence.equals(newEvidence));
        assertFalse(gafService.isMoreSpecificAnnotation(existingEvidence, newEvidence));

    }


    // just makes sure that the service is still there, not something we want to run all of the time.
//    //@Test
    public void testDownload() throws Exception {
        DownloadService downloadService = new DownloadService();
        File downloadedFile = downloadService.downloadGzipFile(new File(System.getProperty("java.io.tmpdir") + "/" + "gene_association.goa_zebrafish")
                , new URL("ftp://ftp.geneontology.org/pub/go/gene-associations/submission/gene_association.goa_zebrafish.gz")
                , true);
    }


    @Test
    public void dontAddIfMoreSpecificExists() throws Exception {
        File file = new File("test/goa_go/gene_association.goa_zebrafish_morespecific");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertEquals(4, gafEntries.size());

        GafJobData gafReport1 = new GafJobData();

        gafService.processGoaGafEntries(gafEntries, gafReport1);

        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertEquals(0, gafReport1.getNewEntries().size());
        assertEquals(4, gafReport1.getExistingEntries().size());
        assertEquals(0, gafReport1.getErrors().size());
        assertEquals(0, gafReport1.getRemovedEntries().size());
    }

    /**
     * Reference: http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0003352#term=ancchart
     */
    @Test
    public void testTransitive() {
        GenericTerm regulationCiliumMovement = ontologyRepository.getTermByName("regulation of cilium movement", Ontology.GO_BP);
        assertNotNull(regulationCiliumMovement);
        assertTrue(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, regulationCiliumMovement));

        GenericTerm cellularComponentMovement = ontologyRepository.getTermByName("cellular component movement", Ontology.GO_BP);
        assertNotNull(cellularComponentMovement);
        assertFalse(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, cellularComponentMovement));
        assertTrue(ontologyRepository.isParentChildRelationshipExist(cellularComponentMovement, regulationCiliumMovement));

        GenericTerm localization = ontologyRepository.getTermByName("localization", Ontology.GO_BP);
        assertNotNull(localization);
        assertFalse(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, localization));
        assertTrue(ontologyRepository.isParentChildRelationshipExist(localization, regulationCiliumMovement));

        GenericTerm ciliumMovement = ontologyRepository.getTermByName("cilium movement", Ontology.GO_BP);
        assertNotNull(ciliumMovement);
        assertFalse(ontologyRepository.isParentChildRelationshipExist(localization, ciliumMovement));
        assertFalse(ontologyRepository.isParentChildRelationshipExist(ciliumMovement, localization));
        assertFalse(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, ciliumMovement));
        assertTrue(ontologyRepository.isParentChildRelationshipExist(ciliumMovement, regulationCiliumMovement));

    }


    @Test
    public void getParentTerms() {
//        Term ciliumMovement = getOntologyRepository().getTermByName("cilium movement", Ontology.GO_BP);
        GenericTerm regulationOfCiliumMovement = ontologyRepository.getTermByName("regulation of cilium movement", Ontology.GO_BP);
        assertEquals(3, ontologyRepository.getParentDirectTerms(regulationOfCiliumMovement).size());
        assertEquals(1, ontologyRepository.getParentTerms(regulationOfCiliumMovement, 0).size());
        assertEquals(3, ontologyRepository.getParentTerms(regulationOfCiliumMovement, 1).size());
    }

}
