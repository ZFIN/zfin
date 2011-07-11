package org.zfin.mutant.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.expression.Experiment;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.expression.FigureFigure;
import org.zfin.expression.repository.ExpressionRepository;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.dto.*;
import org.zfin.gwt.root.server.DTOConversionService;
import org.zfin.marker.Marker;
import org.zfin.mutant.*;
import org.zfin.ontology.*;
import org.zfin.publication.Publication;
import org.zfin.publication.presentation.FigureLink;
import org.zfin.publication.presentation.PublicationLink;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;

import java.util.List;

import static junit.framework.Assert.*;
import static org.junit.Assert.fail;
import static org.zfin.repository.RepositoryFactory.getMutantRepository;
import static org.zfin.repository.RepositoryFactory.getPhenotypeRepository;

/**
 * Test class for PhenotypeRepository.
 */
@SuppressWarnings({"SuppressionAnnotation", "NonBooleanMethodNameMayNotStartWithQuestion"})
public class PhenotypeRepositoryTest extends AbstractOntologyTest {

    private static PublicationRepository pubRepository = RepositoryFactory.getPublicationRepository();
    private static AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
    private static ExpressionRepository expressionRepository = RepositoryFactory.getExpressionRepository();

    static {
        TestConfiguration.configure();
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        if (sessionFactory == null) {
            new HibernateSessionCreator();
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.setAuthenticatedUser();
    }


    /**
     * phenotype structure pile for  ZDB-PUB-970210-18
     */
    @Test
    public void retrievePhenotypeStructures() {
        //String pubID = "ZDB-PUB-970210-18";
        String pubID = "ZDB-PUB-961014-496";

        List<PhenotypeStructure> structures = getPhenotypeRepository().retrievePhenotypeStructures(pubID);
        assertNotNull(structures);
        for (PhenotypeStructure structure : structures) {
            System.out.println(structure.getEntity().getSuperterm().getTermName());
            System.out.println(structure.getQualityTerm().getTermName());
        }
    }

    @SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion"})
    @Test
    public void isPhenotypeStructureOnPile() throws TermNotFoundException {
        // Antibody data submission
        // this publication should not have any phenotypes annotated to
        String publicationID = "ZDB-PUB-080117-1";
        Publication pub = pubRepository.getPublication(publicationID);
        List<PhenotypeStructure> structures = getPhenotypeRepository().retrievePhenotypeStructures(publicationID);
        assertTrue(structures == null || structures.size() == 0);
        PhenotypeStatementDTO phenotypeDTOE1a = createAnatomyPhenotypeDTO("eye", null, "degenerate", null, null, true);
        PhenotypeStatementDTO phenotypeDTOE1aE1b = createAnatomyPhenotypeDTO("eye", "melanoblast", "degenerate", null, null, true);

        PhenotypeStructure structureE1a = DTOConversionService.getPhenotypeStructure(phenotypeDTOE1a);
        structureE1a.setPublication(pub);
        PhenotypeStructure structureE1aE1b = DTOConversionService.getPhenotypeStructure(phenotypeDTOE1aE1b);
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            getPhenotypeRepository().createPhenotypeStructure(structureE1aE1b, publicationID);
            boolean isOnPile = getPhenotypeRepository().isPhenotypeStructureOnPile(structureE1aE1b);
            assertTrue(isOnPile);
            // make sure this can be put onto the pile: see FB case 5772
            isOnPile = getPhenotypeRepository().isPhenotypeStructureOnPile(structureE1a);
            assertFalse(isOnPile);

        } finally {
            tx.rollback();
        }
    }

    @SuppressWarnings({"NonBooleanMethodNameMayNotStartWithQuestion"})
    @Test
    public void isRelatedPhenotypeStructureOnPile() throws TermNotFoundException {
        // Antibody data submission
        // this publication should not have any phenotypes annotated to
        String publicationID = "ZDB-PUB-080117-1";
        Publication pub = pubRepository.getPublication(publicationID);
        List<PhenotypeStructure> structures = getPhenotypeRepository().retrievePhenotypeStructures(publicationID);
        assertTrue(structures == null || structures.size() == 0);
        PhenotypeStatementDTO phenotypeDTOE2a = createAnatomyPhenotypeDTO("eye", null, "degenerate", "brainstem", null, true);
        PhenotypeStatementDTO phenotypeDTOE2aE2b = createAnatomyPhenotypeDTO("eye", null, "degenerate", "brainstem", "cerebellum", true);

        PhenotypeStructure structureE2a = DTOConversionService.getPhenotypeStructure(phenotypeDTOE2a);
        structureE2a.setPublication(pub);
        PhenotypeStructure structureE2aE2b = DTOConversionService.getPhenotypeStructure(phenotypeDTOE2aE2b);
        Transaction tx = HibernateUtil.currentSession().beginTransaction();
        try {
            getPhenotypeRepository().createPhenotypeStructure(structureE2aE2b, publicationID);
            boolean isOnPile = getPhenotypeRepository().isPhenotypeStructureOnPile(structureE2aE2b);
            assertTrue(isOnPile);
            // make sure this can be put onto the pile: see FB case 5772
            isOnPile = getPhenotypeRepository().isPhenotypeStructureOnPile(structureE2a);
            assertFalse(isOnPile);

        } finally {
            tx.rollback();
        }
    }

    @Test
    public void getPhenotypeExperiment() {
        String genotypeID = "ZDB-GENO-070119-8";
        Genotype genotype = getMutantRepository().getGenotypeByID(genotypeID);
        GenotypeExperiment genoExperiment = new GenotypeExperiment();
        genoExperiment.setGenotype(genotype);
        PhenotypeExperiment phenotypeExperimentFilter = new PhenotypeExperiment();
        phenotypeExperimentFilter.setGenotypeExperiment(genoExperiment);
        DevelopmentStage start = new DevelopmentStage();
        start.setZdbID("ZDB-STAGE-010723-39");
        phenotypeExperimentFilter.setEndStage(start);
        phenotypeExperimentFilter.setStartStage(start);
        Experiment experiment = new Experiment();
        experiment.setZdbID("ZDB-EXP-041102-1");
        genoExperiment.setExperiment(experiment);
        Figure figure = new FigureFigure();
        figure.setZdbID("ZDB-FIG-060214-4");
        phenotypeExperimentFilter.setFigure(figure);
        PhenotypeExperiment phenoExperiment = getPhenotypeRepository().getPhenotypeExperiment(phenotypeExperimentFilter);
        assertNotNull(phenoExperiment);
    }

    private PhenotypeStatementDTO createAnatomyPhenotypeDTO(String e1a, String e1b, String quality, String e2a, String e2b, boolean abnormal) {
        PhenotypeStatementDTO phenotypeDTO = new PhenotypeStatementDTO();
        EntityDTO entityDTO = new EntityDTO();
        entityDTO.setSuperTerm(createTerm(e1a, OntologyDTO.ANATOMY));
        if (e1b != null) {
            entityDTO.setSubTerm(createTerm(e1b, OntologyDTO.ANATOMY));
        }
        phenotypeDTO.setEntity(entityDTO);
        if (e2a != null) {
            EntityDTO relatedEntityDTO = new EntityDTO();
            relatedEntityDTO.setSuperTerm(createTerm(e2a, OntologyDTO.ANATOMY));
            if (e2b != null) {
                relatedEntityDTO.setSubTerm(createTerm(e2b, OntologyDTO.ANATOMY));
            }
            phenotypeDTO.setRelatedEntity(relatedEntityDTO);
        }
        phenotypeDTO.setQuality(createTerm(quality, OntologyDTO.QUALITY));
        if (abnormal)
            phenotypeDTO.setTag(PhenotypeStatement.Tag.ABNORMAL.toString());
        else
            phenotypeDTO.setTag(PhenotypeStatement.Tag.NORMAL.toString());
        return phenotypeDTO;
    }

    @Test
    public void retrievePhenotypeStructure() {
        String zdbID = "ZDB-API-100331-10";
        PhenotypeStructure structure = getPhenotypeRepository().getPhenotypeStructureByID(zdbID);
        assertNotNull(structure);
        assertEquals("fin regeneration", structure.getEntity().getSuperterm().getTermName());

    }

    @Test
    public void retrieveMutantExpressions() {
        String publicationID = "ZDB-PUB-970210-18";
        List<PhenotypeExperiment> structure = getPhenotypeRepository().getMutantExpressionsByFigureFish(publicationID, null, null, null);
        assertNotNull(structure);

    }

    @Test
    public void retrieveMultiMutantExpressions() {
        String publicationID = "ZDB-PUB-090731-2";
        List<PhenotypeExperiment> structure = getPhenotypeRepository().getMutantExpressionsByFigureFish(publicationID, null, null, null);
        assertNotNull(structure);

    }

    @Test
    public void retrievePhenotypePileStructure() {
        String patoID = "ZDB-API-100331-10";
        PhenotypeStructure structure = getPhenotypeRepository().getPhenotypePileStructure(patoID);
        assertNotNull(structure);
        Term superTerm = structure.getEntity().getSuperterm();
        assertNotNull(superTerm);
        assertEquals("fin regeneration", superTerm.getTermName());
        assertEquals("biological_process", superTerm.getOntology().getOntologyName());

    }

    @Test
    public void getGoPhenotype() {
        // this phenotype has a single go term as a superterm.
        long patoID = 647;
        Session session = HibernateUtil.currentSession();
        PhenotypeStatement phenotype = (PhenotypeStatement) session.get(PhenotypeStatement.class, patoID);
        assertNotNull(phenotype);
    }

    @Test
    public void getDistinctPhenotypePileStructures() {
        // Johnson et al. 1995
        String publicationID = "ZDB-PUB-961014-496";
        List<PhenotypeExperiment> phenotypes = getPhenotypeRepository().getAllPhenotypes(publicationID);
        assertNotNull(phenotypes);
    }

    @Test
    public void checkIfPhenotypeStructureExists() {
        PhenotypeStructure structure = new PhenotypeStructure();
        structure.setTag(PhenotypeStatement.Tag.ABNORMAL);
        PostComposedEntity entity = new PostComposedEntity();
        GenericTerm superterm = new GenericTerm();
        superterm.setZdbID("ZDB-TERM-091209-5266");
        entity.setSuperterm(superterm);
        structure.setEntity(entity);
        Publication pub = new Publication();
        pub.setZdbID("ZDB-PUB-961014-496");
        structure.setPublication(pub);
        boolean exists = getPhenotypeRepository().isPhenotypeStructureOnPile(structure);
        assertFalse(exists);
    }

    @Test
    public void createPhenotypePile() {
        // Antibody Data Submissions
        String publicationID = "ZDB-PUB-080117-1";

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            getPhenotypeRepository().createPhenotypePile(publicationID);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }
    }

    @Test
    public void regenGenofigGenotype() {
        PhenotypeStatement phenotype = new PhenotypeStatement();
        GenotypeExperiment genox = new GenotypeExperiment();
        genox.setZdbID("ZDB-GENOX-041102-1948");
        Genotype geno = new Genotype();
        geno.setZdbID("ZDB-GENO-980202-405");
        genox.setGenotype(geno);
        Experiment experiment = new Experiment();
        experiment.setZdbID("ZDB-EXP-041102-1");
        genox.setExperiment(experiment);
        PhenotypeExperiment phenox = new PhenotypeExperiment();
        phenox.setGenotypeExperiment(genox);

        phenotype.setPhenotypeExperiment(phenox);

        HibernateUtil.createTransaction();
        //getPhenotypeRepository().runRegenGenotypeFigureScript(genox);
        HibernateUtil.flushAndCommitCurrentSession();

    }

    @Test
    public void createPatoRecord() {
        String expressionExperimentID = "ZDB-XPAT-081003-1";
        String genoxID = "ZDB-GENOX-041102-700";
        String figureID = "ZDB-FIG-050720-1";
        String startID = "ZDB-STAGE-010723-4";
        String endID = "ZDB-STAGE-010723-4";
        String pubZdbId = "ZDB-PUB-990507-16";

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {

            ExpressionExperiment expressionExperiment = expressionRepository.getExpressionExperiment(expressionExperimentID);
            Figure figure = pubRepository.getFigureByID(figureID);
            DevelopmentStage start = anatomyRepository.getStageByID(startID);
            DevelopmentStage end = anatomyRepository.getStageByID(endID);
            PhenotypeExperiment phenoExperiment = new PhenotypeExperiment();
            phenoExperiment.setFigure(figure);
            phenoExperiment.setStartStage(start);
            phenoExperiment.setEndStage(end);
            phenoExperiment.setGenotypeExperiment(expressionExperiment.getGenotypeExperiment());
            getPhenotypeRepository().createPhenotypeExperiment(phenoExperiment);
            assertTrue(phenoExperiment.getId() > 0);
            // no default statement is created. 
            assertNull(phenoExperiment.getPhenotypeStatements());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            // rollback on success or exception to leave no new records in the database
            session.getTransaction().rollback();
        }


    }

    @Test
    public void getPhenotypeExperimentsWithoutAnnotation() {
        String publicationID = "ZDB-PUB-101011-54";
        List<PhenotypeExperiment> phenotypeExperiments =
                getPhenotypeRepository().getPhenotypeExperimentsWithoutAnnotation(publicationID);
        assertEquals(0, phenotypeExperiments.size());
    }

    @Test
    public void getExperimentsHistory() {
        List<PhenotypeExperiment> phenotypeExperiments =
                getPhenotypeRepository().getLatestPhenotypeExperiments(3);
        //assertEquals(0, phenotypeExperiments.size());
    }

    @Test
    public void getPhenotypeStatementHistory() {
        List<PhenotypeStatement> phenotypeStatements =
                getPhenotypeRepository().getLatestPhenotypeStatements(0, 2);
        //assertEquals(0, phenotypeStatements.size());
    }

    private TermDTO createTerm(String supertermName, OntologyDTO ontology) {
        TermDTO dto = new TermDTO();
        dto.setName(supertermName);
        dto.setOntology(ontology);
        return dto;
    }


    @Override
    protected Ontology[] getOntologiesToLoad() {
        Ontology[] ontologies = new Ontology[3];
        ontologies[0] = Ontology.ANATOMY;
        ontologies[1] = Ontology.QUALITY;
        ontologies[2] = Ontology.STAGE;
        return ontologies;
    }

    @Test
    public void getNumberPhenotypeFigures(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int numFigures = getPhenotypeRepository().getNumPhenotypeFigures(m);
        assertTrue(numFigures>10);
        assertTrue(numFigures<20);
    }


    @Test
    public void getNumberPhenotypePublications(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int numPubs  = getPhenotypeRepository().getNumPhenotypePublications(m);
        assertTrue(numPubs >5);
        assertTrue(numPubs < 20);
    }

    @Test
    public void getFirstPhenotypePublication(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-980526-333");
        PublicationLink publicationLink = getPhenotypeRepository().getPhenotypeFirstPublication(m);
        assertEquals("ZDB-PUB-080630-4", publicationLink.getPublicationZdbId());
        assertEquals("Dee <i>et al.</i>, 2008", publicationLink.getLinkContent());
    }

    @Test
    public void getFirstPhenotypeFigure(){
        Marker m = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-MRPHLNO-070305-1");
        FigureLink figureLink = getPhenotypeRepository().getPhenotypeFirstFigure(m);
        assertEquals("ZDB-FIG-070307-8", figureLink.getFigureZdbId());
        assertEquals("Fig. 7", figureLink.getLinkContent());
        assertEquals("<a href=\"/quark/webdriver?MIval=aa-fxfigureview.apg&OID=ZDB-FIG-070307-8\">Fig. 7</a>", figureLink.getLink());
    }

    @Test
    public void getPhenotypeAnatomy(){
        Marker m = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-010606-1");
        int numAnatomy = getPhenotypeRepository().getPhenotypeAnatomy(m).size();
        assertTrue(numAnatomy >10);
        assertTrue(numAnatomy < 40);

        Marker m2 = RepositoryFactory.getMarkerRepository().getGeneByID("ZDB-GENE-031112-7");
        int numAnatomy2 = getPhenotypeRepository().getPhenotypeAnatomy(m2).size();
        assertEquals(63,numAnatomy2 );
    }
}