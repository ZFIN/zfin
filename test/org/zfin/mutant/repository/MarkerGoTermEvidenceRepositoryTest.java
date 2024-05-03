package org.zfin.mutant.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.gwt.root.dto.GoEvidenceDTO;
import org.zfin.gwt.root.server.MarkerGoEvidenceRPCServiceImpl;
import org.zfin.marker.Marker;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.MarkerGoTermEvidenceCreatedBySource;
import org.zfin.mutant.NoctuaModel;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.*;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;

/**
 */
public class MarkerGoTermEvidenceRepositoryTest extends AbstractDatabaseTest {

    private Logger logger = LogManager.getLogger(MarkerGoTermEvidenceRepositoryTest.class);
    private static MarkerGoTermEvidenceRepository markerGoTermEvidenceRepository =
            RepositoryFactory.getMarkerGoTermEvidenceRepository();


    @Test
    public void getMarkerGoTermEvidencesForMarkerAbbreviation() {
        List<MarkerGoTermEvidence> markerGoTermEvidenceRepositoryList = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerAbbreviation("pax%");
        assertNotNull(markerGoTermEvidenceRepositoryList);
        logger.debug(markerGoTermEvidenceRepositoryList.size());
        assertTrue(markerGoTermEvidenceRepositoryList.size() > 100);
    }

    @Test
    public void getMarkerGoTermEvidenceByZdbID() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        MarkerGoTermEvidence evidenceTest = markerGoTermEvidenceRepository.getMarkerGoTermEvidenceByZdbID(markerGoTermEvidence.getZdbID());
        assertNotNull(evidenceTest);
        assertEquals(markerGoTermEvidence, evidenceTest);
    }

    @Test
    public void getMarkerGoTermEvidencesForMarkerZdbID() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbID(markerGoTermEvidence.getMarker().getZdbID());
        assertNotNull(evidences);
        logger.debug(evidences.size());
        assertTrue(evidences.size() > 0);
    }

    @Test
    public void getMarkerGoTermEvidencesForPubZdbID() {
        String pubId = "ZDB-PUB-110330-1";
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForPubZdbID(pubId);
        assertNotNull(evidences);
        assertThat(evidences.size(), greaterThanOrEqualTo(2));
    }

    @Test
    public void getMarkerGoTermEvidencesForMarkerZdbIDOrdered() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbIDOrdered(markerGoTermEvidence.getMarker().getZdbID());
        assertNotNull(evidences);
        logger.debug(evidences.size());
        assertTrue(evidences.size() > 0);

    }

    @Test
    public void getMarkerGoTermEvidenceCodes() {
        for (GoEvidenceCodeEnum goEvidenceCodeEnum : GoEvidenceCodeEnum.values()) {
            assertNotNull(markerGoTermEvidenceRepository.getGoEvidenceCode(goEvidenceCodeEnum.name()));
        }
        assertNull(markerGoTermEvidenceRepository.getGoEvidenceCode("badcode"));
    }

    @Test
    public void getNdExistsForGoGeneEvidenceCode() {
        // should fine: ZDB-MRKRGOEV-031218-39
        MarkerGoTermEvidence markerGoTermEvidence = new MarkerGoTermEvidence();
        Marker m = new Marker();
        m.setZdbID("ZDB-GENE-081022-77");
        markerGoTermEvidence.setMarker(m);
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-091209-4029");
        markerGoTermEvidence.setGoTerm(term);
        assertNotNull(markerGoTermEvidenceRepository.getNdExistsForGoGeneEvidenceCode(markerGoTermEvidence));
        // some random go term
        term.setZdbID("ZDB-TERM-091209-3222");
        assertNull(markerGoTermEvidenceRepository.getNdExistsForGoGeneEvidenceCode(markerGoTermEvidence));
    }

    @Test
    public void sourceOrganizations() {
        List<MarkerGoTermEvidenceCreatedBySource> sources = currentSession()
                .createQuery("FROM MarkerGoTermEvidenceCreatedBySource")
                .list();
        assertNotNull(sources);
        assertTrue(sources.size() > 3);
        assertTrue(sources.size() < 20);
        for (MarkerGoTermEvidenceCreatedBySource source : sources) {
            logger.debug(source);
        }
    }

    @Test
    public void getGafOrganization() {
        GafOrganization gafOrganization = markerGoTermEvidenceRepository.getGafOrganization(GafOrganization.OrganizationEnum.ZFIN);
        assertNotNull(gafOrganization);
        assertEquals(GafOrganization.OrganizationEnum.ZFIN.toString(), gafOrganization.getOrganization());
    }

    @Test
    public void getEvidencesForGafOrganization() {
        //ignore this test until 12/1/24
        //depends on NOCTUA GPAD LOAD being fixed
        Assume.assumeTrue( new Date().after( new GregorianCalendar(2024,Calendar.DECEMBER, 1).getTime() ) );


        GafOrganization gafOrganization = markerGoTermEvidenceRepository.getGafOrganization(GafOrganization.OrganizationEnum.NOCTUA);
        List<String> zdbIds = markerGoTermEvidenceRepository.getEvidencesForGafOrganization(gafOrganization);
        assertNotNull(zdbIds);
        // typically about 20K now that they have been moved to Uniprot as the source
        assertTrue(zdbIds.size() > 10000);
    }

    @Test
    public void getLikeMarkerGoTermEvidencesButGo() {
        List<MarkerGoTermEvidence> evidences = currentSession()
                .createQuery("from MarkerGoTermEvidence")
                .setMaxResults(2).list();
        for (MarkerGoTermEvidence evidence : evidences) {
            assertNotNull(markerGoTermEvidenceRepository.getLikeMarkerGoTermEvidencesButGo(evidence));
        }
    }

    @Test
    public void addEvidenceWithInference() {
        MarkerGoTermEvidence existingEvidence = (MarkerGoTermEvidence) currentSession()
                .createQuery("from MarkerGoTermEvidence where zdbID = :mrkrID")
                .setParameter("mrkrID", "ZDB-MRKRGOEV-211013-579")
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

        // change the
        InferenceGroupMember inferenceGroupMember = new InferenceGroupMember();
        inferenceGroupMember.setInferredFrom("UniProtKB:Q9NXR7");
        Set<InferenceGroupMember> inferenceGroupMemberSet = new HashSet<>();
        inferenceGroupMemberSet.add(inferenceGroupMember);

        evidence.setInferredFrom(inferenceGroupMemberSet);
        markerGoTermEvidenceRepository.addEvidence(evidence, false);

        assertNotNull(currentSession().createQuery("from MarkerGoTermEvidence where zdbID = :mrkrID")
                .setParameter("mrkrID", evidence.getZdbID())
                .uniqueResult());

        InferenceGroupMember inferenceGroupMemberFound = (InferenceGroupMember) currentSession().createQuery(" select ev.inferredFrom from MarkerGoTermEvidence ev where ev.zdbID = :zdbID ")
                .setParameter("zdbID", evidence.getZdbID())
                .uniqueResult();

        assertNotNull(inferenceGroupMemberFound);
        assertEquals("UniProtKB:Q9NXR7", inferenceGroupMemberFound.getInferredFrom());
    }

    @Test
    public void getEvidenceForMarkerCount() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int count = markerGoTermEvidenceRepository.getEvidenceForMarkerCount(m);
        assertTrue(count > 20);
        assertTrue(count < 60);
    }


    @Test
    public void getFirstEvidenceForMarkerOntology() {
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        assertNotNull(markerGoTermEvidenceRepository.getFirstEvidenceForMarkerOntology(m, Ontology.GO_BP));
        assertNotNull(markerGoTermEvidenceRepository.getFirstEvidenceForMarkerOntology(m, Ontology.GO_MF));
        assertNotNull(markerGoTermEvidenceRepository.getFirstEvidenceForMarkerOntology(m, Ontology.GO_CC));
    }

    @Test
    public void getMarkerGoEvidence() {
        MarkerGoEvidenceRPCServiceImpl service = new MarkerGoEvidenceRPCServiceImpl();
        List<GoEvidenceDTO> list = service.getMarkerGoTermEvidencesForPub("ZDB-PUB-160828-8");
        assertNotNull(list);
    }

    @Test
    public void getNoctuaModel() {
        NoctuaModel model = markerGoTermEvidenceRepository.getNoctuaModel("gomodel:59dc728000000555");
        assertNotNull(model);
    }
}
