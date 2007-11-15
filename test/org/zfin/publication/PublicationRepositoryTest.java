package org.zfin.publication;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.Morpholino;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.GregorianCalendar;
import java.util.List;


public class PublicationRepositoryTest {

    private static PublicationRepository pr = RepositoryFactory.getPublicationRepository();
    private static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();

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


    //@Test
    public void retrieveSinglePublication() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {

            String pubZdbId = insertTestData();
            Publication testPublication = pr.getPublication(pubZdbId);

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
        String zdbID = "ZDB-ANAT-010921-591";
        AnatomyItem term = new AnatomyItem();
        term.setZdbID(zdbID);
        List<MarkerStatistic> list = pr.getAllExpressedMarkers(term, 1, 5);
        assertEquals("5 genes", 5, list.size());
    }

    /**
     * Get all expressed genes for a given anatomy structure
     */
    @Test
    public void getAllExpressedMarkersCount() {
        String termName = "somite";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem term = aoRepository.getAnatomyItem(termName);
        int number = pr.getAllExpressedMarkersCount(term);
        assertEquals("5 genes", 969, number);
    }

    @Test
    public void getAllExpressedGenesWithMutant() {
        // lateral floor plate
        // no olig2 gene as it is expressed only in a mutant fish
        String zdbID = "ZDB-ANAT-050711-66 ";
        AnatomyItem term = new AnatomyItem();
        term.setZdbID(zdbID);
        List<MarkerStatistic> list = pr.getAllExpressedMarkers(term, 1, 10);
        assertEquals("9 genes", 9, list.size());
    }

    @Test
    public void getNumberOfPubWithFigures() {
        String aoZdbID = "ZDB-ANAT-010921-561";
        String zdbID = "ZDB-GENE-980526-36";
        int number = pr.getNumberOfExpressedGenePublicationsWithFigures(zdbID, aoZdbID);
        assertEquals("2 publications", 2, number);
    }

    @Test
    public void getNumberOfFiguresPerAnatomyStructure() {
        String termName = "neural rod";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem term = aoRepository.getAnatomyItem(termName);

        int number = pr.getTotalNumberOfFiguresPerAnatomyItem(term);
        assertEquals("200 images", 213, number);
    }

    @Test
    public void getNumberOfFiguresPerAnatomyStructureLateralFloorPlate() {
        String termName = "lateral floor plate";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem term = aoRepository.getAnatomyItem(termName);
        int number = pr.getTotalNumberOfFiguresPerAnatomyItem(term);
        assertEquals("13 images", 14, number);
    }

    @Test
    public void getNumberOfImagesPerAnatomyStructure() {
        String termName = "neural rod";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem term = aoRepository.getAnatomyItem(termName);
        int number = pr.getTotalNumberOfImagesPerAnatomyItem(term);
        assertEquals("518 images", 518, number);
    }

    @Test
    public void getAllExpressedMutants() {
        // lateral floor plate
        String aoZdbID = "ZDB-ANAT-050711-66 ";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Genotype> list = mutantRepository.getGenotypesByAnatomyTerm(item, false, 4);
        assertEquals("8 genes", 4, list.size());
    }

    @Test
    public void getNumberOfPublicationsPerAnatomyStructureAndGli1Mutant() {
        // Anatomical Structure: lateral floor plate
        String aoZdbID = "ZDB-ANAT-050711-66";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        // Genotype: gli1^te370a/+ 
        String genotypeZdbID = "ZDB-GENO-070307-4";
        Genotype genotype = new Genotype();
        genotype.setZdbID(genotypeZdbID);
        int number = mutantRepository.getNumberOfPublicationsPerAnatomyAndMutantWithFigures(item, genotype);
        assertEquals("1 publication",1, number);
    }

    @Test
    public void getMorpholinos() {
        //  locus coeruleus
        String aoZdbID = "ZDB-ANAT-011113-460";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Morpholino> morphs = mutantRepository.getPhenotypeMorhpolinosByAnatomy(item, 4);
        assertTrue(morphs != null);
        assertEquals("13 figures", 3, morphs.size());

    }

    @Test
    public void getFiguresForProbesAndPublication() {
        //  probe eu815
        String probeZdbID = "ZDB-EST-060130-371";
        // publication
        String pubZdbID = "ZDB-PUB-051025-1";
        List<Figure> figs = pr.getFiguresByProbeAndPublication(probeZdbID, pubZdbID);
        assertTrue(figs != null);
        assertEquals("6 figures", 6, figs.size());

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
        List<Figure> figs = pr.getFiguresPerProbeAndAnatomy(gene, probe, item);
        assertTrue(figs != null);
        assertEquals("1 figures", 1, figs.size());

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
        List<Publication> pubs = pr.getPublicationsWithFiguresPerProbeAndAnatomy(gene, probe, item);
        assertTrue(pubs != null);
        assertEquals("1 publication", 1, pubs.size());

    }

    /**
     * anterior lateral line ganglia is expressed in a double mutant: fgf3t24149/+;fgf8ti282a/+
     */
    @Test
    public void getMutantByAnatomyExpression() {
        //  somite 8
        String aoZdbID = "ZDB-ANAT-020309-228";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Genotype> genotypes = mutantRepository.getGenotypesByAnatomyTerm(item, false, 5);

        assertTrue(genotypes != null);

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
        List<Figure> figs = pr.getFiguresByGenoAndAnatomy(geno, item);
        assertTrue(figs != null);
        assertEquals("1 figure", 1, figs.size());

    }

    @Test
    public void getFiguresForMorpholino() {
        //  morhpolino MO1-nkx2.2a
        String morpholinoZdbID = "ZDB-MRPHLNO-070305-1";
        Morpholino morpholino = new Morpholino();
        morpholino.setZdbID(morpholinoZdbID);
        //  lateral floor plate
        String aoZdbID = "ZDB-ANAT-050711-66";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Figure> figs = pr.getFiguresByMorpholinoAndAnatomy(morpholino, item);
        assertTrue(figs != null);
        assertEquals("1 figure", 1, figs.size());

    }

    @Test
    public void getPublicationsForFiguresForGenotype() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-GENO-020426-5";
        Genotype geno = new Genotype();
        geno.setZdbID(genoZdbID);
        // brain
        String aoZdbID = "ZDB-ANAT-010921-415";
        AnatomyItem item = new AnatomyItem();
        item.setZdbID(aoZdbID);
        List<Publication> publications = pr.getPublicationsWithFiguresPerGenotypeAndAnatomy(geno, item);
        assertTrue(publications != null);
        assertEquals("1 publication", 1, publications.size());

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
        List<Figure> figs = pr.getFiguresByGeneAndAnatomy(marker, item);
        assertTrue(figs != null);
        assertEquals("1 figure", 1, figs.size());

    }

    @Test
    public void getHighQualityProbePublicationsForBrain() {
        String termName = "neural rod";
        AnatomyRepository aoRepository = RepositoryFactory.getAnatomyRepository();
        AnatomyItem item = aoRepository.getAnatomyItem(termName);
        List<Publication> qualityPubs = pr.getHighQualityProbePublications(item);
        assertTrue(qualityPubs != null);
        assertEquals("2 pubs", 2, qualityPubs.size());

    }
}
