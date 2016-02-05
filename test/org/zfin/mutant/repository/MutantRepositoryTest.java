package org.zfin.mutant.repository;


import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.expression.Figure;
import org.zfin.feature.Feature;
import org.zfin.feature.repository.FeatureRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.GoEvidenceCodeEnum;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.presentation.TermHistogramBean;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.STRMarkerSequence;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.zfin.repository.RepositoryFactory.*;

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

        assertThat("Background exists", geno.getAssociatedGenotypes(), notNullValue());

    }

    @Test
    public void checkGetGenotypeByHandle() {
        //In particular, this test is against TU because it's the standard background for Vega
        Genotype genotype = getMutantRepository().getGenotypeByHandle(Genotype.Wildtype.TU.toString());
        assertThat("Got TU genotype by handle", genotype, notNullValue());
    }

    @Test
    public void checkGenotypeRecords() {

        String name = "ZDB-ALT-000921-6";
        FeatureRepository mr = RepositoryFactory.getFeatureRepository();
        Feature ftr = mr.getFeatureByID(name);

        List<Genotype> genos = getMutantRepository().getGenotypesByFeature(ftr);
        assertThat("genos exist", genos, notNullValue());
        assertThat("genos exist", genos, not(empty()));

    }


    @Test
    public void checkQualityTerms() {
        //  quality term: red
        String name = "red";
        List<GenericTerm> terms = getMutantRepository().getQualityTermsByName(name);
        assertThat(terms, notNullValue());
        assertThat(terms, not(empty()));

        boolean findKnown = false;
        for (GenericTerm term : terms) {
            if (term.getTermName().equals("dark red brown")) {
                findKnown = true;
            }
        }

        assertThat(findKnown, is(true));
    }


    @Test
    public void checkForPatoRecord() {
        String genoxID = "ZDB-GENOX-041102-700";
        String figureID = "ZDB-FIG-050720-1";
        String startID = "ZDB-STAGE-010723-4";
        String endID = "ZDB-STAGE-010723-4";
        String publicationID = "ZDB-PUB-090828-23";

        boolean patoExists = getMutantRepository().isPatoExists(genoxID, figureID, startID, endID, publicationID);
        assertThat(patoExists, is(false));

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
        assertThat("Time to execute getMutantRepository().isPatoExists() is too long", end - start, lessThan(4000L));
    }

    @Test
    public void createDefaultPhenotype() {
        String genoxID = "ZDB-GENOX-100111-1";

        Session session = HibernateUtil.currentSession();
        Transaction tx = session.beginTransaction();
        try {
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
        assertThat(goTerms, is(empty()));
    }

    @Test
    public void goTermsByPhenotypeAndPublication() {
        Publication publication = RepositoryFactory.getPublicationRepository().getPublication("ZDB-PUB-080501-10");
        List<GenericTerm> goTerms = getMutantRepository().getGoTermsByPhenotypeAndPublication(publication);
        assertThat(goTerms, notNullValue());
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

        assertThat(markerGoTermEvidence, notNullValue());
        assertThat(mutantRepository.getNumberMarkerGoTermEvidences(markerGoTermEvidence), is(1));
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

            assertThat(mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence), is(1));

            GoEvidenceCode ndEvidenceCode = new GoEvidenceCode();
            ndEvidenceCode.setCode(GoEvidenceCodeEnum.ND.toString());
            newMarkerGoTermEvidence.setEvidenceCode(ndEvidenceCode);
            assertThat(mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence), is(0));

            newMarkerGoTermEvidence.setEvidenceCode(markerGoTermEvidence.getEvidenceCode());
            assertThat(mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence), is(1));

            // copy and change inferences to test
            Set<InferenceGroupMember> newInferenceGroupMemberSet = new HashSet<>();

            newMarkerGoTermEvidence.setInferredFrom(newInferenceGroupMemberSet);
            assertThat(mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence), is(0));

            InferenceGroupMember inferenceGroupMember = new InferenceGroupMember();
            inferenceGroupMember.setInferredFrom("some inference");
            newInferenceGroupMemberSet.add(inferenceGroupMember);
            newMarkerGoTermEvidence.setInferredFrom(newInferenceGroupMemberSet);
            assertThat(mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence), is(0));

            newMarkerGoTermEvidence.setInferredFrom(markerGoTermEvidence.getInferredFrom());
            assertThat(mutantRepository.getNumberMarkerGoTermEvidences(newMarkerGoTermEvidence), is(1));
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void getSTRsWithMarkerRelationships() {
        List<STRMarkerSequence> sequenceTargetingReagents = mutantRepository.getSequenceTargetingReagentsWithMarkerRelationships();
        assertThat(sequenceTargetingReagents, notNullValue());
        LOG.info("# of sequence targeting reagents: " + sequenceTargetingReagents.size());
        assertThat(sequenceTargetingReagents, hasSize(greaterThan(3000)));
        assertThat(sequenceTargetingReagents.get(0).getSequence(), notNullValue());
    }

    @Test
    public void phenotypesWithObsoleteTerms() {
        List<PhenotypeStatement> phenotypes = mutantRepository.getPhenotypesOnObsoletedTerms();
        assertThat(phenotypes, notNullValue());
        assertThat(phenotypes, is(empty()));

        mutantRepository.getPhenotypesOnObsoletedTerms(Ontology.ANATOMY);
        mutantRepository.getPhenotypesOnObsoletedTerms(Ontology.QUALITY);
    }

    @Test
    public void getGoEvidenceOnObsoletedTerms() {
        List<MarkerGoTermEvidence> goEvidence = mutantRepository.getGoEvidenceOnObsoletedTerms();
        assertThat(goEvidence, notNullValue());
    }

    @Test
    public void getTermPhenotypeUsage() {
        Map<TermHistogramBean, Long> termHistogramBeanLongMap = mutantRepository.getTermPhenotypeUsage();
        assertThat(termHistogramBeanLongMap, notNullValue());
        assertThat(termHistogramBeanLongMap.keySet(), not(empty()));
    }

    @Test
    public void phenotypesWithSecondaryTerms() {
        List<PhenotypeStatement> phenotypes = mutantRepository.getPhenotypesOnSecondaryTerms();
        assertThat(phenotypes, notNullValue());
        assertThat(phenotypes, is(empty()));
    }

    @Test
    public void getAllelesForMarker() {
        List<Feature> features = mutantRepository.getAllelesForMarker("ZDB-GENE-010606-1", "is allele of");
        assertThat(features, hasSize(both(greaterThan(4)).and(lessThan(20))));
    }

    @Test
    public void getTransgenicLines() {
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-TGCONSTRCT-070117-94");
        List<Genotype> links = mutantRepository.getTransgenicLinesForConstruct(m);
        assertThat(links, hasSize(greaterThan(50)));
    }

    @Test
    public void getPhenotypeStatementsByGenotype() {
        String genotypeID = "ZDB-GENO-070215-11";
        Genotype genotype = mutantRepository.getGenotypeByID(genotypeID);
        List<PhenotypeStatementWarehouse> statements = mutantRepository.getPhenotypeStatementsByGenotype(genotype);
        assertThat(statements, notNullValue());
    }

    @Test
    public void getPhenotypeStatementsByGene() {
        Marker gene = getMarkerRepository().getMarkerByAbbreviation("bmp4");
        List<PhenotypeStatement> statements = mutantRepository.getPhenotypeStatementsByMarker(gene);
        assertThat(statements, notNullValue());
    }

    @Test
    public void getPhenotypeStatementsByGenotypeExperiment() {
        String genoxID = "ZDB-GENOX-091027-5";
        List<String> genoxIds = new ArrayList<>(1);
        genoxIds.add(genoxID);
        List<PhenotypeStatement> statements = mutantRepository.getPhenotypeStatementsByGenotypeExperiments(genoxIds);
        assertThat(statements, notNullValue());
    }

    @Test
    public void getMorpholinosById() {
        String moID = "ZDB-MRPHLNO-101014-10";
        SequenceTargetingReagent sequenceTargetingReagent = mutantRepository.getSequenceTargetingReagentByID(moID);
        assertThat(sequenceTargetingReagent, notNullValue());
        assertThat(sequenceTargetingReagent.getTargetGenes(), notNullValue());
        assertThat(sequenceTargetingReagent.getTargetGenes(), hasSize(2));
    }

    @Test
    public void getGenoxAttributions() {
        String genoxID = "ZDB-GENOX-091027-5";
        List<String> genoxIds = new ArrayList<>(1);
        genoxIds.add(genoxID);
        Set<String> attributions = mutantRepository.getGenoxAttributions(genoxIds);
        assertThat(attributions, notNullValue());
        assertThat(attributions, hasSize(greaterThan(1)));
    }

    @Test
    public void getFishCitations() {
        Fish fish = new Fish();
        fish.setZdbID("ZDB-FISH-150901-1187");
        Genotype genotype = new Genotype();
        genotype.setZdbID("ZDB-GENO-070406-1");
        fish.setGenotype(genotype);
        List<Publication> attributions = mutantRepository.getFishAttributionList(fish);
        assertThat(attributions, notNullValue());
        assertThat(attributions, hasSize(greaterThan(1)));
    }

    @Test
    public void getWildtypeLinesSummary() {
        List<Genotype> wildtypes = mutantRepository.getAllWildtypeGenotypes();
        assertThat(wildtypes, notNullValue());
        assertThat(wildtypes, hasSize(greaterThan(20)));
    }

    @Test
    public void getWildtypeFish() {
        List<Fish> wildtypes = mutantRepository.getAllWildtypeFish();
        assertThat(wildtypes, notNullValue());
        assertThat(wildtypes, hasSize(lessThan(50)));
    }

    @Test
    public void getPhenotypeFigures() {
        // fin fold actinotrichium
        String oboID = "ZFA:0000089";
        String genotypeID = "ZDB-GENO-090827-1";
        GenericTerm term = getOntologyRepository().getTermByOboID(oboID);
        Genotype genotype = RepositoryFactory.getMutantRepository().getGenotypeByID(genotypeID);
        List<Figure> figures = getMutantRepository().getPhenotypeFigures(term, genotype, true);
        assertThat(figures, notNullValue());
    }

    @Test
    public void getPhenotypeStatementsForGenoAndStructure() {
        // fin fold actinotrichium
        String oboID = "ZFA:0000089";
        String fishID = "ZDB-FISH-090827-1";
        GenericTerm term = getOntologyRepository().getTermByOboID(oboID);
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        List<PhenotypeStatement> statements = getMutantRepository().getPhenotypeStatement(term, fish, true);
        assertThat(statements, notNullValue());
    }

    @Test
    public void gwtStrList() {
        String publicationID = "ZDB-PUB-130403-23";
        List<SequenceTargetingReagent> reagentList = RepositoryFactory.getMutantRepository().getStrList(publicationID);
        assertThat(reagentList, notNullValue());
    }

    @Test
    public void gwtFishList() {
        String publicationID = "ZDB-PUB-130403-23";
        List<Fish> reagentList = RepositoryFactory.getMutantRepository().getFishList(publicationID);
        assertThat(reagentList, notNullValue());
    }

    @Test
    public void getDiseaseModel() {
        String publicationID = "ZDB-PUB-990507-16";
        String fishID = "ZDB-FISH-150901-19447";
        List<DiseaseAnnotation> reagentList = RepositoryFactory.getMutantRepository().getDiseaseModel(fishID, publicationID);
        assertThat(reagentList, notNullValue());
    }

    @Test
    public void gwtFish() {
        String fishID = "ZDB-FISH-150901-1441";
        Fish fish = RepositoryFactory.getMutantRepository().getFish(fishID);
        assertThat(fish, notNullValue());
    }

    @Test
    public void checkExistingFish() {
        String genotypeID = "ZDB-GENO-960809-7";
        String strID = "ZDB-TALEN-150413-1";
        Fish fish = new Fish();
        fish.setGenotype(mutantRepository.getGenotypeByID(genotypeID));
        List<SequenceTargetingReagent> strList = new ArrayList<>(2);
        strList.add((SequenceTargetingReagent) getMarkerRepository().getMarkerByID(strID));
        fish.setStrList(strList);
        Fish zFish = RepositoryFactory.getMutantRepository().getFishByGenoStr(fish);
    }

    @Test
    public void checkExistingFishDoubleStr() {
        String genotypeID = "ZDB-GENO-960809-7";
        String strID = "ZDB-TALEN-150413-1";
        String str1ID = "ZDB-MRPHLNO-090212-1";
        Fish fish = new Fish();
        fish.setGenotype(mutantRepository.getGenotypeByID(genotypeID));
        List<SequenceTargetingReagent> strList = new ArrayList<>(2);
        strList.add((SequenceTargetingReagent) getMarkerRepository().getMarkerByID(strID));
        strList.add((SequenceTargetingReagent) getMarkerRepository().getMarkerByID(str1ID));
        fish.setStrList(strList);
        Fish zFish = RepositoryFactory.getMutantRepository().getFishByGenoStr(fish);
    }

    @Test
    public void getAllPubsForFish() {
        String fishID = "ZDB-FISH-150901-20021";
        List<Publication> publicationList = RepositoryFactory.getMutantRepository().getPublicationWithFish(fishID);
        assertThat(publicationList, notNullValue());
        assertThat(publicationList, not(empty()));
    }

    @Test
    public void getFishModel() {
        String fishID = "ZDB-FISH-150901-20021";
        String expID = "ZDB-EXP-050930-4";
        FishExperiment fishModel = RepositoryFactory.getMutantRepository().getFishModel(fishID, expID);
        assertThat(fishModel, notNullValue());
    }

    @Test
    public void getExistingDiseaseModel() {
        String diseaseID = "ZDB-TERM-150506-542";
        GenericTerm disease = getOntologyRepository().getTermByZdbID(diseaseID);
        String pubID = "ZDB-PUB-990507-16";
        Publication pub = getPublicationRepository().getPublication(pubID);
        DiseaseAnnotation model = new DiseaseAnnotation();
        model.setDisease(disease);
        model.setPublication(pub);
        model.setEvidenceCode("IC");
        DiseaseAnnotation fishModel = RepositoryFactory.getMutantRepository().getDiseaseModel(model);
    }

    @Test
    public void getFishExperiment() {
        Genotype genotype = getMutantRepository().getGenotypeByID("ZDB-GENO-071127-8");
        List<FishExperiment> fishList = mutantRepository.getFishExperiment(genotype);
        for (FishExperiment experiment : fishList) {
            System.out.println(experiment.getFish().getHandle());
        }
        assertThat(fishList, notNullValue());
        assertThat(fishList, not(empty()));
    }

    @Test
    public void getFishByGenotype() {
        Genotype genotype = getMutantRepository().getGenotypeByID("ZDB-GENO-140109-26");
        List<Fish> list = mutantRepository.getFishByGenotype(genotype);
        assertThat(list, notNullValue());
        assertThat(list, not(empty()));
    }

    @Test
    public void getFishByGenotypeNoExperiment() {
        Genotype genotype = getMutantRepository().getGenotypeByID("ZDB-GENO-140109-26");
        List<Fish> list = mutantRepository.getFishByGenotypeNoExperiment(genotype);
        assertThat(list, notNullValue());
        assertThat(list, not(empty()));
    }

    @Test
    public void getPhenotypeStatementForMutantSummary() {
        Fish genotype = getMutantRepository().getFish("ZDB-FISH-150901-21301");
        GenericTerm term = getOntologyRepository().getTermByOboID("ZFA:0000386/");
        List<PhenotypeStatement> fishList = mutantRepository.getPhenotypeStatementForMutantSummary(term, genotype, false);
        assertThat(fishList, notNullValue());
    }

    @Test
    public void getPhenotypeStatementForMarker() {
        Marker marker = getMarkerRepository().getMarkerByID("ZDB-GENE-000627-2");
        List<PhenotypeStatement> fishList = mutantRepository.getPhenotypeStatementForMarker(marker);
        assertThat(fishList, notNullValue());
    }

    @Test
    public void fishListBySequenceTargetingReagent() {
        SequenceTargetingReagent sequenceTargetingReagent = mutantRepository.getSequenceTargetingReagentByID("ZDB-MRPHLNO-060317-4");
        List<Fish> fishList = mutantRepository.getFishListBySequenceTargetingReagent(sequenceTargetingReagent);
        assertThat(fishList, notNullValue());
        assertThat(fishList, hasSize(greaterThan(20)));
    }

    @Test
    public void getGenotypesByFeatureAndBackground() {
        String featureID = "ZDB-ALT-040917-2";
        Feature feature = getFeatureRepository().getFeatureByID(featureID);
        Publication publication = getPublicationRepository().getPublication("ZDB-PUB-140101-33");
        Genotype background = getMutantRepository().getGenotypeByID("ZDB-GENO-010924-10");
        List<Genotype> genotypeList = mutantRepository.getGenotypesByFeatureAndBackground(feature, background, publication);
        assertThat(genotypeList, notNullValue());
    }

    @Test
    public void getZygosityList() {
        List<Zygosity> zygosityList = getMutantRepository().getListOfZygosity();
        assertThat(zygosityList, notNullValue());
    }

    @Test
    public void getFishByGenotypeCount() {
        String genoID = "ZDB-GENO-030619-2";
        long count = getMutantRepository().getFishCountByGenotype(genoID, "ZDB-PUB-040617-4");
        assertThat(count, greaterThan(0L));
    }

    @Test
    public void getPhenotypeByFishAndPublication() {
        String fishID = "ZDB-FISH-150901-1282";
        Fish fish = getMutantRepository().getFish(fishID);
        long count = getMutantRepository().getPhenotypeByFishAndPublication(fish, "ZDB-PUB-140822-1");
        assertThat(count, greaterThan(0L));
        count = getMutantRepository().getFishExperimentCountByGenotype(fish, "ZDB-PUB-140822-1");
        assertThat(count, greaterThan(0L));
    }

    @Test
    public void getInferredFromCountByGenotype() {
        String genoID = "ZDB-GENO-000412-4";
        String publicationID = "ZDB-PUB-040617-4";
        long count = getMutantRepository().getInferredFromCountByGenotype(genoID, publicationID);
        assertThat(count, greaterThan(0L));
    }
}