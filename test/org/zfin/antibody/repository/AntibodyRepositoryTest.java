package org.zfin.antibody.repository;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.HibernateException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.zfin.ExternalNote;
import org.zfin.Species;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.AntibodyExternalNote;
import org.zfin.antibody.AntibodyType;
import org.zfin.antibody.presentation.AntibodySearchCriteria;
import org.zfin.expression.Assay;
import org.zfin.expression.ExpressionExperiment;
import org.zfin.expression.Figure;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.*;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.*;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.people.*;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.FilterType;

import java.util.List;
import java.util.Set;

public class AntibodyRepositoryTest {

    private static AntibodyRepository antibodyRep = RepositoryFactory.getAntibodyRepository();
    AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
    private InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

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

    /**
     * Retrieving an antibody by ID'
     */
    //ToDo: wait until Abs are in production and then hard-code a correct AB
    // @Test
    public void getAntibodyByID() {
        String zdbID = "ZDB-ATB-080421-2";

        Antibody ab = antibodyRep.getAntibodyByID(zdbID);
        assertTrue(ab == null);
    }

    /**
     * Retrieving an antibody by ID and a cellular component'
     */
    @Test
    public void getAntibodWithCC() {
        String abName = "zm-4";
        Antibody ab = antibodyRep.getAntibodyByName(abName);
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

        List<Antibody> abs = antibodyRep.getAntibodies(ab);
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

        List<Antibody> abs = antibodyRep.getAntibodies(ab);
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

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by a single ao term only
    @Test
    public void getAntibodyByAnatomyTerm() {

        // cranial nerve
        String aoTermCN = "ZDB-ANAT-011113-81";
        // cranial nerve V
        String aotermCNV = "cranial nerve V";


        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
/*
        searchCriteria.setName("");
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);
*/
        searchCriteria.setAnatomyTermsString(aotermCNV);
        searchCriteria.setIncludeSubstructures(false);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    @Test
    public void getAntibodyByAnatomyTermIncludingSubstructures() {

        // zdbID "ZDB-ANAT-010921-415";
        // brain
        String aotermCNV = "brain";


        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermsString(aotermCNV);
        searchCriteria.setIncludeSubstructures(true);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by two ao terms ANDed
    @Test
    public void getAntibodyByTwoAnatomyTermAndConnected() {

        String aoTerm = "brain,rhombomere";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermsString(aoTerm);
        searchCriteria.setIncludeSubstructures(false);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by two ao terms ANDed
    @Test
    public void getAntibodyByTwoAnatomyTermAndConnectedIncludingSubstructures() {

        String aoTerm = "brain,rhombomere";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermsString(aoTerm);
        searchCriteria.setIncludeSubstructures(true);
        searchCriteria.setAnatomyEveryTerm(true);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by two ao terms ORed
    @Test
    public void getAntibodyByTwoAnatomyTermsOr() {

        String aoTerm = "brain,rhombomere";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTermsString(aoTerm);
        searchCriteria.setIncludeSubstructures(false);
        searchCriteria.setAnatomyEveryTerm(false);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by immunogen species only
    @Test
    public void getAntibodyByImmunogenSpecies() {

        String immunogenSpecies = "Zebrafish";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setImmunogenSpecies(immunogenSpecies);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by host species only
    @Test
    public void getAntibodyByHostSpecies() {

        String hostSpecies = "Mouse";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setHostSpecies(hostSpecies);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by clonal type only
    @Test
    public void getAntibodyByClonalType() {

        AntibodyType type = AntibodyType.MONOCLONAL;

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setClonalType(type.getName());

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by Zirc-only
    @Test
    public void getAntibodyByZircOnlyFilter() {

        boolean zircOnly = true;

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setZircOnly(zircOnly);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

        zircOnly = false;
        searchCriteria.setZircOnly(zircOnly);
        abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);
    }

    // Test search by:
    //  antigen name
    // Todo: Need to add an AB that has a gene associated once in production.
    //@Test
    public void getAntibodyByAntigenName() {

        String antigenName = "tbx16";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAntigenGeneName(antigenName);
        searchCriteria.setAntigenNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
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
                for (MarkerAlias alias : aliases) {
                    if (alias.getAlias().indexOf(antigenName) > -1) {
                        foundAB = ab;
                        foundGene = true;
                    }
                }
            }
        }
        // found antibody
        assertEquals("anti-Tbx16", foundAB.getName());
        // found foxd3 gene on
        assertTrue(foundGene);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
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

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);
        assertTrue(abs.size() > 0);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    @Test
    public void searchAntibodyByAntibodyName() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        String abName = "x";
        searchCriteria.setName(abName);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);
        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by:
    //  antigen name
    // ToDo: Need to wait until code in production and antigen gene is created for an ab
    //@Test
    public void getAntibodyByAntigenGeneName() {

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();

        String antigenName = "propionyl-Coenzyme A";
        searchCriteria.setAntigenGeneName(antigenName);
        searchCriteria.setAntigenNameFilterType(FilterType.CONTAINS);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
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
                for (MarkerAlias alias : aliases) {
                    if (alias.getAlias().indexOf(antigenName) > -1) {
                        foundAB = ab;
                        foundGene = true;
                    }
                }
            }
        }
        // found antibody
        assertEquals("zn-5", foundAB.getName());
        // found pcca gene on
        assertTrue(foundGene);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test for assay
    @Test
    public void getAntibodyByAssay() {
        String assayName = Assay.IMMUNOHISTOCHEMISTRY.getName();

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAssay(assayName);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
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

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }

    // Test search by
    // single ao term
    // ab name
    @Test
    public void getAntibodyByAnatomyTermAndABName() {

        String aotermCNV = "cranial nerve V";
        String name = "z";

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setName(name);
        searchCriteria.setAntibodyNameFilterType(FilterType.CONTAINS);
        searchCriteria.setAnatomyTermsString(aotermCNV);

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
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

        List<Antibody> abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        int numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

        searchCriteria.setZircOnly(false);

        abs = antibodyRep.getAntibodies(searchCriteria);
        assertTrue(abs != null);

        numberOfAb = antibodyRep.getNumberOfAntibodies(searchCriteria);
        assertTrue(numberOfAb > 0);

    }


    @Test
    public void getHostSpeciesList() {

        List<Species> species = antibodyRep.getHostSpeciesList();
        assertTrue(species != null);

    }

    @Test
    public void getImmunogenSpeciesList() {

        List<Species> species = antibodyRep.getImmunogenSpeciesList();
        assertTrue(species != null);

    }

    // ToDo: Need to wait until code is in production for an antigen gene
    //@Test
    public void getAssociatedGenes() {
        String abName = "anti-Tbx16";
        String geneID = "ZDB-GENE-980526-143";
        String assocGene = "ZDB-GENE-990415-9";

        MarkerRepository mr = RepositoryFactory.getMarkerRepository();
        Marker marker = mr.getMarkerByID(geneID);
        Marker marker2 = mr.getMarkerByID(assocGene);
        Set<MarkerRelationship> rel1 = marker.getFirstMarkerRelationships();
        Set<MarkerRelationship> rel = marker2.getSecondMarkerRelationships();

        Antibody ab = antibodyRep.getAntibodyByName(abName);
        assertTrue(ab != null);

        List<Marker> markers = ab.getAllRelatedMarker();
        assertTrue(markers != null);

    }

    @Test
    public void getAllAntibodiesByAOTerm() {
        String aoTermName = "forerunner cell group";
        AnatomyItem term = anatomyRep.getAnatomyItem(aoTermName);

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTerm(term);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        int count = antibodyRep.getAntibodiesByAOTermCount(term);
        assertTrue(count > 0);

        List<Antibody> abs = antibodyRep.getAntibodiesByAOTerm(term, pagination);
        assertTrue(abs != null);
        assertTrue(abs.size() == count);
    }

    @Test
    public void getAllAntibodiesByAOTermSecondaryMotorNeuron() {
        //  pectoral fin
        String aoTermZdbID = "ZDB-ANAT-011113-511";
        AnatomyItem term = new AnatomyItem();
        term.setZdbID(aoTermZdbID);

        AntibodySearchCriteria searchCriteria = new AntibodySearchCriteria();
        searchCriteria.setAnatomyTerm(term);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        int count = antibodyRep.getAntibodiesByAOTermCount(term);
        assertTrue(count > -1);

        List<Antibody> abs = antibodyRep.getAntibodiesByAOTerm(term, pagination);
    }

    @Test
    public void getFigureCount() {
        // zn-5
        String abName = "Ab1-elavl3/4";
        Antibody antibody = antibodyRep.getAntibodyByName(abName);
        // spinal cord
        String aoID = "ZDB-ANAT-010921-494";
        AnatomyItem aoTerm = new AnatomyItem();
        aoTerm.setZdbID(aoID);

        int numOfFigures = antibodyRep.getNumberOfFiguresPerAoTerm(antibody, aoTerm, Figure.Type.FIGURE);
        assertTrue(numOfFigures > 0 );

        List<Figure> figures = antibodyRep.getFiguresPerAoTerm(antibody, aoTerm);
        assertTrue(figures != null);
        assertEquals(true, figures.size() > 0);
    }

    @Test
    public void getPublicationsPerAntibodyAndAOTerm() {
        String abName = "zn-5";
        Antibody antibody = antibodyRep.getAntibodyByName(abName);
        // brain
        String aoID = "ZDB-ANAT-010921-415";
        AnatomyItem aoTerm = new AnatomyItem();
        aoTerm.setZdbID(aoID);

        PaginationResult<Publication> pubs = antibodyRep.getPublicationsWithFigures(antibody, aoTerm);
        assertTrue(pubs != null);

        assertEquals(true, pubs.getPopulatedResults().size() > 0);
    }

    @Test
    public void getAntibodiesPerPub() {
        // Trevarrow, Marks and Kimmel
        String pubZdbID = "ZDB-PUB-961014-1139";
        PublicationRepository pr = RepositoryFactory.getPublicationRepository();
        Publication pub = pr.getPublication(pubZdbID);

        List<Antibody> abs = antibodyRep.getAntibodiesByPublication(pub);
        assertTrue(abs != null);
    }

    @Test
    public void getSuppliers() {
        String abName = "anti-Tbx16";
        Antibody antibody = antibodyRep.getAntibodyByName(abName);

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
    public void externalNoteRecordAttribution() {

        String externalNoteZdbID = "ZDB-EXTNOTE-080424-1";
        ActiveData data = new ActiveData();
        data.setZdbID(externalNoteZdbID);

        String pubID = "ZDB-PUB-070210-20";
        ActiveSource source = new ActiveSource();
        source.setZdbID(pubID);

        RecordAttribution rec = ir.getRecordAttribution(data, source, RecordAttribution.SourceType.STANDARD);
        assertTrue(rec == null);

        PublicationAttribution record = new PublicationAttribution();
        record.setDataZdbID("externalNoteZdbID");
        Publication pub = new Publication();
        pub.setZdbID(pubID);
        record.setPublication(pub);
        rec = ir.getPublicationAttribution(record);
        assertTrue(rec == null);

    }

    @Test
    public void getExternalNote() {
        String externalNoteZdbID = "ZDB-EXTNOTE-080424-1";

        ExternalNote note = ir.getExternalNoteByID(externalNoteZdbID);

    }

    @Test
    public void antibodyExternalNote() {

        String antibodyName = "anti-Tbx16";
        Antibody ab = antibodyRep.getAntibodyByName(antibodyName);

        Set<AntibodyExternalNote> notes = ab.getExternalNotes();
        assertTrue(notes != null);

    }

    @Test
    public void labSupplier() {
        String antibodyName = "anti-Tbx16";
        Antibody antibody = antibodyRep.getAntibodyByName(antibodyName);

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
        Antibody antibody = antibodyRep.getAntibodyByName(antibodyName);
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
            Antibody antibody = new Antibody();
            antibody.setMarkerType(mt);
            antibody.setName("werner");
            antibody.setAbbreviation("werner");
            antibody.setOwner(TestConfiguration.getPerson());
            session.save(antibody);
            session.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
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
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
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
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            try {
                tx.rollback();
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    //@Test
    public void getAllMapNameForAntibodies(){
        String string = "pdx";
        List<AllMarkerNamesFastSearch> all = antibodyRep.getAllNameAntibodyMatches(string);
        assertTrue(all != null);
    }

    @Test
    public void getUsedAntibodies(){
        List<Species> all = antibodyRep.getUsedHostSpeciesList();
        assertTrue(all != null);
    }

    @Test
    public void getPublicationsPerAntibodyAndAoTerm(){
        String antibodyName = "zn-5";
        String aoTermName = "spinal cord";
        Antibody antibody = antibodyRep.getAntibodyByName(antibodyName);
        AnatomyItem aoTerm =anatomyRep.getAnatomyItem(aoTermName);

        PaginationResult<Publication> pubs = antibodyRep.getPublicationsWithFigures(antibody, aoTerm);
        assertTrue(pubs != null);
    }

}