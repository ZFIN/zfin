package org.zfin.antibody.repository;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Test;
import org.zfin.AbstractDatabaseTest;
import org.zfin.Species;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.AnatomyService;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Assay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.AllMarkerNamesFastSearch;
import org.zfin.infrastructure.RecordAttribution;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.Marker;
import org.zfin.marker.MarkerAlias;
import org.zfin.marker.MarkerHistory;
import org.zfin.marker.MarkerType;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.mutant.presentation.AntibodyStatistics;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.Organization;
import org.zfin.people.SourceUrl;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.repository.RepositoryFactory.*;

@SuppressWarnings({"FeatureEnvy"})
public class AntibodyRepositoryTest extends AbstractDatabaseTest {

    /**
     * Retrieving an antibody by ID'
     */
    @Test
    public void getAntibodyByID() {
        String zdbID = "ZDB-ATB-080421-2";

        Antibody ab = getAntibodyRepository().getAntibodyByID(zdbID);
        assertTrue(ab == null);
    }

    /**
     * Retrieving an antibody by ID and a cellular component'
     */
    @Test
    public void getAntibodyWithCC() {
        String abName = "zm-4";
        Antibody ab = getAntibodyRepository().getAntibodyByName(abName);
        assertTrue(ab != null);

        Set<ExpressionExperiment> labelings = ab.getAntibodyLabelings();
        assertTrue(labelings != null);

        Set<MarkerSupplier> antibodyLabSuppliers = ab.getSuppliers();
        assertTrue(antibodyLabSuppliers != null);

        assertEquals(antibodyLabSuppliers.size(), 1);

        MarkerSupplier antibodyLabSupplier = antibodyLabSuppliers.iterator().next();
        Organization lab = antibodyLabSupplier.getOrganization();
        assertEquals("Zebrafish International Resource Center (ZIRC)", lab.getName());

        Set<SourceUrl> orgUrls = lab.getOrganizationUrls();
        assertTrue(orgUrls != null);

        SourceUrl orgUrl = orgUrls.iterator().next();
        assertEquals("order", orgUrl.getBusinessPurpose());
    }

    /**
     * Retrieving antibodies with '%zn%'
     */
    @Test
    public void getAntibodyByCriteria() {
        //
        String name = "zn";
        AntibodySearchCriteria ab = new AntibodySearchCriteria();
        ab.setName(name);
        ab.setAntibodyNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(ab);
        assertTrue(abs != null);
    }

    /**
     * Retrieving antibodies with 'anti*' includes an alias.
     */
    @Test
    public void getAntibodyByCriteriaAndAlias() {
        // name of abs as well as alias
        String name = "anti";
        AntibodySearchCriteria ab = new AntibodySearchCriteria();
        ab.setName(name);
        ab.setAntibodyNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(ab);
        assertTrue(abs != null);
    }

    /**
     * Retrieving antibodies with stage range only
     */
    @Test
    public void getAntibodyByStartStage() {

        // Cleavage:4-cell
        String stageZdbID = "ZDB-STAGE-010723-11";
        // Adult
        String stageZdbIDEnd = "ZDB-STAGE-010723-39";
        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        DevelopmentStage startStage = new DevelopmentStage();
        startStage.setZdbID(stageZdbID);
        DevelopmentStage endStage = new DevelopmentStage();
        endStage.setZdbID(stageZdbIDEnd);
        searchCriteria.setStartStage(startStage);
        searchCriteria.setEndStage(endStage);
/*
        searchCriteria.setName("anti");
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);
*/

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by a single ao term only

    @Test
    public void getAntibodyByAnatomyTerm() {

        // cranial nerve
        String aoTermCN = "ZDB-ANAT-011113-81";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(aoTermCN);
        searchCriteria.setIncludeSubstructures(false);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    @Test
    public void getAntibodyByAnatomyTermIncludingSubstructures() {

        // brain
        String termID = "ZDB-ANAT-010921-415";
        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(termID);
        searchCriteria.setIncludeSubstructures(true);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by two ao terms ANDed

    @Test
    public void getAntibodyByTwoAnatomyTermAndConnected() {

        // brain,rhombomere
        String aoTermIds = "ZDB-ANAT-010921-415,ZDB-ANAT-020702-3";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(aoTermIds);
        searchCriteria.setIncludeSubstructures(false);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by two ao terms ANDed

    @Test
    public void getAntibodyByTwoAnatomyTermAndConnectedIncludingSubstructures() {

        // brain,rhombomere
        String aoTermIds = "ZDB-ANAT-010921-415,ZDB-ANAT-020702-3";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(aoTermIds);
        searchCriteria.setIncludeSubstructures(true);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by two ao terms ORed

    @Test
    public void getAntibodyByTwoAnatomyTermsOr() {

        // brain,rhombomere
        String aoTermIds = "ZDB-ANAT-010921-415,ZDB-ANAT-020702-3";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermIDs(aoTermIds);
        searchCriteria.setIncludeSubstructures(false);
        searchCriteria.setAnatomyEveryTerm(false);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by immunogen species only

    @Test
    public void getAntibodyByImmunogenSpecies() {

        String immunogenSpecies = "Zebrafish";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setImmunogenSpecies(immunogenSpecies);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by host species only

    @Test
    public void getAntibodyByHostSpecies() {

        String hostSpecies = "Mouse";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setHostSpecies(hostSpecies);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by clonal type only

    @Test
    public void getAntibodyByClonalType() {

        AntibodyType type = AntibodyType.MONOCLONAL;

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setClonalType(type.getValue());

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by Zirc-only

    @Test
    public void getAntibodyByZircOnlyFilter() {

        boolean zircOnly = true;

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setZircOnly(zircOnly);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

        zircOnly = false;
        searchCriteria.setZircOnly(zircOnly);
        abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);
    }

    // Test search by:
    //  antigen name

    @Test
    public void getAntibodyByAntigenName() {

        String antigenName = "alcama";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAntigenGeneName(antigenName);
        searchCriteria.setAntigenNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);
        boolean foundGene = false;
        Antibody foundAB = null;
        for (Antibody ab : abs) {

            List<Marker> markers = ab.getAllRelatedMarker();
            for (Marker marker : markers) {
                if (marker.getAbbreviation().indexOf(antigenName) > -1) {
                    foundAB = ab;
                    foundGene = true;
                }
                if (marker.getName().indexOf(antigenName) > -1) {
                    foundAB = ab;
                    foundGene = true;
                }
                Set<MarkerAlias> aliases = marker.getAliases();
                if (aliases == null)
                    continue;
                for (MarkerAlias alias : aliases) {
                    if (alias.getAlias().indexOf(antigenName) > -1) {
                        foundAB = ab;
                        foundGene = true;
                    }
                }
            }
        }
        // found antibody
        assertNotNull(foundAB);
        assertTrue(foundGene);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by:
    //  antigen name

    @Test
    public void getAntibodyByAntibodyPreviousName() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        String antibodyAlias = "veg";
        searchCriteria.setName(antibodyAlias);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);
        assertTrue(abs.size() > 0);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    @Test
    public void searchAntibodyByAntibodyName() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        String abName = "x";
        searchCriteria.setName(abName);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);
        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by:
    //  antigen name

    @Test
    public void getAntibodyByAntigenGeneName() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        // this is the name and not the abbreviation
        String antigenName = "adhesion";
        searchCriteria.setAntigenGeneName(antigenName);
        searchCriteria.setAntigenNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);
        assertEquals(true, abs.size() > 0);

        boolean foundGene = false;
        Antibody foundAB = null;
        for (Antibody ab : abs) {

            List<Marker> markers = ab.getAllRelatedMarker();
            for (Marker marker : markers) {
                if (marker.getAbbreviation().indexOf(antigenName) > -1) {
                    foundAB = ab;
                    foundGene = true;
                }
                if (marker.getName().indexOf(antigenName) > -1) {
                    foundAB = ab;
                    foundGene = true;
                }
                Set<MarkerAlias> aliases = marker.getAliases();
                if (aliases == null)
                    continue;
                for (MarkerAlias alias : aliases) {
                    if (alias.getAlias().indexOf(antigenName) > -1) {
                        foundAB = ab;
                        foundGene = true;
                    }
                }
            }
        }
        // found antibody
        assertTrue(foundGene);
        assertNotNull(foundAB);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test for assay

    @Test
    public void getAntibodyByAssay() {
        String assayName = Assay.IMMUNOHISTOCHEMISTRY.getName();

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAssay(assayName);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);
        assertEquals(true, abs.size() > 0);
    }

    // Test search by: host and immunogen species

    @Test
    public void getAntibodyByHostAndImmunogen() {

        String immunogenSpecies = "Zebrafish";
        String hostSpecies = "Mouse";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setHostSpecies(hostSpecies);
        searchCriteria.setImmunogenSpecies(immunogenSpecies);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by
    // single ao term
    // ab name

    @Test
    public void getAntibodyByAnatomyTermAndABName() {

        //cranial nerve V
        String termID = "ZDB-ANAT-011113-81";
        String name = "z";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setName(name);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);
        searchCriteria.setAnatomyTermIDs(termID);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }


    // Test search by:
    //
    // host species
    // immunogen species
    // ZircOnly 

    @Test
    public void getAntibodyByHostAndImmunogenAndZircOnly() {

        String immunogenSpecies = "Zebrafish";
        String hostSpecies = "Mouse";
        boolean zircOnly = true;

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setHostSpecies(hostSpecies);
        searchCriteria.setImmunogenSpecies(immunogenSpecies);
        searchCriteria.setZircOnly(zircOnly);

        List<Antibody> abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

        searchCriteria.setZircOnly(false);

        abs = getAntibodyRepository().getAntibodies(searchCriteria);
        assertTrue(abs != null);

        numberOfAb = getAntibodyRepository().getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }


    @Test
    public void getHostSpeciesList() {

        List<Species> species = getAntibodyRepository().getHostSpeciesList();
        assertTrue(species != null);

    }

    @Test
    public void getImmunogenSpeciesList() {

        List<Species> species = getAntibodyRepository().getImmunogenSpeciesList();
        assertNotNull(species);

    }

    @Test
    public void getAssociatedGenes() {
        String abName = "zn-5";

        Antibody ab = getAntibodyRepository().getAntibodyByName(abName);
        assertNotNull(ab);

        List<Marker> markers = ab.getAllRelatedMarker();
        assertNotNull(markers);
        assertTrue(markers.size() > 0);

    }

    @Test
    public void getAllAntibodiesByAOTerm() {
        String aoTermName = "forerunner cell group";
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-22");
        term.setTermName(aoTermName);

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setTerm(term);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        int count = getAntibodyRepository().getAntibodiesByAOTermCount(term);
        assertTrue(count > 0);

        PaginationResult<Antibody> abs = getAntibodyRepository().getAntibodiesByAOTerm(term, pagination, false);
        assertTrue(abs != null);
        assertTrue(abs.getTotalCount() == count);
    }

    @Test
    public void getAllAntibodiesByAOTermSecondaryMotorNeuron() {
        //  secondary motor neuron
        String aoTermZdbID = "ZDB-TERM-100331-2304";
        GenericTerm term = new GenericTerm();
        term.setZdbID(aoTermZdbID);

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setTerm(term);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        int count = getAntibodyRepository().getAntibodiesByAOTermCount(term);
        assertTrue(count > -1);

        PaginationResult<Antibody> abs = getAntibodyRepository().getAntibodiesByAOTerm(term, pagination, false);
    }

    @Test
    public void getFigureCount() {
        // zn-5
        //String abName = "Ab1-elavl";
        String abZdbID = "ZDB-ATB-081003-2";
        Antibody antibody = getAntibodyRepository().getAntibodyByID(abZdbID);
        // spinal cord
        GenericTerm aoTerm = getOntologyRepository().getTermByName("spinal cord", Ontology.ANATOMY);

        int numOfFigures = getAntibodyRepository().getNumberOfFiguresPerAoTerm(antibody, aoTerm, Figure.Type.FIGURE);
        assertTrue(numOfFigures > 0);

        List<Figure> figures = getAntibodyRepository().getFiguresPerAoTerm(antibody, aoTerm);
        assertTrue(figures != null);
        assertEquals(true, figures.size() > 0);
    }

    @Test
    public void getPublicationsPerAntibodyAndAOTerm() {
        String abName = "Ab2-dag1";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(abName);
        assertNotNull(antibody);
        // brain
//        String aoID = "ZDB-ANAT-010921-415"; // this is a text-only figure for the anatomy item

        // cerebellum
        String aoID = "ZDB-ANAT-010921-522";
        GenericTerm aoTerm = getOntologyRepository().getTermByName("cerebellum", Ontology.ANATOMY);

        PaginationResult<Publication> pubs = getAntibodyRepository().getPublicationsWithFigures(antibody, aoTerm);
        assertNotNull(pubs);
        assertTrue(pubs.getPopulatedResults().size() > 0);
    }

    @Test
    public void getAntibodiesPerPub() {
        // Trevarrow, Marks and Kimmel
        String pubZdbID = "ZDB-PUB-961014-1139";
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication pub = pr.getPublication(pubZdbID);

        List<Antibody> abs = getAntibodyRepository().getAntibodiesByPublication(pub);
        assertTrue(abs != null);
    }

    @Test
    public void getSuppliers() {
        String abName = "anti-Tbx16";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(abName);

        Set<MarkerSupplier> suppliers = antibody.getSuppliers();
        assertTrue(suppliers != null);

    }

    @Test
    public void externalNoteRecordAttributionList() {

        String externalNoteZdbID = "ZDB-EXTNOTE-080424-1";
        ActiveData data = new ActiveData();
        data.setZdbID(externalNoteZdbID);

        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();
        List<RecordAttribution> rec = ir.getRecordAttributions(data);
        assertTrue(rec != null);
    }

    @Test
    public void antibodyExternalNote() {

        String antibodyName = "anti-Tbx16";
        Antibody ab = getAntibodyRepository().getAntibodyByName(antibodyName);

        Set<AntibodyExternalNote> notes = ab.getExternalNotes();
        assertTrue(notes != null);

    }

    @Test
    public void labSupplier() {
        String antibodyName = "anti-Tbx16";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(antibodyName);

        String name = "Zebrafish International Resource Center (ZIRC)";
        ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
        Organization org = profileRepository.getOrganizationByName(name);
        MarkerSupplier sup = profileRepository.getSpecificSupplier(antibody, org);
        assertTrue(sup != null);
        assertTrue(sup.getOrganization() != null);
    }

    @Test
    public void labSupplierSecond() {
        String antibodyName = "anti-DLX3b";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(antibodyName);
        Set<MarkerSupplier> suppliers = antibody.getSuppliers();
        if (suppliers != null) {
            for (MarkerSupplier supplier : suppliers) {
                Organization org = supplier.getOrganization();
                SourceUrl url = org.getOrganizationOrderURL();
                String urlString = supplier.getOrderURL();

                assertTrue(org != null);
            }
        }

        String name = "Zebrafish International Resource Center (ZIRC)";
        ProfileRepository profileRepository = RepositoryFactory.getProfileRepository();
        Organization org = profileRepository.getOrganizationByName(name);
        MarkerSupplier sup = profileRepository.getSpecificSupplier(antibody, org);
        assertTrue(sup.getOrganization() != null);
    }

    //@Test

    public void createAntibody() {
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            MarkerType mt = mr.getMarkerTypeByName(Marker.Type.ATB.name());
            Marker antibody = new Antibody();
            antibody.setMarkerType(mt);
            antibody.setName("werner");
            antibody.setAbbreviation("werner");
            antibody.setOwner(TestConfiguration.getPerson());
            session.save(antibody);
            session.flush();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                tx.rollback();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    //@Test

    public void updateAntibody() {

        // anti-DLX3b
        String abName = "anti-DLX3b";
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
            Antibody antibody = antibodyRepository.getAntibodyByName(abName);
            antibody.setName("new name");
            antibody.setAbbreviation("new name");
            session.update(antibody);
            session.flush();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                tx.rollback();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    //@Test

    public void updateAntibodyViaRenameMarkerMethod() {

        // anti-DLX3b
        String abName = "anti-DLX3b";
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            AntibodyRepository antibodyRepository = RepositoryFactory.getAntibodyRepository();
            Antibody antibody = antibodyRepository.getAntibodyByName(abName);
            antibody.setAbbreviation("new name");


            PublicationRepository pr = RepositoryFactory.getPublicationRepository();
            Publication pub = pr.getPublication("ZDB-PUB-000104-1");
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker marker = mr.getMarkerByID("ZDB-GENE-000112-30");
            marker.setAbbreviation("newman");
            mr.renameMarker(marker, pub, MarkerHistory.Reason.NOT_SPECIFIED);
            session.flush();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                tx.rollback();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getAllMapNameForAntibodies() {
        String string = "pdx";
        List<AllMarkerNamesFastSearch> all = getAntibodyRepository().getAllNameAntibodyMatches(string);
        assertTrue(all != null);
    }

    @Test
    public void getUsedAntibodies() {
        List<Species> all = getAntibodyRepository().getUsedHostSpeciesList();
        assertTrue(all != null);
    }

    @Test
    public void getPublicationsPerAntibodyAndAoTerm() {
        String antibodyName = "zn-5";
        String aoTermName = "spinal cord";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(antibodyName);
        AnatomyItem aoTerm = getAnatomyRepository().getAnatomyItem(aoTermName);

        PaginationResult<Publication> pubs = getAntibodyRepository().getPublicationsWithFigures(antibody, aoTerm.createGenericTerm());
        assertTrue(pubs != null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getAntibodiesForAoTerm() {
        String aoTermName = "pancreas";
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-130");
        term.setTermName(aoTermName);

        Session session = HibernateUtil.currentSession();
        String hql = "select distinct stat.antibody " +
                "     from AntibodyAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           and stat.subterm = :aoterm";
        Query query = session.createQuery(hql);
        query.setParameter("aoterm", term);

        List<AntibodyAOStatistics> list = query.list();
        assertNotNull(list);
        assertTrue(list.size() > 0);

        hql = " " +
                "     from AntibodyAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           and stat.subterm = :aoterm";
        query = session.createQuery(hql);
        query.setParameter("aoterm", term);
        List<AntibodyAOStatistics> listStat = query.list();
        assertNotNull(listStat);
        assertTrue(list.size() > 0);

    }

    /**
     * Check that antibody lookup by name is case insensitive.
     */
    @Test
    public void getAntibodyByName() {
        // real name is Ab-F59
        String antibodyName = "AB-F59";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(antibodyName);
        assertTrue(antibody != null);
    }

    @Test
    public void getAntibodyStatistics() {
        String aoTermName = "eye";
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-100");
        term.setTermName(aoTermName);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        // without substructures
        PaginationResult<AntibodyStatistics> result = AnatomyService.getAntibodyStatistics(term, pagination, false);
        assertTrue(result != null);
        assertTrue(result.getTotalCount() > 0);
        assertTrue(result.getPopulatedResults().size() > 1);
        // including substructures
        result = AnatomyService.getAntibodyStatistics(term, pagination, true);
        assertTrue(result != null);
        assertTrue(result.getTotalCount() > 0);

    }

    @Test
    public void antibodySummaryPage() {
        String aoTermName = "muscle pioneer";
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-1053");
        term.setTermName(aoTermName);

        // real name is Ab-Eng
        String antibodyName = "Ab-eng";
        Antibody antibody = getAntibodyRepository().getAntibodyByName(antibodyName);
        assertTrue(antibody != null);

        List<Figure> figures = getAntibodyRepository().getFiguresForAntibodyWithTermsAtStage(antibody, term, null, null, null, false);
        assertNotNull(figures);
        assertTrue(figures.size() > 5);

        figures = getAntibodyRepository().getFiguresForAntibodyWithTerms(antibody, term, false);
        assertNotNull(figures);
        assertTrue(figures.size() > 7);

    }

    @Test
    public void antibodyAoStatistics() {
        // check if there are any antibodies for a given AO
        // brain
        String termID = "ZDB-TERM-100331-8";
        GenericTerm term = getOntologyRepository().getTermByZdbID(termID);
        int numberOfAntibodiesPerAOTerm = getAntibodyRepository().getAntibodyCount(term, false);
        assertTrue(numberOfAntibodiesPerAOTerm > 50);
        int numberOfAntibodiesIncludingSubstructures = getAntibodyRepository().getAntibodyCount(term, true);
        assertTrue(numberOfAntibodiesIncludingSubstructures >= numberOfAntibodiesPerAOTerm);
    }
}
