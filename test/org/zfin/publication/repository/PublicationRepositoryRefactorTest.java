package org.zfin.publication.repository;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.zfin.AbstractDatabaseTest;
import org.zfin.AppConfig;
import org.zfin.expression.Figure;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.publication.Journal;
import org.zfin.publication.Publication;

import java.lang.reflect.Method;
import java.util.List;
import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;
import static org.zfin.repository.RepositoryFactory.getOntologyRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class})
@WebAppConfiguration
public class PublicationRepositoryRefactorTest extends AbstractDatabaseTest {

    @Autowired
    private PublicationRepository publicationRepository;

    @Test
    public void getSNPPublicationIDs() {
        Marker marker = getMarkerRepository().getMarker("ZDB-BAC-050218-656");
        List<String> pubs = publicationRepository.getSNPPublicationIDs(marker);
        assertNotNull(pubs);
        assertEquals(1, pubs.size());
        assertEquals("ZDB-PUB-070427-10", pubs.get(0));

    }

    @Test
    public void getPublications() {
        List<String> testPubs = List.of("ZDB-PUB-180130-17", "ZDB-PUB-190613-8");
        List<Publication> pubs = publicationRepository.getPublications(testPubs);
        assertEquals(2, pubs.size());

        testPubs.containsAll(List.of(pubs.get(0).getZdbID(),pubs.get(1).getZdbID()));

        pubs = publicationRepository.getPublications(null);
        assertEquals(0, pubs.size());
    }

    @Test
    public void publicationExists() {
        boolean exists = publicationRepository.publicationExists("ZDB-PUB-180130-17");
        assertTrue(exists);

        boolean notExists = publicationRepository.publicationExists("BOGUS-IDENTIFIER");
        assertFalse(notExists);
    }

    @Test
    public void getPubsForDisplay() {
        assertTrue(publicationRepository.getPubsForDisplay("ZDB-GENE-040426-1855").size() > 10);
        assertTrue(publicationRepository.getPubsForDisplay("ZDB-GENE-051005-1").size() > 15);
        assertEquals(0, publicationRepository.getPubsForDisplay("ZDB-SSLP-000315-3").size());
    }

    @Test
    public void getAllJournals() {
        List<Journal> journals = publicationRepository.getAllJournals();
        assertTrue( journals.size() > 3000);
    }

    @Test
    public void getJournalByAbbreviation() {
        Journal journal = publicationRepository.getJournalByAbbreviation("Food Chem X");
        assertEquals( "Food chemistry: X", journal.getName());
    }

    @Test
    public void getJournalByPrintIssn() {
        Journal journal = publicationRepository.getJournalByPrintIssn("2590-1575");
        assertEquals( "Food chemistry: X", journal.getName());
    }

    @Test
    public void getJournalByEIssn() {
        Journal journal = publicationRepository.getJournalByEIssn("2041-1723");
        assertEquals( "Nature communications", journal.getName());
    }

    @Test
    public void getNumberAssociatedPublicationsForMarker() {

        Marker m;
        int numberPubs;

        m = getMarkerRepository().getMarkerByID("ZDB-GENE-051005-1");
        numberPubs = publicationRepository.getNumberAssociatedPublicationsForZdbID(m.getZdbID());
        assertTrue(numberPubs > 15);
        assertTrue(numberPubs < 35);
//        assertEquals(28, numberPubs);

        m = getMarkerRepository().getMarkerByAbbreviation("pax6a");
        numberPubs = publicationRepository.getNumberAssociatedPublicationsForZdbID(m.getZdbID());
        assertTrue(numberPubs > 190);
        assertTrue(numberPubs < 500);
//        assertEquals(334, numberPubs);

    }

    @Test
    public void getNumberOfPublicationForPax2aAndMHB() {
        String termName = "midbrain hindbrain boundary";
        OntologyRepository aoRepository = getOntologyRepository();
        GenericTerm item = aoRepository.getTermByName(termName, Ontology.ANATOMY);
        Marker pax2a = getMarkerRepository().getMarkerByAbbreviation("pax2a");

        PaginationResult<Publication> qualityPubs = publicationRepository.getPublicationsWithFigures(pax2a, item);
        assertTrue(qualityPubs != null);
        assertEquals("122 pubs", 122, qualityPubs.getPopulatedResults().size());

    }

    @Test
    public void getPublicationsWithFigures() {
        Marker marker = getMarkerRepository().getMarker("ZDB-GENE-990415-8");
        OntologyRepository aoRepository = getOntologyRepository();
        GenericTerm item = aoRepository.getTermByZdbID("ZDB-TERM-100331-40");

        PaginationResult<Publication> pubs = publicationRepository.getPublicationsWithFigures(marker, item);
        assertEquals("122 pubs", 122, pubs.getPopulatedResults().size());

//        pubs = publicationRepository.getPublicationsWithFigures_New(marker, item);
//        assertEquals("122 pubs", 122, pubs.getPopulatedResults().size());
    }

    @Test
    public void getPublicationsWithFigures2() {
        //tbxta
        Marker marker = getMarkerRepository().getMarker("ZDB-GENE-980526-437");
        OntologyRepository aoRepository = getOntologyRepository();

        //dorsal region
        GenericTerm item = aoRepository.getTermByZdbID("ZDB-TERM-100722-81");

        PaginationResult<Publication> pubs = publicationRepository.getPublicationsWithFigures(marker, item);
        assertEquals("5 pubs", 5, pubs.getPopulatedResults().size());

//        pubs = publicationRepository.getPublicationsWithFigures_New(marker, item);
//        assertEquals("5 pubs", 5, pubs.getPopulatedResults().size());

    }


    @Test
    public void getFigureLabels() {
        String zdbID = "ZDB-PUB-990507-16";

        List<String> experiments = publicationRepository.getDistinctFigureLabels(zdbID);
        assertTrue(experiments != null);
        assertTrue(experiments.size() > 0);
        assertTrue(experiments.size() == 6);

//        experiments = publicationRepository.getDistinctFigureLabels_New(zdbID);
//        assertTrue(experiments != null);
//        assertTrue(experiments.size() > 0);
//        assertTrue(experiments.size() == 6);
    }

    @Test
    public void getGenesAndMarkersByPublication() {
        String zdbID = "ZDB-PUB-990507-16";

        List<Marker> markers = publicationRepository.getGenesAndMarkersByPublication(zdbID);
        assertTrue(markers != null);
        assertTrue(markers.size() > 0);
        assertTrue(markers.size() == 2);

        String zdbID2 = "ZDB-PUB-150809-4";

//        List<Marker> markers2 = publicationRepository.getMarkersPulledThroughSTRs(zdbID2);
        List<Marker> markers2 = getMarkersPulledThroughSTRs(zdbID2);

        markers2 = publicationRepository.getGenesAndMarkersByPublication(zdbID2);
        assertTrue(markers2 != null);
        assertTrue(markers2.size() > 0);
        assertTrue(markers2.size() == 29);

//        List<Marker> markers3 = publicationRepository.getMarkersPulledThroughSTRs_New(zdbID2);
//        assertEquals(markers3.size(), markers2.size());
    }

    @Test
    public void getMarkersByTypeForPublication() {
        String publicationID = "ZDB-PUB-220412-11";
        MarkerType efgType = getMarkerRepository().getMarkerTypeByName(Marker.Type.EFG.name());
        List<Marker> markers = publicationRepository.getMarkersByTypeForPublication(publicationID, efgType);

        assertEquals(3, markers.size());

    }

    @Test
    public void getSTRsByPublication() {
        MarkerType morpholinoMarkerType = getMarkerRepository().getMarkerTypeByName("MRPHLNO");
        MarkerType crisprMarkerType = getMarkerRepository().getMarkerTypeByName("CRISPR");
        String publicationID = "ZDB-PUB-190215-8";
        List<SequenceTargetingReagent> morpholinos = publicationRepository.getSTRsByPublication(publicationID, morpholinoMarkerType);
        List<SequenceTargetingReagent> crisprs = publicationRepository.getSTRsByPublication(publicationID, crisprMarkerType);

        assertEquals(2, morpholinos.size());
        assertEquals(3, crisprs.size());
    }

    @Test
    public void getClonesByPublication() {
        PaginationBean paginationBean = new PaginationBean();
        PaginationResult<Clone> clones = publicationRepository.getClonesByPublication("ZDB-PUB-080422-3", paginationBean);
        assertNotNull(clones);
        assertEquals(1, clones.getTotalCount());
    }

    @Test
    public void getMarkersByPublication() throws Exception {
        Marker.TypeGroup typeGroup = Marker.TypeGroup.GENEDOM;
        List<MarkerType> markerTypes = getMarkerRepository().getMarkerTypesByGroup(typeGroup);
        markerTypes.add(getMarkerRepository().getMarkerTypeByName(Marker.Type.EFG.toString()));

//        test private method, similar to the following if it were a public method:
//        List<Marker> markers1 = publicationRepository.getMarkersByPublication("ZDB-PUB-080422-3", markerTypes);
//        List<Marker> markers2 = publicationRepository.getMarkersByPublication("ZDB-PUB-190215-8", markerTypes);
        List<Marker> markers1 = getMarkersByPublication("ZDB-PUB-080422-3", markerTypes);
        List<Marker> markers2 = getMarkersByPublication("ZDB-PUB-190215-8", markerTypes);

        assertTrue(markers1.size() > 5);
        assertTrue(markers2.size() > 5);
    }

    // similar to the following method call, but with a workaround to access private method:
    //   List<Marker> markers = publicationRepository.getMarkersPulledThroughSTRs(zdbID2);
    @SneakyThrows
    private List<Marker> getMarkersPulledThroughSTRs(String zdbID) {
        Method getMarkersPulledThroughSTRs = HibernatePublicationRepository.class.getDeclaredMethod(
                "getMarkersPulledThroughSTRs", String.class);

        getMarkersPulledThroughSTRs.setAccessible(true);
        return (List<Marker>) getMarkersPulledThroughSTRs.invoke(publicationRepository, zdbID);
    }

    // similar to the following method call, but with a workaround to access private method:
    //   List<Marker> markers = publicationRepository.getMarkersByPublication(zdbID);
    @SneakyThrows
    private List<Marker> getMarkersByPublication(String zdbID, List<MarkerType> markerTypes) {
        Method getMarkersByPublication = HibernatePublicationRepository.class.getDeclaredMethod(
                "getMarkersByPublication", String.class, List.class);
        getMarkersByPublication.setAccessible(true);
        return (List<Marker>) getMarkersByPublication.invoke(publicationRepository, zdbID, markerTypes);
    }

}
