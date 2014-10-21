package org.zfin.mutant.repository;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.presentation.AnatomySearchBean;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.presentation.TermHistogramBean;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MorpholinoSequence;
import org.zfin.util.DateUtil;

import java.util.*;

import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

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
        OntologyRepository ar = RepositoryFactory.getOntologyRepository();
        GenericTerm ai = ar.getTermByName(name, Ontology.ANATOMY);
        List<SequenceTargetingReagent> morphs =
                getMutantRepository().getPhenotypeMorpholinos(ai, AnatomySearchBean.MAX_NUMBER_GENOTYPES);
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
        String name = "otic placode";
        OntologyRepository ar = RepositoryFactory.getOntologyRepository();
        GenericTerm ai = ar.getTermByName(name, Ontology.ANATOMY);
        PaginationResult<GenotypeExperiment> morphs =
                getMutantRepository().getGenotypeExperimentSequenceTargetingReagents(ai, true, null);
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

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            getMutantRepository().getGenotypeExperiment(genoxID);
        } finally {
            tx.rollback();
        }
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
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication("ZDB-PUB-080501-10");
        List<GenericTerm> goTerms = getMutantRepository().getGoTermsByPhenotypeAndPublication(publication);
        assertNotNull(goTerms);
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
    public void phenotypeExistForAOTerm() {
        // bile canaliculus
        String oboID = "ZFA:0005163 ";
        GenericTerm term = RepositoryFactory.getOntologyRepository().getTermByOboID(oboID);
        List<GenotypeExperiment> genox = mutantRepository.getGenotypeExperimentSequenceTargetingReagents(term, null);
        assertNotNull(genox);
        assertTrue(genox.size() >= 0);
    }

    @Test
    public void phenotypesWithObsoleteTerms() {
        List<PhenotypeStatement> phenotypes = mutantRepository.getPhenotypesOnObsoletedTerms();
        assertNotNull(phenotypes);
        assertEquals(0, phenotypes.size());

        mutantRepository.getPhenotypesOnObsoletedTerms(Ontology.ANATOMY);
        mutantRepository.getPhenotypesOnObsoletedTerms(Ontology.QUALITY);
    }

    //@Test
    public void getGoEvidenceOnObsoletedTerms() {
        List<MarkerGoTermEvidence> goEvidence = mutantRepository.getGoEvidenceOnObsoletedTerms();
        assertNotNull(goEvidence);
        assertEquals(0, goEvidence.size());
    }

    @Test
    public void getTermPhenotypeUsage() {
        Map<TermHistogramBean, Long> termHistogramBeanLongMap = mutantRepository.getTermPhenotypeUsage();
        assertNotNull(termHistogramBeanLongMap);
        assertTrue(termHistogramBeanLongMap.size() > 0);
    }

    @Test
    public void phenotypesWithSecondaryTerms() {
        List<PhenotypeStatement> phenotypes = mutantRepository.getPhenotypesOnSecondaryTerms();
        assertNotNull(phenotypes);
        assertEquals(0, phenotypes.size());
    }

    @Test
    public void getLineForMarker() {
        String display = mutantRepository.getMutantLinesDisplay("ZDB-GENE-010606-1");
        assertNotNull(display);
    }

    @Test
    public void getAllelesForMarker() {
        List<FeaturePresentationBean> featurePresentationBeans = mutantRepository.getAllelesForMarker("ZDB-GENE-010606-1");
        assertTrue(featurePresentationBeans.size() > 4);
        assertTrue(featurePresentationBeans.size() < 20);
    }

    @Test
    public void getTransgenicLines() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-TGCONSTRCT-070117-94");
        List<String> links = mutantRepository.getTransgenicLines(m);
        assertThat(links.size(), greaterThan(65));
    }

    @Test
    public void getPhenotypeStatementsByGenotype() {
        //String genotypeID = "ZDB-GENO-030619-2";
        String genotypeID = "ZDB-GENO-070215-11";
        Genotype genotype = mutantRepository.getGenotypeByID(genotypeID);
        List<PhenotypeStatement> statements = mutantRepository.getPhenotypeStatementsByGenotype(genotype);
        assertNotNull(statements);
    }

    @Test
    public void getPhenotypeStatementsByGene() {
        Marker gene = getMarkerRepository().getMarkerByAbbreviation("bmp4");
        List<PhenotypeStatement> statements = mutantRepository.getPhenotypeStatementsByMarker(gene);
        assertNotNull(statements);
    }

    @Test
    public void getPhenotypeStatementsByGenotypeExperiment() {
        //String genotypeID = "ZDB-GENO-030619-2";
        String genoxID = "ZDB-GENOX-091027-5";
        List<String> genoxIds = new ArrayList<String>(1);
        genoxIds.add(genoxID);
        List<PhenotypeStatement> statements = mutantRepository.getPhenotypeStatementsByGenotypeExperiments(genoxIds);
        assertNotNull(statements);
    }

    @Test
    public void getMorpholinosById() {
        //String genotypeID = "ZDB-GENO-030619-2";
        String moID = "ZDB-MRPHLNO-101014-10";
        SequenceTargetingReagent sequenceTargetingReagent = mutantRepository.getMorpholinosById(moID);
        assertNotNull(sequenceTargetingReagent);
        assertNotNull(sequenceTargetingReagent.getTargetGenes());
        assertEquals(2, sequenceTargetingReagent.getTargetGenes().size());
    }

    @Test
    public void getGenoxAttributions() {
        String genoxID = "ZDB-GENOX-091027-5";
        List<String> genoxIds = new ArrayList<String>(1);
        genoxIds.add(genoxID);
        Set<String> attributions = mutantRepository.getGenoxAttributions(genoxIds);
        assertNotNull(attributions);
        assertTrue(attributions.size() > 1);
    }

    @Test
    public void getFishCitations() {
        List<String> genoxIds = new ArrayList<String>(1);
        genoxIds.add("ZDB-GENO-070406-1");
        genoxIds.add("ZDB-GENOX-100402-4");
        List<Publication> attributions = mutantRepository.getFishAttributionList(genoxIds);
        assertNotNull(attributions);
        assertTrue(attributions.size() > 1);
    }

    @Test
    public void getWildtypeLinesSummary() {
        List<Genotype> wildtypes = mutantRepository.getAllWildtypeGenotypes();
        assertNotNull(wildtypes);
        assertTrue(wildtypes.size() > 20);
    }

    @Test
    public void getPhenotypeFigures() {
        // actinotrichium
        ////String oboID = "ZFA:0005435";
        // fin fold actinotrichium
        String oboID = "ZFA:0000089";
        String genotypeID = "ZDB-GENO-090827-1";
        GenericTerm term = getOntologyRepository().getTermByOboID(oboID);
        Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(genotypeID);
        List<Figure> figures = getMutantRepository().getPhenotypeFigures(term, genotype, true);
        assertNotNull(figures);
    }

    @Test
    public void getPhenotypeStatementsForGenoAndStructure() {
        // actinotrichium
        ////String oboID = "ZFA:0005435";
        // fin fold actinotrichium
        String oboID = "ZFA:0000089";
        String genotypeID = "ZDB-GENO-090827-1";
        GenericTerm term = getOntologyRepository().getTermByOboID(oboID);
        Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(genotypeID);
        List<PhenotypeStatement> statements = getMutantRepository().getPhenotypeStatement(term, genotype, true);
        assertNotNull(statements);
    }

}
