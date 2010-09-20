package org.zfin.ontology.repository;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.framework.HibernateUtil;
import org.zfin.mutant.MarkerGoTermEvidence;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static org.junit.Assert.*;

/**
 */
public class MarkerGoTermEvidenceRepositoryTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(MarkerGoTermEvidenceRepositoryTest.class) ;
    private static MarkerGoTermEvidenceRepository markerGoTermEvidenceRepository =
            RepositoryFactory.getMarkerGoTermEvidenceRepository();


    @Test
    public void getMarkerGoTermEvidencesForMarkerAbbreviation(){
        List<MarkerGoTermEvidence> markerGoTermEvidenceRepositoryList = markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerAbbreviation("pax%") ;
        assertNotNull(markerGoTermEvidenceRepositoryList) ;
        logger.debug(markerGoTermEvidenceRepositoryList.size());
        assertTrue(markerGoTermEvidenceRepositoryList.size()>100) ;
    }

    @Test
    public void getMarkerGoTermEvidenceByZdbID() {
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        MarkerGoTermEvidence evidenceTest = markerGoTermEvidenceRepository.getMarkerGoTermEvidenceByZdbID(markerGoTermEvidence.getZdbID()) ;
        assertNotNull(evidenceTest) ;
        assertEquals(markerGoTermEvidence,evidenceTest) ;
    }

    @Test
    public void getMarkerGoTermEvidencesForMarkerZdbID(){
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences =  markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbID(markerGoTermEvidence.getMarker().getZdbID()) ;
        assertNotNull(evidences) ;
        logger.debug(evidences.size());
        assertTrue(evidences.size()>0) ;
    }

    @Test
    public void getMarkerGoTermEvidencesForPubZdbID(){
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences =  markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForPubZdbID(markerGoTermEvidence.getSource().getZdbID()) ;
        assertNotNull(evidences) ;
        logger.debug(evidences.size());
        assertTrue(evidences.size()>0) ;
    }

    @Test
    public void getMarkerGoTermEvidencesForMarkerZdbIDOrdered(){
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("from MarkerGoTermEvidence ev")
                .setMaxResults(1)
                .uniqueResult();
        List<MarkerGoTermEvidence> evidences =  markerGoTermEvidenceRepository.getMarkerGoTermEvidencesForMarkerZdbIDOrdered(markerGoTermEvidence.getMarker().getZdbID()) ;
        assertNotNull(evidences) ;
        logger.debug(evidences.size());
        assertTrue(evidences.size()>0) ;

    }
}
