package org.zfin.sequence.reno;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.Updates;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.Species;
import org.zfin.orthology.repository.OrthologyRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.Entrez;
import org.zfin.sequence.EntrezMGI;
import org.zfin.sequence.EntrezOMIM;
import org.zfin.sequence.EntrezProtRelation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zfin.framework.HibernateUtil.getSessionFactory;

/**
 * Class RenoRepositoryTest.
 */

public class OrthologyTest {

    private static MarkerRepository repository = RepositoryFactory.getMarkerRepository();
    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static OrthologyRepository orthoRepository = RepositoryFactory.getOrthologyRepository();
    static {
        SessionFactory sessionFactory = getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(false, TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }


    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


    @Test
    // Test that there are Redundancy runs in the database
    // Test the test redundancy run
    public void insertSingleOrthology() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            Marker pax2a = repository.getMarkerByAbbreviation("fsb");
            Publication publication = pubRepository.getPublication("ZDB-PUB-030905-1");

            Orthologue ortho = new Orthologue();
            ortho.setAbbreviation("FGF8");
            ortho.setName("fibroblast growth factor 8 (androgen-induced)");
            Species human = Species.HUMAN;
            ortho.setOrganism(human);
            ortho.setGene(pax2a);
            Set<OrthoEvidence> evidences  = new HashSet<OrthoEvidence>();
            OrthoEvidence evidence = new OrthoEvidence();
            evidence.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
            evidence.setPublication(publication);
            evidences.add(evidence);
            ortho.setEvidence(evidences);

            Entrez entrez = new Entrez() ;
            entrez.setEntrezAccNum("2253");
            entrez.setAbbreviation("FGF8");
            entrez.setRelatedMGIAccessions(new HashSet<EntrezMGI>());
            entrez.setRelatedOMIMAccessions(new HashSet<EntrezOMIM>());

            EntrezProtRelation relatedAccession= new EntrezProtRelation() ;
            relatedAccession.setEntrezAccession(entrez);
            relatedAccession.setProteinAccNum("12345678");


            ortho.setAccession(relatedAccession);
            orthoRepository.saveOrthology(ortho, publication, new Updates());

            String zdbID = ortho.getZdbID();
            assertTrue("ID created for Orthologue", zdbID != null && zdbID.startsWith("ZDB-ORTHO"));

            String hql = "from OrthoEvidence where orthologueZdbID = :zdbID ";
            Query query = session.createQuery(hql);
            query.setParameter("zdbID", zdbID);
            List<OrthoEvidence> evids = (List<OrthoEvidence>) query.list();
            assertTrue("One evidence code created", evids != null);
            assertEquals("One evidence code created", evids.size(), 1);
            OrthoEvidence evid = evids.get(0);
            assertEquals("One evidence code created", evid.getOrthologueEvidenceCode(), OrthoEvidence.Code.AA);

        }
        catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

}
