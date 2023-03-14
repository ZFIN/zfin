package org.zfin.mutant.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.datatransfer.go.GafOrganization;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

/**
 *
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
    public void getMarkerGoTermEvidencesForPubZdbID() {
        String pubId = "ZDB-PUB-160828-8";
        List<MarkerGoTermEvidence> evidences = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForPubZdbID(pubId);
        assertNotNull(evidences);
        assertThat(evidences.size(), greaterThanOrEqualTo(2));
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
        GafOrganization gafOrganization = markerGoTermEvidenceRepository.getGafOrganization(GafOrganization.OrganizationEnum.NOCTUA);
        List<String> zdbIds = markerGoTermEvidenceRepository.getEvidencesForGafOrganization(gafOrganization);
        assertNotNull(zdbIds);
        // typically about 20K now that they have been moved to Uniprot as the source
        assertTrue(zdbIds.size() > 10000);
    }

    @Test
    public void getLikeMarkerGoTermEvidencesButGo() {
        List<MarkerGoTermEvidence> evidences = HibernateUtil.currentSession().createCriteria(MarkerGoTermEvidence.class).setMaxResults(2).list();
        for (MarkerGoTermEvidence evidence : evidences) {
            assertNotNull(markerGoTermEvidenceRepository.getLikeMarkerGoTermEvidencesButGo(evidence));
        }
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
    public void getNoctuaModel() {
        NoctuaModel model = markerGoTermEvidenceRepository.getNoctuaModel("gomodel:59dc728000000555");
        assertNotNull(model);
    }
}
