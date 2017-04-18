package org.zfin.datatransfer.go.service;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.*;
import org.zfin.datatransfer.service.DownloadService;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.repository.RepositoryFactory;

import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

/**
 * Tests GafService methods
 */
public class GoaGafServiceTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(GoaGafServiceTest.class);
    private GafService gafService = new GafService(GafOrganization.OrganizationEnum.GOA);
    private FpInferenceGafParser gafParser = new GoaGafParser();

    // this is a pub with no go evidence annotations and is closed, so none will be added
    private final String DEFAULT_TEST_ACCESSION = "PMID:10630700"; // "ZDB-PUB-000118-16"
    private OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    private final String GOA_DIRECTORY = "test/gaf/goa/";


    @After
    public void closeSession() {
        super.closeSession();
        // make sure to close the session to be able to re-create the entities
        HibernateUtil.closeSession();
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
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_inferencetest");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(1));
        makeTestPub(gafEntries);

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);

        assertThat("errors", gafJobData.getErrors(), hasSize(0));
        assertThat("existing", gafJobData.getExistingEntries(), hasSize(0));
        assertThat("new", gafJobData.getNewEntries(), hasSize(1));
        assertThat("updated", gafJobData.getUpdateEntries(), hasSize(0));
        assertThat("removed", gafJobData.getRemovedEntries(), hasSize(0));

        assertTrue(gafJobData.getNewEntries().iterator().next().getInferredFrom().iterator().next().getInferredFrom().equals("UniProtKB:Q9NXR7"));
    }


    @Test
    public void gafServiceBadAdd() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_badadd");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(3));
        makeTestPub(gafEntries);


        GafJobData gafJobData = new GafJobData();
        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries());
        logger.debug("errors: " + gafJobData.getErrors());

        assertThat("removed", gafJobData.getRemovedEntries(), hasSize(0));
        assertThat("new", gafJobData.getNewEntries(), hasSize(3));
        assertThat("errors", gafJobData.getErrors(), hasSize(0));
        assertThat("updated", gafJobData.getUpdateEntries(), hasSize(0));

        Iterator<MarkerGoTermEvidence> iter = gafJobData.getNewEntries().iterator();

        assertTrue(iter.next().getInferredFrom().iterator().next().getInferredFrom().equals(("UniProtKB:Q9UBQ5")));
        assertTrue(iter.next().getInferredFrom().iterator().next().getInferredFrom().equals(("UniProtKB:Q9UBQ5")));
        assertTrue(iter.next().getInferredFrom().iterator().next().getInferredFrom().equals(("UniProtKB:Q9UBQ5")));
    }

    @Test
    public void badGafEntry() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_badentry");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(13)); // 17 - 4 = 13
        makeTestPub(gafEntries);


        GafJobData gafJobData = new GafJobData();
        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());
        logger.debug("removed: " + gafJobData.getRemovedEntries().size());

        assertThat("removed", gafJobData.getRemovedEntries(), hasSize(0));
        gafJobData.getErrors().forEach(System.out::println);
        assertThat("errors", gafJobData.getErrors(), hasSize(0));
        assertThat("new", gafJobData.getNewEntries(), hasSize(13));
        assertThat("updated", gafJobData.getUpdateEntries(), hasSize(0));
    }

    @Test
    public void alreadyRanOnce() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_inferencetest");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(1));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        gafService.addAnnotations(gafReport1);

        gafReport2 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport2);
        assertThat("first new", gafReport1.getNewEntries(), hasSize(1));
        assertThat("first errors", gafReport1.getErrors(), hasSize(0));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        assertEquals("UniProtKB:Q9NXR7", gafReport1.getNewEntries().iterator().next().getInferredFrom().iterator().next().getInferredFrom());

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second errors", gafReport2.getErrors(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(1));
        assertThat("second removed", gafReport2.getRemovedEntries(), hasSize(0));
        assertThat("second updated", gafReport2.getUpdateEntries(), hasSize(0));
    }

    @Test
    public void alreadyRanOnce_2() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_badadd");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(3));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = new GafJobData();
        gafService.addAnnotations(gafReport1);
        gafService.processEntries(gafEntries, gafReport2);


        assertThat("first new", gafReport1.getNewEntries(), hasSize(3));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first errors", gafReport1.getErrors(), hasSize(0));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(3));
        assertThat("second errors", gafReport2.getErrors(), hasSize(0));
        assertThat("second removed", gafReport2.getRemovedEntries(), hasSize(0));
        assertThat("second updated", gafReport2.getUpdateEntries(), hasSize(0));
    }

    // tests null inferences and redundant entries
    @Test
    public void findDupeInferences() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_dupeinference");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(3));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        gafService.addAnnotations(gafReport1);

        gafReport2 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport2);

        logger.debug("summary: " + gafReport2.toString());
        logger.debug("entries: " + gafReport2.getNewEntries());
        logger.debug("existing: " + gafReport2.getExistingEntries());
        logger.debug("errors: " + gafReport2.getErrors());


        assertThat("first new", gafReport1.getNewEntries(), hasSize(3));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first errors", gafReport1.getErrors(), hasSize(0));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(3));
        assertThat("second errors", gafReport2.getErrors(), hasSize(0));
        assertThat("second removed", gafReport2.getRemovedEntries(), hasSize(0));
        assertThat("second updated", gafReport2.getUpdateEntries(), hasSize(0));
    }

    // valid additions, but duplicate within the gaf file
    @Test
    public void knowDupesWithAnAdd() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_duplicateentries");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(15));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
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

        assertThat("new", gafReport1.getNewEntries(), hasSize(2));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("errors", gafReport1.getErrors(), hasSize(13));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));

    }


    // tests null inferences and redundant entries
    @Test
    public void dontAddDupes() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_redundantadd");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(3));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        gafService.addAnnotations(gafReport1);

        gafReport2 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport2);

        logger.debug("summary: " + gafReport2.toString());
        logger.debug("entries: " + gafReport2.getNewEntries());
        logger.debug("existing: " + gafReport2.getExistingEntries());
        logger.debug("errors: " + gafReport2.getErrors());


        assertThat("first new", gafReport1.getNewEntries(), hasSize(2));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first errors", gafReport1.getErrors(), hasSize(1));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(3));
        assertThat("second errors", gafReport2.getErrors(), hasSize(0));
        assertThat("second removed", gafReport2.getRemovedEntries(), hasSize(0));
        assertThat("second updated", gafReport2.getUpdateEntries(), hasSize(0));
    }

    @Test
    public void mapQualifiers() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_qualifiers");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(4));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("errors", gafReport1.getErrors(), hasSize(0));
        assertThat("new", gafReport1.getNewEntries(), hasSize(4));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));

    }

    @Test
    public void colocalizeOnGoCC() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_gocc_colocalize");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(2));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("errors", gafReport1.getErrors(), hasSize(1));
        assertThat("new", gafReport1.getNewEntries(), hasSize(1));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));
    }

    @Test
    public void igiRemap() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_igi_remap");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(2));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("errors: " + gafReport1.getErrors());


        assertThat("errors", gafReport1.getErrors(), hasSize(0));
        assertThat("new", gafReport1.getNewEntries(), hasSize(2));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));

        Iterator<MarkerGoTermEvidence> iter = gafReport1.getNewEntries().iterator();

        assertEquals("ZFIN:ZDB-GENE-000201-18", iter.next().getInferredFrom().iterator().next().getInferredFrom());
        assertEquals("ZFIN:ZDB-GENE-980526-290", iter.next().getInferredFrom().iterator().next().getInferredFrom());
    }

    @Test
    public void multipleAddExists() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_otherexists");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(8));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        GafJobData gafReport2 = null;
        gafService.addAnnotations(gafReport1);

        gafReport2 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport2);

        logger.debug("summary: " + gafReport2.toString());
        logger.debug("entries: " + gafReport2.getNewEntries());
        logger.debug("existing: " + gafReport2.getExistingEntries());
        logger.debug("errors: " + gafReport2.getErrors());

        assertThat("first new", gafReport1.getNewEntries(), hasSize(7));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first errors", gafReport1.getErrors(), hasSize(1));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(7));
        assertThat("second errors", gafReport2.getErrors(), hasSize(1));
        assertThat("second removed", gafReport2.getRemovedEntries(), hasSize(0));
        assertThat("second updated", gafReport2.getUpdateEntries(), hasSize(0));

    }


    @Test
    public void ndReplace() throws GafValidationError {
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
        gafService.addAnnotations(gafJobData);

        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries());
        logger.debug("existing: " + gafJobData.getExistingEntries());
        logger.debug("errors: " + gafJobData.getErrors());

        assertThat("new", gafJobData.getNewEntries(), hasSize(1));
        assertThat("existing", gafJobData.getExistingEntries(), hasSize(0));
        assertThat("errors", gafJobData.getErrors(), hasSize(0));
        assertThat("removed", gafJobData.getRemovedEntries(), hasSize(1));
        assertThat("updated", gafJobData.getUpdateEntries(), hasSize(0));

    }


    @Test
    public void doNotDeleteAdded() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_deleteadded");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(1));
        makeTestPub(gafEntries);

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("first new", gafReport1.getNewEntries(), hasSize(1));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first errors", gafReport1.getErrors(), hasSize(0));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        GafJobData gafReport2 = null;
        gafService.addAnnotations(gafReport1);

        gafReport2 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport2);
        HibernateUtil.currentSession().flush();
        GafOrganization gafOrganization = RepositoryFactory.getMarkerGoTermEvidenceRepository().getGafOrganization(GafOrganization.OrganizationEnum.GOA);
        Set<String> existingZfinZdbIDs = new TreeSet<>(RepositoryFactory.getMarkerGoTermEvidenceRepository().getEvidencesForGafOrganization(gafOrganization));

        // is it in the database currently
        String newZdbID = gafReport1.getNewEntries().iterator().next().getZdbID();
        assertTrue(existingZfinZdbIDs.contains(newZdbID));

        // is it one of the processed existing entries
        assertTrue(gafReport2.getExistingEntries().contains(new GafJobEntry(newZdbID)));


        Collection<String> outdatedEntries = gafService.findOutdatedEntries(gafReport2, gafOrganization);

        assertFalse(outdatedEntries.contains(newZdbID));

        logger.debug("summary: " + gafReport2.toString());
        logger.debug("entries: " + gafReport2.getNewEntries());
        logger.debug("existing: " + gafReport2.getExistingEntries());
        logger.debug("errors: " + gafReport2.getErrors());

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(1));
        assertThat("second errors", gafReport2.getErrors(), hasSize(0));
        assertThat("second updated", gafReport1.getUpdateEntries(), hasSize(0));
        // never processed this
//        assertThat("", gafReport2.getRemovedEntries(), hasSize(0));
    }

    @Test
    public void alreadyExistsComparesInference() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_betterinference");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(2));

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("new", gafReport1.getNewEntries(), hasSize(0));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(2));
        assertThat("errors", gafReport1.getErrors(), hasSize(0));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));

    }

    @Test
    public void evaluateNewIeaGoRefs() throws Exception {

        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_new_iea");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(4));
        // they need to be IEA pubs, so we don't call makeTestPub here

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);
        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("new", gafReport1.getNewEntries(), hasSize(2));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("errors", gafReport1.getErrors(), hasSize(2));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));
    }

    @Test
    public void updateIeaDates() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_new_iea");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(4));

        Calendar longAgo = new GregorianCalendar();
        longAgo.add(Calendar.MONTH, -18);
        replaceDates(gafEntries, longAgo.getTime());

        GafJobData gafReport1 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport1);

        assertThat("first new", gafReport1.getNewEntries(), hasSize(2));
        assertThat("first existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("first errors", gafReport1.getErrors(), hasSize(2));
        assertThat("first removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("first updated", gafReport1.getUpdateEntries(), hasSize(0));

        gafService.addAnnotations(gafReport1);

        replaceDates(gafEntries);
        GafJobData gafReport2 = new GafJobData();
        gafService.processEntries(gafEntries, gafReport2);

        assertThat("second new", gafReport2.getNewEntries(), hasSize(0));
        assertThat("second existing", gafReport2.getExistingEntries(), hasSize(0));
        assertThat("second errors", gafReport2.getErrors(), hasSize(2));
        assertThat("second removed", gafReport2.getRemovedEntries(), hasSize(0));
        assertThat("second updated", gafReport2.getUpdateEntries(), hasSize(2));
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
        newEvidence.setGoTerm(parentTerms.iterator().next());

        assertFalse(existingEvidence.equals(newEvidence));
        assertTrue(gafService.isMoreSpecificAnnotation(existingEvidence, newEvidence));
        parentTerms = ontologyRepository.getParentDirectTerms(existingEvidence.getGoTerm());

        newEvidence.setGoTerm(parentTerms.iterator().next());

        assertFalse(existingEvidence.equals(newEvidence));
        assertTrue(gafService.isMoreSpecificAnnotation(existingEvidence, newEvidence));

        List<GenericTerm> childTerms = ontologyRepository.getChildDirectTerms(existingEvidence.getGoTerm());
        newEvidence.setGoTerm(childTerms.iterator().next());

        assertFalse(existingEvidence.equals(newEvidence));
        assertFalse(gafService.isMoreSpecificAnnotation(existingEvidence, newEvidence));

    }


    @Test
    @Ignore("just makes sure that the service is still there, not something we want to run all of the time")
    public void testDownloadGoaGzip() throws Exception {
        DownloadService downloadService = new DownloadService();
        File downloadedFile = downloadService.downloadFile(new File(System.getProperty("java.io.tmpdir") + "/" + "gene_association.goa_zebrafish")
                , new URL("ftp://ftp.ebi.ac.uk/pub/databases/GO/goa/ZEBRAFISH/gene_association.goa_zebrafish.gz")
                , true);
        assertTrue(downloadedFile.exists());
        assertTrue(downloadedFile.delete());
    }


    @Test
    public void dontAddIfMoreSpecificExists() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_morespecific");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(4));

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);

        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("new", gafReport1.getNewEntries(), hasSize(0));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(4));
        assertThat("errors", gafReport1.getErrors(), hasSize(0));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafReport1.getUpdateEntries(), hasSize(0));
    }

    @Test
    public void updateDateIfOlderThanExisting() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_morespecific");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        replaceDates(gafEntries);
        assertThat("", gafEntries, hasSize(4));

        GafJobData gafReport1 = new GafJobData();

        gafService.processEntries(gafEntries, gafReport1);

        logger.debug("summary: " + gafReport1.toString());
        logger.debug("entries: " + gafReport1.getNewEntries());
        logger.debug("existing: " + gafReport1.getExistingEntries());
        logger.debug("errors: " + gafReport1.getErrors());

        assertThat("new", gafReport1.getNewEntries(), hasSize(0));
        assertThat("existing", gafReport1.getExistingEntries(), hasSize(0));
        assertThat("update", gafReport1.getUpdateEntries(), hasSize(4));
        assertThat("errors", gafReport1.getErrors(), hasSize(0));
        assertThat("removed", gafReport1.getRemovedEntries(), hasSize(0));
    }

    /**
     * Reference: http://www.ebi.ac.uk/QuickGO/GTerm?id=GO:0003352#term=ancchart
     */
    @Test
    public void testTransitive() {
        GenericTerm regulationCiliumMovement = ontologyRepository.getTermByName("regulation of cilium movement", Ontology.GO_BP);
        assertNotNull(regulationCiliumMovement);
        assertTrue(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, regulationCiliumMovement));

        GenericTerm localization = ontologyRepository.getTermByName("localization", Ontology.GO_BP);
        assertNotNull(localization);
        assertFalse(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, localization));
        // regulates relationships no longer mapped
        assertFalse(ontologyRepository.isParentChildRelationshipExist(localization, regulationCiliumMovement));

        GenericTerm ciliumMovement = ontologyRepository.getTermByName("cilium movement", Ontology.GO_BP);
        assertNotNull(ciliumMovement);
        assertFalse(ontologyRepository.isParentChildRelationshipExist(localization, ciliumMovement));
        assertFalse(ontologyRepository.isParentChildRelationshipExist(ciliumMovement, localization));
        assertFalse(ontologyRepository.isParentChildRelationshipExist(regulationCiliumMovement, ciliumMovement));
        // regulates relationships no longer mapped
        assertFalse(ontologyRepository.isParentChildRelationshipExist(ciliumMovement, regulationCiliumMovement));

    }


    @Test
    public void getParentTerms() {
        GenericTerm regulationOfCiliumMovement = ontologyRepository.getTermByName("regulation of cilium movement", Ontology.GO_BP);
        // exactly one self record: transitive closure contains a term relating to itself.
        assertThat(ontologyRepository.getParentTerms(regulationOfCiliumMovement, 0), hasSize(1));
        // at least one parent term
        assertThat(ontologyRepository.getParentTerms(regulationOfCiliumMovement, 1), hasSize(greaterThan(0)));
    }

    @Test
    public void getZdbPub() {
        assertEquals("ZDB-PUB-000111-5", gafService.getZfinPubId("ZFIN:ZDB-PUB-000111-5|PMID:10611375"));
        assertEquals("ZDB-PUB-000111-5", gafService.getZfinPubId("ZFIN:ZDB-PUB-000111-5"));
    }

    /**
     * Should report an error if it can't find a uniprot entry.
     *
     * @throws Exception
     */
    @Test
    public void createErrorForValues() throws Exception {
        File file = new File(GOA_DIRECTORY + "gene_association.goa_zebrafish_noerror");
        List<GafEntry> gafEntries = gafParser.parseGafFile(file);
        assertThat("gaf entries loaded", gafEntries, hasSize(12));

        GafJobData gafJobData = new GafJobData();

        gafService.processEntries(gafEntries, gafJobData);
        logger.debug("summary: " + gafJobData.toString());
        logger.debug("entries: " + gafJobData.getNewEntries().size());
        logger.debug("existing: " + gafJobData.getExistingEntries().size());
        logger.debug("errors: " + gafJobData.getErrors().size());


        assertThat("errors", gafJobData.getErrors(), hasSize(12));
        assertThat("existing", gafJobData.getExistingEntries(), hasSize(0));
        assertThat("new", gafJobData.getNewEntries(), hasSize(0));
        assertThat("removed", gafJobData.getRemovedEntries(), hasSize(0));
        assertThat("updated", gafJobData.getUpdateEntries(), hasSize(0));

    }

    private static void replaceDates(List<GafEntry> entries) {
        replaceDates(entries, new Date());
    }

    private static void replaceDates(List<GafEntry> entries, Date newDate) {
        DateFormat gafDateFormat = new SimpleDateFormat("yyyyMMdd");
        for (GafEntry entry : entries) {
            entry.setCreatedDate(gafDateFormat.format(newDate));
        }
    }

}
