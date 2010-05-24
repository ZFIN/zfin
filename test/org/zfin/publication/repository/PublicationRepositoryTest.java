package org.zfin.publication.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureService;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.mutant.Feature;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.GregorianCalendar;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;


public class PublicationRepositoryTest {

    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator(TestConfiguration.getHibernateConfiguration());
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
    }


    //    @Test
    public void retrieveSinglePublication() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {

            String pubZdbId = insertTestData();
            Publication testPublication = publicationRepository.getPublication(pubZdbId);

            assertNotNull("Test publication is retrieved", testPublication);
            assertEquals("Test publication has the right title", "test publication", testPublication.getTitle());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }


    }


    public String insertTestData() {
        Session session = HibernateUtil.currentSession();

        // form test data
        Publication testPublication = new Publication();
        testPublication.setTitle("test publication");
        testPublication.setAuthors("Westerfield, M.");
        testPublication.setType("Journal");
        testPublication.setShortAuthorList("Short Authors");
        testPublication.setPublicationDate(new GregorianCalendar(1, 1, 2000));

        Journal testJournal = new Journal();
        testJournal.setName("testJournal");

        testPublication.setJournal(testJournal);

        session.save(testPublication);

        return testPublication.getZdbID();

    }

    /**
     * Get all expressed genes for a given anatomy structure
     */
    @Test
    public void runGetAllExpressedGenes() {
        // somite
        String zdbID = "ZDB-TERM-100331-144";
        Term term = new GenericTerm();
        term.setID(zdbID);
        PaginationResult<MarkerStatistic> paginationResult = publicationRepository.getAllExpressedMarkers(term, 0, 5);
        List<MarkerStatistic> list = paginationResult.getPopulatedResults();
        assertEquals("5 genes", 5, paginationResult.getPopulatedResults().size());
        assertTrue(list.size() < paginationResult.getTotalCount());
    }

    /**
     * Get all expressed genes for a given anatomy structure
     */
    @Test
    public void getAllExpressedMarkersCount() {
        String termName = "somite";
        Term term = ontologyRepository.getTermByName(termName, Ontology.ANATOMY);
        int number1 = publicationRepository.getAllExpressedMarkers(term, 0, 5).getTotalCount();
        int number2 = publicationRepository.getAllExpressedMarkers(term, 12, 20).getTotalCount();
        assertTrue(number1 > 0);
        assertEquals(number1, number2);
    }

    @Test
    public void getAllExpressedGenesWithMutant() {
        // lateral floor plate
        // no olig2 gene as it is expressed only in a mutant fish
        String zdbID = "ZDB-TERM-100331-1214";
        Term term = new GenericTerm();
        term.setID(zdbID);
        List<MarkerStatistic> list = publicationRepository.getAllExpressedMarkers(term, 0, 10).getPopulatedResults();
//        assertEquals("10 genes", 10, list.size());
        assertNotNull(list);
    }

    @Test
    public void getNumberOfPubWithFigures() {
        //  neural rod
        String aoZdbID = "ZDB-TERM-100331-125";
        String zdbID = "ZDB-GENE-980526-36";
        int number = publicationRepository.getNumberOfExpressedGenePublicationsWithFigures(zdbID, aoZdbID);
//        assertEquals("2 publications", 2, number);
        assertTrue(number > 0);
    }

    @Test
    public void getNumberOfFiguresPerAnatomyStructure() {
        String termName = "neural rod";
        Term term = ontologyRepository.getTermByName(termName, Ontology.ANATOMY);
        assertNotNull(term);

        int number = publicationRepository.getTotalNumberOfFiguresPerAnatomyItem(term);
//        assertEquals("200 images", 213, number);
        assertTrue(number > 0);
    }

    @Test
    public void getNumberOfFiguresPerAOFloorPlate() {
        String termName = "floor plate";
        Term term = ontologyRepository.getTermByName(termName, Ontology.ANATOMY);
        assertNotNull(term);
        int number = publicationRepository.getTotalNumberOfFiguresPerAnatomyItem(term);
//        assertEquals("13 images", 14, number);
        assertTrue(number > 0);
    }

    @Test
    public void getNumberOfImagesPerAnatomyStructure() {
        String termName = "neural rod";
        Term term = ontologyRepository.getTermByName(termName, Ontology.ANATOMY);
        int number = publicationRepository.getTotalNumberOfImagesPerAnatomyItem(term);
//        assertEquals("518 images", 518, number);
        assertTrue(number > 0);
    }

    @Test
    public void getAllExpressedMutants() {
        // lateral floor plate
        String aoZdbID = "ZDB-TERM-100331-1214";
        Term item = new GenericTerm();
        item.setID(aoZdbID);
        PaginationResult<Genotype> genotypeResult = mutantRepository.getGenotypesByAnatomyTerm(item, false, 4);
//        assertEquals("8 genes", 4, list.size());
        assertNotNull(genotypeResult.getPopulatedResults());
        assertEquals(genotypeResult.getPopulatedResults().size(), 4);
        assertTrue(genotypeResult.getTotalCount() > 4);
    }

    @Test
    public void getNumOfPublicationsPerAOAndGli1Mutant() {
        // lateral floor plate
        String aoZdbID = "ZDB-TERM-100331-1214";
        Term item = new GenericTerm();
        item.setID(aoZdbID);
        // Genotype: gli1^te370a/+
        String genotypeZdbID = "ZDB-GENO-070307-4";
        Genotype genotype = new Genotype();
        genotype.setZdbID(genotypeZdbID);
        int number = mutantRepository.getNumberOfPublicationsPerAnatomyAndMutantWithFigures(item, genotype);
//        assertEquals("1 publication",1, number);
        assertTrue(number > 0);
    }

    @Test
    public void getMorpholinos() {
        //  locus coeruleus
        String aoZdbID = "ZDB-ANAT-011113-460";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Morpholino> morphs = mutantRepository.getPhenotypeMorpholinos(item, 4);
        assertTrue(morphs != null);
//        assertEquals("13 figures", 3, morphs.size());

    }

    @Test
    public void getFiguresForProbesAndPublication() {
        //  probe eu815
        String probeZdbID = "ZDB-EST-060130-371";
        // publication
        String pubZdbID = "ZDB-PUB-051025-1";
        List<Figure> figs = publicationRepository.getFiguresByProbeAndPublication(probeZdbID, pubZdbID);
        assertTrue(figs != null);
//        assertEquals("6 figures", 6, figs.size());

    }

    @Test
    public void getFiguresForProbes() {
        //  probe eu815
        String probeZdbID = "ZDB-EST-051103-38";
        Marker probe = new Marker();
        probe.setZdbID(probeZdbID);
        //  gene ascl1b
        String geneZdbID = "ZDB-GENE-980526-174";
        Marker gene = new Marker();
        gene.setZdbID(geneZdbID);
        // neural rod
        String aoZdbID = "ZDB-ANAT-010921-561";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Figure> figs = publicationRepository.getFiguresPerProbeAndAnatomy(gene, probe, item);
        assertTrue(figs != null);
//        assertEquals("1 figures", 1, figs.size());

    }

    @Test
    public void getFigurePublicationsForProbes() {
        //  probe eu815
        String probeZdbID = "ZDB-EST-051103-38";
        Marker probe = new Marker();
        probe.setZdbID(probeZdbID);
        //  gene ascl1b
        String geneZdbID = "ZDB-GENE-980526-174";
        Marker gene = new Marker();
        gene.setZdbID(geneZdbID);
        // neural rod
        String aoZdbID = "ZDB-ANAT-010921-561";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Publication> pubs = publicationRepository.getPublicationsWithFiguresPerProbeAndAnatomy(gene, probe, item);
        assertTrue(pubs != null);
//        assertEquals("1 publication", 1, pubs.size());

    }

    /**
     * anterior lateral line ganglia is expressed in a double mutant: fgf3t24149/+;fgf8ti282a/+
     */
    @Test
    public void getMutantByAnatomyExpression() {
        //  midbrain
        String aoZdbID = "ZDB-TERM-100331-121";
        Term item = new GenericTerm();
        item.setID(aoZdbID);
        PaginationResult<Genotype> genotypeResult = mutantRepository.getGenotypesByAnatomyTerm(item, false, 5);

        assertNotNull(genotypeResult);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertEquals(5, genotypeResult.getPopulatedResults().size());
        assertTrue(genotypeResult.getTotalCount() > 5);

    }

    @Test
    public void getFiguresForGenotype() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-GENO-020426-5";
        Genotype geno = new Genotype();
        geno.setZdbID(genoZdbID);
        // brain
        String aoZdbID = "ZDB-ANAT-010921-415";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        PaginationResult<Figure> figs = publicationRepository.getFiguresByGenoAndAnatomy(geno, item);
        assertTrue(figs.getPopulatedResults() != null);
//        assertEquals("1 figure", 1, figs.size());

    }
    @Test
    public void getFiguresForGeno() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-GENO-980202-822";
        Genotype geno = new Genotype();
        geno.setZdbID(genoZdbID);
        // brain

        PaginationResult<Figure> figs = publicationRepository.getFiguresByGeno(geno);
        assertTrue(figs.getPopulatedResults() != null);
//        assertEquals("1 figure", 1, figs.size());

    }


    @Test
    public void getPubsForFeature() {
        //  genotype adss^hi1433Tg
        String featZdbID = "ZDB-ALT-980413-502";
        Feature feature = new Feature();
        feature.setZdbID(featZdbID);
        // brain

        PaginationResult<Publication> pubs= publicationRepository.getAllAssociatedPublicationsForFeature(feature,0);
        assertTrue(pubs.getPopulatedResults() != null);
//        assertEquals("1 figure", 1, figs.size());

    }
    @Test
    public void getFiguresForGenotypeExp() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-GENO-020426-5";
        Genotype geno = new Genotype();
        geno.setZdbID(genoZdbID);
        // brain
        

        PaginationResult<Figure> figs = publicationRepository.getFiguresByGenoExp(geno);
        assertTrue(figs.getPopulatedResults() != null);
//        assertEquals("1 figure", 1, figs.size());

    }

    @Test
    public void getFiguresForMorpholino() {
        //  morhpolino MO1-nkx2.2a
        String morpholinoZdbID = "ZDB-MRPHLNO-070305-1";
        Morpholino morpholino = new Morpholino();
        morpholino.setZdbID(morpholinoZdbID);
        //  lateral floor plate
        String aoZdbID = "ZDB-TERM-100331-1214";
        Term item = new GenericTerm();
        item.setID(aoZdbID);
        List<Figure> figs = publicationRepository.getFiguresByMorpholinoAndAnatomy(morpholino, item);
        assertTrue(figs != null);
//        assertEquals("1 figure", 1, figs.size());

    }

    @Test
    public void getPublicationsForFiguresForGenotype() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-GENO-020426-5";
        Genotype geno = new Genotype();
        geno.setZdbID(genoZdbID);
        // brain
        String aoZdbID = "ZDB-TERM-100331-8";
        Term item = new GenericTerm();
        item.setID(aoZdbID);
        int publicationCount = publicationRepository.getNumPublicationsWithFiguresPerGenotypeAndAnatomy(geno, item);
        assertTrue(publicationCount > 0);
//        assertEquals("1 publication", 1, publications.size());

    }

    @Test
    public void getPublicationsForFiguresForGeno() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-GENO-070615-1";
        Genotype geno = new Genotype();
        geno.setZdbID(genoZdbID);
        // brain

         PaginationResult<Publication> pubs = publicationRepository.getPublicationsWithFiguresbyGeno(geno);
        assertTrue(pubs.getPopulatedResults() != null);
//        assertEquals("1 publication", 1, publications.size());

    }

    @Test
    public void getFiguresForGene() {
        //  foxi1
        String markerZdbID = "ZDB-GENE-030505-1";
        Marker marker = new Marker();
        marker.setZdbID(markerZdbID);
        //   presumptive ectoderm
        String aoZdbID = "ZDB-ANAT-060131-50";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Figure> figs = publicationRepository.getFiguresByGeneAndAnatomy(marker, item);
        assertTrue(figs != null);
//        assertEquals("1 figure", 1, figs.size());

    }

    @Test
    public void getHighQualityProbePublicationsForBrain() {
        String termName = "neural rod";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem item = aoRepository.getAnatomyItem(termName);
        List<Publication> qualityPubs = publicationRepository.getHighQualityProbePublications(item);
        assertTrue(qualityPubs != null);
//        assertEquals("2 pubs", 2, qualityPubs.size());

    }

    @Test
    public void getExpressedGenesForFigure() {
        Figure fig = publicationRepository.getFigureByID("ZDB-FIG-080617-24"); //has xpat, pheno & AB

        List<Marker> expressedGenes = FigureService.getExpressionGenes(fig);
        assertNotNull("FigureService.getExpressedGenes doesn't return a null", expressedGenes);
        for (Marker gene : expressedGenes) {
            assertTrue("expressed genes for a figure should all be genes", gene.isInTypeGroup(Marker.TypeGroup.GENEDOM));
        }

    }

    @Test
    public void getNumberOfPublicationForPax2aAndMHB() {
        String termName = "midbrain hindbrain boundary";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem item = aoRepository.getAnatomyItem(termName);
        Marker pax2a = RepositoryFactory.getMarkerRepository().getMarkerByAbbreviation("pax2a");

        PaginationResult<Publication> qualityPubs = publicationRepository.getPublicationsWithFigures(pax2a, item);
        assertTrue(qualityPubs != null);
//        assertEquals("2 pubs", 2, qualityPubs.size());

    }
    @Test


    
    public void getMarkersPerPublication() {
        String zdbID = "ZDB-PUB-990507-16";

        List<Marker> experiments = publicationRepository.getGenesByPublication(zdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getFigureLabels() {
        String zdbID = "ZDB-PUB-990507-16";

        List<String> experiments = publicationRepository.getDistinctFigureLabels(zdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getExpressionExperiments() {
        String zdbID = "ZDB-PUB-990507-16";

        List<ExpressionExperiment> experiments = publicationRepository.getExperiments(zdbID);
        assertTrue(experiments != null);
        // alcam
        String geneID = "ZDB-GENE-990415-30";
        experiments = publicationRepository.getExperimentsByGeneAndFish(zdbID, geneID, null);
        assertTrue(experiments != null);

        // alcam and WT
        String fishName = "WT";
        experiments = publicationRepository.getExperimentsByGeneAndFish(zdbID, geneID, fishName);
        assertTrue(experiments != null);
    }

    @Test
    public void getFishByPublicationInExperiment() {
        String zdbID = "ZDB-PUB-990507-16";

        List<Genotype> experiments = publicationRepository.getFishUsedInExperiment(zdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getFishByPublication() {
        String zdbID = "ZDB-PUB-970210-18";

        List<Genotype> experiments = publicationRepository.getGenotypesInPublication(zdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getExperimentsByPublications() {
        String zdbID = "ZDB-PUB-990507-16";

        List<Experiment> experiments = publicationRepository.getExperimentsByPublication(zdbID);
        assertTrue(experiments != null);
    }

    @Test
    public void getWTGenotype() {
        String nickname = "WT";

        Genotype geno = publicationRepository.getGenotypeByHandle(nickname);
        assertTrue(geno != null);
    }

    @Test
    public void getGenotypeByPublicationAttribution() {
        String zdbID = "ZDB-PUB-990507-16";

        List<Genotype> genotypes = publicationRepository.getNonWTGenotypesByPublication(zdbID);
        assertTrue(genotypes != null);
    }

    @Test
    public void getAntibodiesByPublicationAttribution() {
        String zdbID = "ZDB-PUB-990507-16";

        List<Antibody> antibodyList = publicationRepository.getAntibodiesByPublication(zdbID);
        assertTrue(antibodyList != null);
    }

    @Test
    public void getAntibodiesByPubAttributionAndGene() {
        String zdbID = "ZDB-PUB-990507-16";
        String geneID = "ZDB-GENE-990415-30";

        List<Antibody> antibodyList = publicationRepository.getAntibodiesByPublicationAndGene(zdbID, geneID);
        assertTrue(antibodyList != null);
    }

    @Test
    public void getGenesByPubAttributionAndAntibody() {
        String zdbID = "ZDB-PUB-990507-16";
        // zn-5
        String antibodyID = "ZDB-ATB-081002-19 ";

        List<Marker> antibodyList = publicationRepository.getGenesByAntibody(zdbID, antibodyID);
        assertTrue(antibodyList != null);
    }

    @Test
    public void getGenesByPublicationAttribution() {
        String zdbID = "ZDB-PUB-080306-3";

        List<Marker> geneList = publicationRepository.getGenesByPublication(zdbID);
        assertTrue(geneList != null);
    }

    @Test
    public void getAccessionBYGenesByPublication() {
        String zdbID = "ZDB-PUB-990507-16";
        // alcam
        String geneID = "ZDB-GENE-990415-30";

        List<MarkerDBLink> antibodyList = publicationRepository.getDBLinksByGene(zdbID, geneID);
        assertTrue(antibodyList != null);
    }

    @Test
    public void getAccessionForAssociatedEst() {
        String zdbID = "ZDB-PUB-990507-16";
        // kif11 has one EST as a probe 
        String geneID = "ZDB-GENE-020426-1";

        List<MarkerDBLink> cloneDBLinks = publicationRepository.getDBLinksForCloneByGene(zdbID, geneID);
        assertTrue(cloneDBLinks != null);
    }

    @Test
    public void retrieveFiguresFromPublication() {
        String pubID = "ZDB-PUB-060105-3";

        List<Figure> figures = publicationRepository.getFiguresByPublication(pubID);
        assertNotNull(figures);
    }


}

