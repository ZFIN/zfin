package org.zfin.mutant.repository;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.GafOrganization;
import org.zfin.mutant.InferenceGroupMember;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.mutant.MarkerGoTermEvidenceCreatedBySource;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.repository.MarkerGoTermEvidenceRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForPubZdbID(markerGoTermEvidence.getSource().getZdbID());
        assertNotNull(evidences);
        logger.debug(evidences.size());
        assertTrue(evidences.size() > 0);
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
        assertTrue(zdbIds.size() > 100000);
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
        try {
            HibernateUtil.createTransaction();
            evidence = markerGoTermEvidenceRepository.addEvidence(evidence);

            assertNotNull(HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class)
                    .add(Restrictions.eq("zdbID", evidence.getZdbID()))
                    .uniqueResult());

            InferenceGroupMember inferenceGroupMemberFound = (InferenceGroupMember) HibernateUtil.currentSession().createQuery(" select ev.inferredFrom from MarkerGoTermEvidence ev where ev.zdbID = :zdbID ")
                    .setParameter("zdbID", evidence.getZdbID())
                    .uniqueResult();

            assertNotNull(inferenceGroupMemberFound);
            assertEquals("UniProtKB:Q9NXR7", inferenceGroupMemberFound.getInferredFrom());
        } catch (HibernateException e) {
            fail(e.toString());
        } finally {
            HibernateUtil.rollbackTransaction();
        }

    }

}
