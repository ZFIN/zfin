package org.zfin.mutant.repository;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MorpholinoSequence;
import org.zfin.util.DateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;

@SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion"})
public class MutantRepositoryTest {

    private static final Logger LOG = Logger.getLogger(MutantRepositoryTest.class);
    private final static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

    static {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    /**
     * Check that genotype anh^m149 has background AB.
     */
    @Test
    public void checkBackground() {
        //  genotype with given background: anh^m149
        String zdbID = "ZDB-GENO-980202-397";
        Genotype geno = getMutantRepository().getGenotypeByID(zdbID);

        assertNotNull("Background exists", geno.getAssociatedGenotypes());

    }

    @Test
    public void checkGetGenotypeByHandle() {
        //In particular, this test is against TU because it's the standard background for Vega
        Genotype genotype = getMutantRepository().getGenotypeByHandle(Genotype.Wildtype.TU.toString());
        Assert.assertNotNull("Got TU genotype by handle", genotype);
    }

    @Test
    public void checkMorpholinoRecords() {

        //  ao term: optic placode
        String name = "neural plate";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        AnatomyItem ai = ar.getAnatomyItem(name);
        List<Morpholino> morphs =
                getMutantRepository().getPhenotypeMorpholinos(ai.createGenericTerm(), AnatomySearchBean.MAX_NUMBER_GENOTYPES);
        assertNotNull("morphs exist", morphs);

    }


    @Test
    public void checkGenotypeRecords() {

        String name = "ZDB-ALT-000921-6";
        FeatureRepository mr = RepositoryFactory.getFeatureRepository();
        Feature ftr = mr.getFeatureByID(name);

        List<Genotype> genos = getMutantRepository().getGenotypesByFeature(ftr);
        assertNotNull("genos exist", genos);
        assertTrue("genos exist", genos.size() > 0);

    }


    @Test
    public void checkPhenotypeDescriptions() {
        //  ao term: optic placode
        String name = "otic placode";
        AnatomyRepository ar = RepositoryFactory.getAnatomyRepository();
        AnatomyItem ai = ar.getAnatomyItem(name);
        PaginationResult<GenotypeExperiment> morphs =
                getMutantRepository().getGenotypeExperimentMorpholinos(ai.createGenericTerm(), true, null);
        assertNotNull("morphs exist", morphs.getPopulatedResults());

    }

    // No repository available because matches should be done through the
    // OntologyManager and MatchingTermService
/*
    @Test
    public void checkGoTerms() {
        //  ao term: ribosome
        String name = "ribosome";
        List<Term> goTerms = getOntologyRepository().getTermByName(name, Ontology.GO);
        assertNotNull(goTerms);
        assertTrue(goTerms.size() > 0);

        boolean findKnown = false;
        for (Term term : goTerms) {
            if (term.getTermName().equals("polysomal ribosome")) {
                findKnown = true;
            }
        }

        assertTrue(findKnown);
    }
*/

    @Test
    public void checkQualityTerms() {
        //  quality term: red 
        String name = "red";
        List<GenericTerm> terms = getMutantRepository().getQualityTermsByName(name);
        assertNotNull(terms);
        assertTrue(!terms.isEmpty());

        boolean findKnown = false;
        for (GenericTerm term : terms) {
            if (term.getTermName().equals("dark red brown")) {
                findKnown = true;
            }
        }

        Assert.assertTrue(findKnown);
    }


    @Test
    public void checkForPatoRecord() {
        String genoxID = "ZDB-GENOX-041102-700";
        String figureID = "ZDB-FIG-050720-1";
        String startID = "ZDB-STAGE-010723-4";
        String endID = "ZDB-STAGE-010723-4";
        String publicationID = "ZDB-PUB-090828-23";

        boolean patoExists = getMutantRepository().isPatoExists(genoxID, figureID, startID, endID, publicationID);
        assertTrue(!patoExists);

    }

    @Test
    public void checkForPatoRecordPerformance() {
        String genoxID = "ZDB-GENOX-041102-1540";
        String figureID = "ZDB-FIG-110413-2";
        String startID = "ZDB-STAGE-010723-10";
        String endID = "ZDB-STAGE-010723-10";
        String publicationID = "ZDB-PUB-090828-23";
        long start = System.currentTimeMillis();
        getMutantRepository().isPatoExists(genoxID, figureID, startID, endID, publicationID);
        long end = System.currentTimeMillis();
        if ((end - start) > 4000)
            fail("Time to execute getMutantRepository().isPatoExists() is too long: " + DateUtil.getTimeDuration(start));
    }

    @Test
    public void createDefaultPhenotype() {
        String genoxID = "ZDB-GENOX-100111-1";
        String figID = "ZDB-FIG-091215-69";

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            GenotypeExperiment genox = getMutantRepository().getGenotypeExperiment(genoxID);
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void retrieveAllWildtypeGenotypes() {
        MutantRepository mr = mutantRepository;
        List<Genotype> terms = mr.getAllWildtypeGenotypes();
        assertNotNull(terms);
    }

    @Test
    public void goTermsByMarkerAndPublication() {
        Marker marker = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-GENE-040624-2");
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication("ZDB-PUB-020724-1");
        List<GenericTerm> goTerms = getMutantRepository().getGoTermsByMarkerAndPublication(marker, publication);
        LOG.debug(goTerms.size());
        assertTrue(goTerms.size() == 0);
    }

    @Test
    public void goTermsByPhenotypeAndPublication() {
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication("ZDB-PUB-080501-11");
        List<GenericTerm> goTerms = getMutantRepository().getGoTermsByPhenotypeAndPublication(publication);
        LOG.debug(goTerms.size());
        assertTrue(goTerms.size() > 0);
    }


    @Test
    public void getZFINInferences() {
        mutantRepository.getZFINInferences("ZDB-MRPHLNO-041110-25", "ZDB-PUB-090324-13");
    }

    public static MarkerGoTermEvidence findSingleMarkerGoTermEvidenceWithOneInference() {
        HibernateUtil.createTransaction();
        MarkerGoTermEvidence markerGoTermEvidence = (MarkerGoTermEvidence) HibernateUtil.currentSession().createQuery("" +
                " from MarkerGoTermEvidence ev where ev.inferredFrom is not empty and size(ev.inferredFrom) = 1 " +
                "").setMaxResults(1).uniqueResult();

        assertNotNull(markerGoTermEvidence);
        assertEquals(1, mutantRepository.getNumberMarkerGoTermEvidences(markerGoTermEvidence));
        return markerGoTermEvidence;
    }


    @Test
    public void markerGoTermEvidenceExists() {

        try {
            MarkerGoTermEvidence markerGoTermEvidence = findSingleMarkerGoTermEvidenceWithOneInference();
            MarkerGoTermEvidence newMarkerGoTermEvidence = new MarkerGoTermEvidence();
            newMarkerGoTermEvidence.setEvidenceCode(markerGoTermEvidence.getEvidenceCode());
            newMarkerGoTermEvidence.setFlag(markerGoTermEvidence.getFlag());
            newMarkerGoTermEvidence.setSource(markerGoTermEvidence.getSource());
            newMarkerGoTermEvidence.setMarker(markerGoTermEvidence.getMarker());
            newMarkerGoTermEvidence.setGoTerm(markerGoTermEvidence.getGoTerm());
            newMarkerGoTermEvidence.setInferredFrom(markerGoTermEvidence.getInferredFrom());

            assertEquals(1, mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence));

            GoEvidenceCode ndEvidenceCode = new GoEvidenceCode();
            ndEvidenceCode.setCode(GoEvidenceCodeEnum.ND.toString());
            newMarkerGoTermEvidence.setEvidenceCode(ndEvidenceCode);
            assertEquals(0, mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence));

            newMarkerGoTermEvidence.setEvidenceCode(markerGoTermEvidence.getEvidenceCode());
            assertEquals(1, mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence));

            // copy and change inferences to test
            Set<InferenceGroupMember> newInferenceGroupMemberSet = new HashSet<InferenceGroupMember>();

            newMarkerGoTermEvidence.setInferredFrom(newInferenceGroupMemberSet);
            assertEquals(0, mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence));

            InferenceGroupMember inferenceGroupMember = new InferenceGroupMember();
            inferenceGroupMember.setInferredFrom("some inference");
            newInferenceGroupMemberSet.add(inferenceGroupMember);
            newMarkerGoTermEvidence.setInferredFrom(newInferenceGroupMemberSet);
            assertEquals(0, mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence));

            newMarkerGoTermEvidence.setInferredFrom(markerGoTermEvidence.getInferredFrom());
            assertEquals(1, mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence));
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void getMorpholinosWithMarkerRelationships() {
        List<MorpholinoSequence> morpholinos = mutantRepository.getMorpholinosWithMarkerRelationships();
        assertNotNull(morpholinos);
        LOG.info("# of morpholinos: " + morpholinos.size());
        assertTrue(morpholinos.size() > 3000);
        assertNotNull(morpholinos.get(0).getSequence());
    }

    @Test
    public void phenotypesWithObsoleteTerms() {
        List<PhenotypeStatement> phenotypes = mutantRepository.getPhenotypesOnObsoletedTerms();
        assertNotNull(phenotypes);
        assertEquals(0, phenotypes.size());
    }

}
