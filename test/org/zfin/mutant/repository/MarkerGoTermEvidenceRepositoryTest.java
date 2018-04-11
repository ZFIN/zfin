package org.zfin.mutant.repository;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.*;

/**
 */
public class MarkerGoTermEvidenceRepositoryTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(MarkerGoTermEvidenceRepositoryTest.class);
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
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        MarkerGoTermEvidence evidenceTest = markerGoTermEvidenceRepository.getMarkerGoTermEvidenceByZdbID(markerGoTermEvidence.getZdbID());
        assertNotNull(evidenceTest);
        assertEquals(markerGoTermEvidence, evidenceTest);
    }

    @Test
    public void getMarkerGoTermEvidencesForMarkerZdbID() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbID(markerGoTermEvidence.getMarker().getZdbID());
        assertNotNull(evidences);
        logger.debug(evidences.size());
        assertTrue(evidences.size() > 0);
    }

    @Test
    public void getMarkerGoTermEvidencesForPubZdbID() {
        String pubId = "ZDB-PUB-160828-8";
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForPubZdbID(pubId);
        assertNotNull(evidences);
        assertThat(evidences.size(), greaterThan(2));
    }

    @Test
    public void getMarkerGoTermEvidencesForMarkerZdbIDOrdered() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
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
        m.setZdbID("ZDB-GENE-011205-3");
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
    public void gafOrganizations() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession()
                .createQuery(" from MarkerGoTermEvidence  ev where ev.id like :zdbId")
                .setMaxResults(1)
                .setString("zdbId", "ZDB-MRKRGOEV-04%")
                .uniqueResult();
        assertNotNull(markerGoTermEvidence);
        assertEquals(GafOrganization.OrganizationEnum.ZFIN.toString(), markerGoTermEvidence.getOrganizationCreatedBy());
        assertNotNull(markerGoTermEvidence.getGafOrganization());
        assertEquals(GafOrganization.OrganizationEnum.ZFIN.toString(), markerGoTermEvidence.getGafOrganization().getOrganization());
    }

    @Test
    public void sourceOrganizations() {
        List<MarkerGoTermEvidenceCreatedBySource> sources = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidenceCreatedBySource.class).list();
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
        GafOrganization gafOrganization = markerGoTermEvidenceRepository.getGafOrganization(GafOrganization.OrganizationEnum.ZFIN);
        List<String> zdbIds = markerGoTermEvidenceRepository.getEvidencesForGafOrganization(gafOrganization);
        assertNotNull(zdbIds);
        // typically about 20K now that they have been moved to Uniprot as the source
        assertTrue(zdbIds.size() > 10000);
        assertTrue(zdbIds.size() < 1000000);
    }

    @Test
    public void getLikeMarkerGoTermEvidencesButGo() {
        List<MarkerGoTermEvidence> evidences = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class).setMaxResults(2).list();
        for (MarkerGoTermEvidence evidence : evidences) {
            assertNotNull(markerGoTermEvidenceRepository.getLikeMarkerGoTermEvidencesButGo(evidence));
        }
    }

    @Test
    public void addEvidenceWithInference() {
        MarkerGoTermEvidence existingEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class).setMaxResults(1).uniqueResult();
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
        Set<InferenceGroupMember> inferenceGroupMemberSet = new HashSet<InferenceGroupMember>();
        inferenceGroupMemberSet.add(inferenceGroupMember);

        evidence.setInferredFrom(inferenceGroupMemberSet);
        markerGoTermEvidenceRepository.addEvidence(evidence);

        assertNotNull(HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class)
                .add(Restrictions.eq("zdbID", evidence.getZdbID()))
                .uniqueResult());

        InferenceGroupMember inferenceGroupMemberFound = (InferenceGroupMember) HibernateUtil.currentSession().createQuery(" select ev.inferredFrom from MarkerGoTermEvidence ev where ev.zdbID = :zdbID ")
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
        NoctuaModel model  = markerGoTermEvidenceRepository.getNoctuaModel("gomodel:59dc728000000555");
        // TODO have to wait until we load noctua models int ZFIN...
        //assertNotNull(model);
    }
}
