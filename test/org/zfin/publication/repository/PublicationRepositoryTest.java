package org.zfin.publication.repository;

import org.hibernate.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.expression.Experiment;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.expression.ImageStage;
import org.zfin.feature.Feature;
import org.zfin.figure.service.FigureViewService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.api.FieldFilter;
import org.zfin.framework.api.Pagination;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerStatistic;
import org.zfin.marker.presentation.GeneBean;
import org.zfin.mutant.Fish;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.mutant.repository.MutantRepository;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.orthology.Ortholog;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;
import org.zfin.publication.PublicationTrackingHistory;
import org.zfin.publication.PublicationTrackingLocation;
import org.zfin.publication.PublicationTrackingStatus;
import org.zfin.publication.presentation.DashboardPublicationBean;
import org.zfin.publication.presentation.DashboardPublicationList;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.MarkerDBLink;

import java.util.Calendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class PublicationRepositoryTest extends AbstractDatabaseTest {

    @Autowired
    private PublicationRepository publicationRepository;

    private static MutantRepository mutantRepository = RepositoryFactory.getMutantRepository();
    private static OntologyRepository ontologyRepository = RepositoryFactory.getOntologyRepository();
    private static AnatomyRepository anatomyRepository = RepositoryFactory.getAnatomyRepository();
    private static FigureViewService figureViewService = new FigureViewService();


    @Test
    public void retrieveSinglePublication() {

        String pubZdbId = "ZDB-PUB-050607-10";
        Publication testPublication = publicationRepository.getPublication(pubZdbId);

        assertNotNull("Test publication is retrieved", testPublication);
        assertEquals("Test publication has the right title", "LZIC regulates neuronal survival during zebrafish development", testPublication.getTitle());
    }

    @Test
    public void getMappingDetailsCount() {
        String pubZdbId = "ZDB-PUB-050607-10";
        Publication testPublication = publicationRepository.getPublication(pubZdbId);
        long number = publicationRepository.getMappingDetailsCount(testPublication);
        assertNotNull("Test publication is retrieved", testPublication);
        assertEquals("Test publication has the right title", "LZIC regulates neuronal survival during zebrafish development", testPublication.getTitle());
    }


    /**
     * Get all expressed genes for a given anatomy structure
     */
    @Test
    public void runGetAllExpressedGenes() {
        // somite
        String zdbID = "ZDB-TERM-100331-665";
        GenericTerm term = new GenericTerm();
        term.setZdbID(zdbID);
        PaginationResult<MarkerStatistic> paginationResult = publicationRepository.getAllExpressedMarkers(term, 0, 5);
        List<MarkerStatistic> list = paginationResult.getPopulatedResults();
        assertEquals("5 genes", 5, paginationResult.getPopulatedResults().size());
        assertTrue(list.size() < paginationResult.getTotalCount());
    }

    @Test
    public void getNumberOfFiguresPerAnatomyStructure() {
        String termName = "neural rod";
        GenericTerm term = ontologyRepository.getTermByName(termName, Ontology.ANATOMY);
        assertNotNull(term);

        int number = publicationRepository.getTotalNumberOfFiguresPerAnatomyItem(term);
//        assertEquals("200 images", 213, number);
        assertTrue(number > 0);
    }

    @Test
    public void getNumberOfFiguresPerAOFloorPlate() {
        String termName = "floor plate";
        GenericTerm term = ontologyRepository.getTermByName(termName, Ontology.ANATOMY);
        assertNotNull(term);
        int number = publicationRepository.getTotalNumberOfFiguresPerAnatomyItem(term);
//        assertEquals("13 images", 14, number);
        assertTrue(number > 0);
    }

    @Test
    public void getAllExpressedMutants() {
        // liver
        String aoZdbID = "ZDB-TERM-100331-116";
        GenericTerm item = new GenericTerm();
        item.setZdbID(aoZdbID);
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(4);
        PaginationResult<Fish> genotypeResult = mutantRepository.getFishByAnatomyTerm(item, false, bean);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertEquals(genotypeResult.getPopulatedResults().size(), 4);
        assertTrue(genotypeResult.getTotalCount() > 4);
    }

    @Test
    public void getAllGoExpressedMutants() {
        // cilium movement involved in cell motility
        String goZdbID = "ZDB-TERM-091209-27755";
        GenericTerm item = new GenericTerm();
        item.setZdbID(goZdbID);
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(4);
        PaginationResult<Fish> genotypeResult = mutantRepository.getFishByAnatomyTerm(item, false, bean);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertTrue(genotypeResult.getTotalCount() > 1);
    }

    @Test
    public void getAllExpressedMutantsForAoIncludingSubstructures() {
        // actinotrichium
        String aoZdbID = "ZDB-TERM-100614-30";
        GenericTerm item = new GenericTerm();
        item.setZdbID(aoZdbID);
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(4);
        PaginationResult<Fish> genotypeResult = mutantRepository.getFishByAnatomyTermIncludingSubstructures(item, false, bean);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertTrue(genotypeResult.getTotalCount() > 2);
    }

    @Test
    public void getAllExpressedMutantsForGoIncludingSubstructures() {
        // cilium movement involved in cell motility
        String goZdbID = "ZDB-TERM-091209-27755";
        GenericTerm item = new GenericTerm();
        item.setZdbID(goZdbID);
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(4);
        PaginationResult<Fish> genotypeResult = mutantRepository.getFishByAnatomyTermIncludingSubstructures(item, false, bean);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertTrue(genotypeResult.getTotalCount() > 0);
    }

    @Test
    public void getNumOfPublicationsPerAOAndGli1Mutant() {
        // lateral floor plate
        String aoZdbID = "ZDB-TERM-100331-1214";
        GenericTerm item = new GenericTerm();
        item.setZdbID(aoZdbID);
        // Genotype: gli1^te370a/+
        String genotypeZdbID = "ZDB-GENO-070307-4";
        Genotype genotype = new Genotype();
        genotype.setZdbID(genotypeZdbID);
        int number = mutantRepository.getNumberOfPublicationsPerAnatomyAndMutantWithFigures(item, genotype);
//        assertEquals("1 publication",1, number);
        assertTrue(number > 0);
    }

    /**
     * anterior lateral line ganglia is expressed in a double mutant: fgf3t24149/+;fgf8ti282a/+
     */
    @Test
    public void getMutantByAnatomyExpression() {
        //  midbrain
        String aoZdbID = "ZDB-TERM-100331-1421";
        GenericTerm item = new GenericTerm();
        item.setZdbID(aoZdbID);
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(5);

        PaginationResult<Fish> genotypeResult = mutantRepository.getFishByAnatomyTerm(item, false, bean);

        assertNotNull(genotypeResult);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertEquals(5, genotypeResult.getPopulatedResults().size());
        assertTrue(genotypeResult.getTotalCount() > 5);

    }

    @Test
    public void getDirtyFishByAnatomy() {
        //  brain
        String termID = "ZDB-TERM-100331-8";
        GenericTerm item = new GenericTerm();
        item.setZdbID(termID);
        PaginationBean bean = new PaginationBean();
        bean.setMaxDisplayRecords(5);

        PaginationResult<Fish> genotypeResult = mutantRepository.getDirtyFishByAnatomyTerm(item, false, bean);

        assertNotNull(genotypeResult);
        assertNotNull(genotypeResult.getPopulatedResults());
        assertEquals(5, genotypeResult.getPopulatedResults().size());
        assertTrue(genotypeResult.getTotalCount() > 5);

    }

    @Test
    public void getFiguresForGenotype() {
        //  genotype adss^hi1433Tg
        String genoZdbID = "ZDB-FISH-020426-5";
        Fish geno = new Fish();
        geno.setZdbID(genoZdbID);
        // brain
        String aoZdbID = "ZDB-ANAT-010921-415";
        GenericTerm item = new GenericTerm();
        item.setZdbID(aoZdbID);
        PaginationResult<Figure> figs = publicationRepository.getFiguresByFishAndAnatomy(geno, item);
        assertNotNull(figs.getPopulatedResults());

/*      This case has two figures where one of them comes from a genotype with MOs and thus should not be retrieved.
        // Df(LG23:acvr1b,sp5l,wnt1,wnt10b)w5/w5
        genoZdbID = "ZDB-GENO-091207-3";
        geno.setZdbID(genoZdbID);
        // midbrain hindbrain boundary
        aoZdbID = "ZDB-TERM-100331-40";
        item.setZdbID(aoZdbID);
        figs = publicationRepository.getFiguresByFishAndAnatomy(geno, item.createGenericTerm());
        assertTrue(figs.getPopulatedResults() != null);
*/
    }

    @Test
    public void getFiguresForGenotypeAndAoPlusSubstructures() {
        Fish fish = mutantRepository.getFish("ZDB-FISH-150901-25831");
        GenericTerm item = getOntologyRepository().getTermByOboID("ZFA:0005435");
        PaginationResult<Figure> figs = publicationRepository.getFiguresByFishAndAnatomy(fish, item, true);
        assertNotNull(figs.getPopulatedResults());
        assertTrue(figs.getPopulatedResults().size() > 0);
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
    public void getFeatureCountForPub() {
        //  genotype adss^hi1433Tg
        String pubZdbID = "ZDB-PUB-140403-2";
        Publication pub = publicationRepository.getPublication(pubZdbID);
        long ftrCount = publicationRepository.getFeatureCount(pub);
        assertTrue(ftrCount > 0);
//        assertEquals("1 publication", 1, publications.size());

    }

    @Test
    public void getFiguresForGene() {
        //  creb1a
        String markerZdbID = "ZDB-GENE-040426-750";
        Marker marker = new Marker();
        marker.setZdbID(markerZdbID);
        //   telencephalic ventricle
        String aoZdbID = "ZDB-TERM-100331-665";
        GenericTerm item = new GenericTerm();
        item.setZdbID(aoZdbID);
        List<Figure> figs = publicationRepository.getFiguresByGeneAndAnatomy(marker, item);
        assertTrue(figs != null);
    }

    @Test
    public void getExpressedGenesForFigure() {
        Figure fig = publicationRepository.getFigureByID("ZDB-FIG-080617-24"); //has xpat, pheno & AB

        List<Marker> expressedGenes = figureViewService.getExpressionGenes(fig);
        assertNotNull("FigureService.getExpressedGenes doesn't return a null", expressedGenes);
        for (Marker gene : expressedGenes) {
            assertTrue("expressed genes for a figure should all be genes", gene.isInTypeGroup(Marker.TypeGroup.GENEDOM));
        }

    }

    @Test
    public void getNumberOfPublicationForPax2aAndMHB() {
        String termName = "midbrain hindbrain boundary";
        OntologyRepository aoRepository = RepositoryFactory.getOntologyRepository();
        GenericTerm item = aoRepository.getTermByName(termName, Ontology.ANATOMY);
        Marker pax2a = getMarkerRepository().getMarkerByAbbreviation("pax2a");

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

    @Test
    public void testImageStage() {
        String imageZdbID = "ZDB-IMAGE-091217-4";
        String imageWithoutStagesZdbID = "ZDB-IMAGE-020322-107";

        Image image = publicationRepository.getImageById(imageZdbID);
        Image imageWithoutStages = publicationRepository.getImageById(imageWithoutStagesZdbID);

        ImageStage imageStage;

        DevelopmentStage start = anatomyRepository.getStageByID(DevelopmentStage.ZYGOTE_STAGE_ZDB_ID);
        DevelopmentStage end = anatomyRepository.getStageByID(DevelopmentStage.ADULT_STAGE_ZDB_ID);

        if (image.getImageStage() == null) {
            imageStage = new ImageStage();
            imageStage.setZdbID(image.getZdbID());
        } else {
            imageStage = image.getImageStage();
        }

        imageStage.setStart(start);
        imageStage.setEnd(end);

        image.setImageStage(imageStage);

        assertNotNull("image has start stage", image.getStart());
        assertNotNull("image has end stage", image.getEnd());
        assertNull("image without stages has null start stage", imageWithoutStages.getStart());
        assertNull("image without stages has null end stage", imageWithoutStages.getEnd());

    }

    @Test
    public void testImageAnatomy() {

        Session session = HibernateUtil.currentSession();

        String imageZdbID = "ZDB-IMAGE-080219-1";

        //this image is a table, ensuring that it should start out without any anatomy
        Image image = publicationRepository.getImageById(imageZdbID);

        assertTrue(image.getTerms().size() == 0);

        GenericTerm liver = ontologyRepository.getTermByName("liver", Ontology.ANATOMY);
        GenericTerm brain = ontologyRepository.getTermByName("brain", Ontology.ANATOMY);

        image.getTerms().add(liver);
        image.getTerms().add(brain);

        session.flush();
        session.refresh(liver);
        session.refresh(brain);

        assertTrue(liver.getImages().contains(image));
        assertTrue(brain.getImages().contains(image));

        image.getTerms().remove(liver);
        image.getTerms().remove(brain);

        session.flush();
        session.refresh(liver);
        session.refresh(brain);

/* I am not sure why this should assert, ie why the bidirectional
        assertFalse(liver.getImages().contains(image));
        assertFalse(brain.getImages().contains(image));
*/

    }

    @Test
    public void getPubByPubmedID() {
        assertEquals(1, publicationRepository.getPublicationByPmid(18056260).size());
    }

    @Test
    public void getNumberAssociatedPublicationsForMarker() {

        Marker m;
        int numberPubs;


        m = getMarkerRepository().getMarkerByID("ZDB-GENE-051005-1");
        numberPubs = publicationRepository.getNumberAssociatedPublicationsForZdbID(m.getZdbID());
        assertThat(numberPubs, greaterThan(15));
        assertThat(numberPubs, lessThan(35));

        m = getMarkerRepository().getMarkerByAbbreviation("pax6a");
        numberPubs = publicationRepository.getNumberAssociatedPublicationsForZdbID(m.getZdbID());
        assertThat(numberPubs, greaterThan(190));
        assertThat(numberPubs, lessThan(500));

    }

    @Test
    public void getPublicationList() {
        assertTrue(publicationRepository.getPubsForDisplay("ZDB-GENE-040426-1855").size() > 10);
        assertTrue(publicationRepository.getPubsForDisplay("ZDB-GENE-051005-1").size() > 15);
        assertEquals(0, publicationRepository.getPubsForDisplay("ZDB-SSLP-000315-3").size());
        assertThat(publicationRepository.getNumberAssociatedPublicationsForZdbID("ZDB-GENE-040426-1855"), greaterThan(10));
        assertTrue(publicationRepository.getNumberAssociatedPublicationsForZdbID("ZDB-GENE-051005-1") > 15);
        assertEquals(0, publicationRepository.getNumberAssociatedPublicationsForZdbID("ZDB-SSLP-000315-3"));
    }

    @Test
    public void getNumberDirectionPublications() {
        int numDirectPubs = publicationRepository.getNumberDirectPublications("ZDB-ATB-081002-19");
        assertThat(numDirectPubs, greaterThan(150));
        assertThat(numDirectPubs, lessThan(300));
    }

    @Test
    public void getExpressedGenePublications() {

        List<Publication> pubs = publicationRepository.getExpressedGenePublications("ZDB-GENE-001103-4 ", "ZDB-TERM-100331-8");
        assertNotNull(pubs);
    }

    @Test
    public void getOrthologyList() {
        List<Ortholog> list = publicationRepository.getOrthologListByPub("ZDB-PUB-060313-16");
        assertNotNull(list);
        assertTrue(list.size() > 5);
    }


    @Test
    public void getPubMedPublications() {
        List<Publication> pubs = publicationRepository.getPublicationWithPubMedId(200);
        assertNotNull(pubs);
        assertThat(pubs, hasSize(200));
    }

    @Test
    public void getWildtypeFish() {
        List<Fish> wildtypeFish = publicationRepository.getWildtypeFish();
        assertNotNull(wildtypeFish);
        assertThat(wildtypeFish.size(), greaterThan(15));
    }

    @Test
    public void getFullTrackingHistoryForPub() {
        String pubId = "ZDB-PUB-160506-2"; // a pub that has been indexed, curated, and closed
        Publication pub = publicationRepository.getPublication(pubId);
        assertThat("Was expecting to find pub with ID " + pubId + ", but didn't", pub, is(notNullValue()));

        List<PublicationTrackingHistory> historyList = publicationRepository.fullTrackingHistory(pub);
        assertThat(pubId + " should have at least two history entries", historyList, hasSize(greaterThanOrEqualTo(2)));
    }

    @Test
    public void getCurrentTrackingStatusForPub() {
        String pubId = "ZDB-PUB-160528-7"; // a pub that has been indexed, curated, and closed
        Publication pub = publicationRepository.getPublication(pubId);
        assertThat("Was expecting to find pub with ID " + pubId + ", but didn't", pub, is(notNullValue()));

        PublicationTrackingHistory currentStatus = publicationRepository.currentTrackingStatus(pub);
        assertThat(currentStatus, is(notNullValue()));

        assertThat(currentStatus.getStatus(), is(notNullValue()));
        assertThat(currentStatus.getStatus().getType(), is(PublicationTrackingStatus.Type.CLOSED));

        assertThat(currentStatus.getDate(), is(notNullValue()));
        assertThat(currentStatus.getDate().get(Calendar.YEAR), is(2016));
        assertThat(currentStatus.getDate().get(Calendar.MONTH), is(Calendar.AUGUST));
        assertThat(currentStatus.getDate().get(Calendar.DATE), is(9));

        assertThat(currentStatus.getLocation(), is(nullValue()));
        assertThat(currentStatus.getOwner(), is(nullValue()));
        assertThat(currentStatus.getUpdater(), is(notNullValue()));
        assertThat(currentStatus.isCurrent(), is(true));
        assertThat(currentStatus.getPublication(), is(pub));
    }

    @Test
    public void getPubStatusClosedCurated() {
        String pubId = "ZDB-PUB-040708-1"; // a pub that has been indexed, curated, and closed
        Publication pub = publicationRepository.getPublication(pubId);

        PublicationTrackingHistory currentStatus = publicationRepository.currentTrackingStatus(pub);
        assertThat(currentStatus.getStatus().getType(), is(PublicationTrackingStatus.Type.CLOSED));
        assertThat(currentStatus.getStatus().getQualifier(), is("curated"));

    }

    @Test
    public void getPublicationTrackingStatusShouldReturnObjectForValidId() {
        PublicationTrackingStatus status = publicationRepository.getPublicationTrackingStatus(1);
        assertThat(status, is(notNullValue()));
    }

    @Test
    public void getPublicationTrackingStatusShouldReturnNullForInvalidId() {
        PublicationTrackingStatus status = publicationRepository.getPublicationTrackingStatus(0);
        assertThat(status, is(nullValue()));
    }

    @Test
    public void getPublicationStatusByNameShouldReturnObjectForValidName() {
        PublicationTrackingStatus.Name name = PublicationTrackingStatus.Name.WAITING_FOR_NOMENCLATURE;
        PublicationTrackingStatus status = publicationRepository.getPublicationStatusByName(name);
        assertThat(status, is(notNullValue()));
        assertThat(status.getName(), is(name));
    }

    @Test
    public void getAllPublicationLocationsShouldReturnCorrectNumberOfValues() {
        List<PublicationTrackingLocation> locations = publicationRepository.getAllPublicationLocations();
        assertThat(locations, hasSize(PublicationTrackingLocation.Name.values().length));
    }

    @Test
    public void getPublicationTrackingLocationShouldReturnObjectForValidId() {
        PublicationTrackingLocation location = publicationRepository.getPublicationTrackingLocation(1);
        assertThat(location, is(notNullValue()));
    }

    @Test
    public void getPublicationTrackingLocationShouldReturnNullForInvalidId() {
        PublicationTrackingLocation location = publicationRepository.getPublicationTrackingLocation(0);
        assertThat(location, is(nullValue()));
    }

    @Test
    public void getPublicationsByStatusShouldOnlyReturnCurrentStatuses() {
        List<DashboardPublicationBean> statuses = publicationRepository
                .getPublicationsByStatus(null, null, null, 20, 0, null)
                .getPublications();
        for (DashboardPublicationBean status : statuses) {
            assertThat(status.getStatus().isCurrent(), is(true));
        }
    }

    @Test
    public void getPublicationsByStatusShouldReturnObjectsWithSpecifiedStatus() {
        long statusId = 1;
        List<DashboardPublicationBean> statuses = publicationRepository
                .getPublicationsByStatus(statusId, null, null, 20, 0, null)
                .getPublications();
        for (DashboardPublicationBean status : statuses) {
            assertThat(status.getStatus().getStatus().getId(), is(statusId));
        }
    }

    @Test
    public void getPublicationsByStatusShouldReturnObjectsWithSpecifiedLocation() {
        long locationId = 1;
        List<DashboardPublicationBean> statuses = publicationRepository
                .getPublicationsByStatus(null, locationId, null, 20, 0, null)
                .getPublications();
        for (DashboardPublicationBean status : statuses) {
            assertThat(status.getStatus().getLocation().getId(), is(locationId));
        }
    }

    @Test
    public void getPublicationsByStatusShouldInterpretZeroAsNullForLocation() {
        long locationId = 0;
        List<DashboardPublicationBean> statuses = publicationRepository
                .getPublicationsByStatus(null, locationId, null, 20, 0, "location")
                .getPublications();
        for (DashboardPublicationBean status : statuses) {
            assertThat(status.getStatus().getLocation(), is(nullValue()));
        }
    }

    @Test
    public void getPublicationsByStatusShouldReturnObjectsWithSpecifiedOwner() {
        // This is tougher than status or location since owner will be in flux a lot.
        // The test uses Holly's id. Hopefully she continues to exist and has something
        // assigned to her otherwise this test isn't doing anything.
        String ownerId = "ZDB-PERS-100329-1";
        List<DashboardPublicationBean> statuses = publicationRepository
                .getPublicationsByStatus(null, null, ownerId, 20, 0, null)
                .getPublications();
        for (DashboardPublicationBean status : statuses) {
            assertThat(status.getStatus().getOwner().getZdbID(), is(ownerId));
        }
    }

    @Test
    public void getPublicationsByStatusShouldInterpretStarAsAnyOwner() {
        String ownerId = "*";
        List<DashboardPublicationBean> statuses = publicationRepository
                .getPublicationsByStatus(null, null, ownerId, 50, 0, "-owner")
                .getPublications();
        for (DashboardPublicationBean status : statuses) {
            assertThat(status.getStatus().getOwner(), is(notNullValue()));
        }
    }

    @Test
    public void getPublicationsByStatusShouldReturnSpecifiedNumberOfObjectsAndPopulateTotalCount() {
        int count = 33;
        DashboardPublicationList statuses = publicationRepository
                .getPublicationsByStatus(null, null, null, count, 0, null);
        assertThat(statuses.getPublications(), hasSize(count));
        assertThat(statuses.getTotalCount(), is(greaterThanOrEqualTo(count)));
    }

    @Test
    public void getPublicationsByStatusShouldPaginateCorrectly() {
        int count = 27;
        DashboardPublicationList firstPage = publicationRepository.getPublicationsByStatus(null, null, null, count, 0, null);
        DashboardPublicationList secondPage = publicationRepository.getPublicationsByStatus(null, null, null, count, count, null);
        assertThat(firstPage.getPublications(), is(not(empty())));
        assertThat(secondPage.getPublications(), is(not(empty())));
        assertThat(secondPage.getPublications().get(0), not(equalTo(firstPage.getPublications().get(0))));
        assertThat(secondPage.getPublications().get(0), not(equalTo(firstPage.getPublications().get(firstPage.getPublications().size() - 1))));
    }

    @Test
    public void getSTRsByPublication() {
        List<SequenceTargetingReagent> strList = publicationRepository.getSTRsByPublication("ZDB-PUB-090807-11", new Pagination());
        assertThat(strList, is(not(empty())));

        Pagination pagination = new Pagination();
        pagination.addFieldFilter(FieldFilter.TARGET_NAME, "x1");
        strList = publicationRepository.getSTRsByPublication("ZDB-PUB-090807-11", pagination);
        assertThat(strList, is(not(empty())));
    }

    @Test
    public void getOrthologyByPublication() {
        GeneBean pagination = new GeneBean();
        pagination.setFirstPageRecord(0);
        PaginationResult<Ortholog> strList = publicationRepository.getOrthologPaginationByPub("ZDB-PUB-050823-6", pagination);
        assertNotNull(strList);
    }

    @Test
    public void getOrthologyByPublicationAPI() {
        List<Ortholog> strList = publicationRepository.getOrthologPaginationByPub("ZDB-PUB-050823-6");
        assertNotNull(strList);
    }

    @Test
    public void getImageForPub() {
        List<Image> images = publicationRepository.getImages(publicationRepository.getPublication("ZDB-PUB-170608-5"));
        assertNotNull(images);
    }

    @Test
    public void getNumberOfStatusChanges() {
        Person person = getProfileRepository().getPerson("ZDB-PERS-220425-1");
        PublicationTrackingStatus status = getPublicationRepository().getPublicationTrackingStatus(2);
        long numberOfStatusChanges = getPublicationRepository().getPublicationTrackingStatus(person, 48, status);

        assertTrue(numberOfStatusChanges >= 0);
    }
}

