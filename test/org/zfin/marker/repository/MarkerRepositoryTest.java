package org.zfin.marker.repository;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.InvokerTransformer;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.zfin.AbstractDatabaseTest;
import org.zfin.Species;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.construct.ConstructComponent;
import org.zfin.construct.ConstructCuration;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.mapping.MarkerLocation;
import org.zfin.marker.*;
import org.zfin.marker.fluorescence.FluorescentMarker;
import org.zfin.marker.fluorescence.FluorescentProtein;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Genotype;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.mutant.SequenceTargetingReagent;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.Term;
import org.zfin.ontology.repository.OntologyRepository;
import org.zfin.profile.MarkerSupplier;
import org.zfin.profile.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getMarkerRepository;

public class MarkerRepositoryTest extends AbstractDatabaseTest {

    private static final MarkerRepository markerRepository = getMarkerRepository();
    private static final ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private static final PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private static final InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    @After
    public void closeSession() {
        super.closeSession();
        // make sure to close the session to be able to re-create the entities
        HibernateUtil.closeSession();
    }


    @Test
    public void testMarkerTypes() {
        List<MarkerType> list = markerRepository.getAllMarkerTypes();
        assertNotNull("Contains at least one marker", list);
    }

    @Test
    public void testMarkerTypeAndGroup() {
//        Marker pax2a = markerRepository.getMarkerByAbbreviation("pax2a");
        Marker pax2a = markerRepository.getGeneByID("ZDB-GENE-990415-8");
        assertSame("pax2a has type GENE", pax2a.getMarkerType().getType(), Marker.Type.GENE);
        assertNotSame("pax2a doesn't have type BAC", pax2a.getMarkerType().getType(), Marker.Type.BAC);
        assertTrue("pax2a is in the type group GENEDOM", pax2a.isInTypeGroup(Marker.TypeGroup.GENEDOM));
        assertFalse("pax2a is not in the type group BAC", pax2a.isInTypeGroup(Marker.TypeGroup.BAC));
    }

    @Test
    public void testMarkerTypeGroup() {
        MarkerTypeGroup group = markerRepository.getMarkerTypeGroupByName(Marker.TypeGroup.GENEDOM.name());
        assertTrue("genedome has more than 5 types associated", group.getTypeStrings().size() > 5);
    }


    @Test
    public void testMarkersByAbbreviation() {
        List<Marker> paxs = markerRepository.getMarkersByAbbreviation("pax");
        assertNotNull("pax should not be null", paxs);
        assertTrue("pax should have multiple markers", paxs.size() > 200);
    }

    @Test
    public void testGenesByAbbreviation() {
        List<Marker> soxs = markerRepository.getGenesByAbbreviation("sox");
        assertNotNull("sox should not be null", soxs);
        assertTrue("sox should have multiple markers", soxs.size() > 1);
        for (Marker m : soxs) {
            assertTrue(m.getZdbID().startsWith("ZDB-GENE"));
        }
    }

    @Test
    public void testGeneById() {
        Assert.assertNull(markerRepository.getGeneByID("ZDB-TSCRIPT-090929-6229"));
        assertNotNull(markerRepository.getGeneByID("ZDB-GENE-990415-200"));
    }

    @Test
    public void getMarkerByName() {
        Marker markerByName = markerRepository.getMarkerByName("clewless");
        assertNotNull(markerByName);
        assertThat("ZDB-GENE-070117-772", equalTo(markerByName.getZdbID()));
    }

    @Test
    public void getSpecificDataAlias() {
        Marker gene = markerRepository.getGeneByID("ZDB-GENE-990415-200");
        MarkerAlias alias = markerRepository.getSpecificDataAlias(gene, "paxzfa");
        assertNotNull(alias);
    }

    @Test
    public void testFluorescentProteins() {
        Marker efg = markerRepository.getMarkerByID("ZDB-EFG-190123-1");
        assertNotNull(efg);
        assertNotNull(efg.getFluorescentProteinEfgs());
        assertEquals(efg.getFluorescentProteinEfgs().iterator().next().getName(), "mOrange2");
        assertNotNull(efg.getFluorescentMarkers());
        assertEquals(efg.getFluorescentMarkers().size(), 1);
        assertNotNull(efg.getFluorescentMarkers().iterator().next().getProtein());
        assertEquals(efg.getFluorescentMarkers().iterator().next().getProtein().getName(), "mOrange2");
    }

    @Test
    public void testFluorescentEfg() {
        List<FluorescentMarker> efgList = markerRepository.getAllFluorescentEfgs();
        assertNotNull(efgList);
    }

    @Test
    public void testFluorescentConstruct() {
        List<FluorescentMarker> constructs = markerRepository.getAllFluorescentConstructs();
        assertNotNull(constructs);
    }

    @Test
    public void getProblemTypes() {
        List<String> problemTypes = markerRepository.getProblemTypes();
        assertNotNull(problemTypes);
    }

    @Test
    public void getAllFluorescentProteins() {
        List<FluorescentProtein> efg = markerRepository.getAllFluorescentProteins();
        assertNotNull(efg);
    }

    @Test
    public void getAllTranscriptTypes() {
        List<TranscriptType> transcriptTypes = markerRepository.getAllTranscriptTypes();
        assertNotNull(transcriptTypes);
        assertTrue(transcriptTypes.size() > 15);
    }

    @Test
    public void getVectorNames() {
        List<String> vectorNames = markerRepository.getVectorNames();
        assertNotNull(vectorNames);
        assertTrue(vectorNames.size() > 15);
    }

    @Test
    public void getProbeLibraries() {
        List<ProbeLibrary> probeLibraries = markerRepository.getProbeLibraries();
        assertNotNull(probeLibraries);
        assertTrue(probeLibraries.size() > 15);
    }

    @Test
    public void getProbeLibrary() {
        ProbeLibrary probeLibrary = markerRepository.getProbeLibrary("ZDB-PROBELIB-080715-11");
        assertNotNull(probeLibrary);
    }

    @Test
    public void testConstruct() {
        ConstructCuration cc = HibernateUtil.currentSession().get(ConstructCuration.class, "ZDB-GTCONSTRCT-110310-1");
        assertNotNull(cc);
    }

    @Test
    public void getMarkerLocationByID() {
        MarkerLocation location = markerRepository.getMarkerLocationByID("ZDB-SFCL-220325-2");
        assertNotNull(location);
    }

    @Test
    public void geneByAbbreviation() {
        Marker adh8a = markerRepository.getGeneByAbbreviation("adh8a");
        assertNotNull(adh8a);
        assertNotNull(markerRepository.getGeneByAbbreviation("pax3a"));
        assertNotNull(markerRepository.getGeneByAbbreviation("pax6a"));
        assertNull(markerRepository.getGeneByAbbreviation("pax6a-001"));
    }

    /**
     * Test that a new Marker record takes the Marker type in the sequence
     * that is given by the Marker
     */

    @Test
    public void testZdbSequenceGenerator() {
        Session session = HibernateUtil.currentSession();
        MarkerType type = markerRepository.getMarkerTypeByName(Marker.Type.BAC.toString());
        Marker marker = new Marker();
        marker.setMarkerType(type);
        marker.setName("test marker");
        marker.setAbbreviation("test Abbreviation");
        marker.setAbbreviationOrder("test Abbreviation");
        marker.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(marker);

        String zdbID = marker.getZdbID();
        assertNotNull("non-null ZDB ID", zdbID);
        assertTrue("ID contains BAC", zdbID.contains(Marker.Type.BAC.toString()));
    }

    @Test
    public void testGetLG() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the existing genes which might be merged
        Marker marker1 = markerRepository.getMarkerByID("ZDB-EST-000426-1181");
        assertTrue("marker lg list contains all self panel mappings", markerRepository.getLG(marker1).contains("13") && markerRepository.getLG(marker1).contains("23"));
        Marker marker2 = markerRepository.getMarkerByID("ZDB-GENE-990415-72");
        assertTrue("gene lg list contains its est's panel mapping", markerRepository.getLG(marker2).contains("23"));
        assertFalse("gene lg list contains no bogus mapping", markerRepository.getLG(marker2).contains("1"));
        Marker marker3 = markerRepository.getMarkerByID("ZDB-GENE-060526-178");
        assertTrue("gene lg list contains clone's panel mapping", markerRepository.getLG(marker3).contains("13"));

        Marker marker4 = markerRepository.getMarkerByID("ZDB-RAPD-980526-288");
        assertTrue("marker lg list contains self linkage group mapping", markerRepository.getLG(marker4).contains("12"));
        Marker marker5 = markerRepository.getMarkerByID("ZDB-BAC-030616-45");
        assertTrue("marker lg list contains linkage mapping of contained marker/segment", markerRepository.getLG(marker5).contains("9"));
        Marker marker6 = markerRepository.getMarkerByID("ZDB-GENE-061013-119");
        assertTrue("gene lg list contains clone's linkage mapping", markerRepository.getLG(marker6).contains("19"));

        Marker marker7 = markerRepository.getMarkerByID("ZDB-GENE-070117-36");
        assertTrue("gene lg list contains allele's linkage group mapping", markerRepository.getLG(marker7).contains("23"));

        Marker marker8 = markerRepository.getMarkerByID("ZDB-GENE-070117-2287");
        assertTrue("marker lg list contains allele's panel mapping", markerRepository.getLG(marker8).contains("7"));
    }

    @Test
    public void markerHistory() {

        // egr2b
        Marker gene = getMarkerRepository().getMarkerByID("ZDB-GENE-980526-283");
        assertNotNull(gene);
        Set<MarkerHistory> markerHistory = gene.getMarkerHistory();
        assertNotNull(markerHistory);
        for (MarkerHistory history : markerHistory) {
            // make sure that this event has an attribution
            if (history.getZdbID().equals("ZDB-NOMEN-030723-28"))
                assertNotNull(history.getAttributions());
        }
    }

    @Test
    public void testRenameMarker() {

        Session session = HibernateUtil.currentSession();
        Marker marker = insertTestMarker();
        Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");
        marker.setName("test1 name");
        marker.setAbbreviation("testsierra");
        markerRepository.renameMarker(marker, publication, MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES, "old symbol", "old name");
        session.flush();
        session.refresh(marker);
        assertEquals("Created one new alias", 1, marker.getAliases().size());
        assertEquals("Created one new marker history record", 1, marker.getMarkerHistory().size());
        assertNotNull(infrastructureRepository.getRecordAttribution(
                marker.getMarkerHistory().iterator().next().getMarkerAlias().getZdbID(),
                publication.getZdbID(), null));
    }

    private Marker insertTestMarker() {
        Session session = currentSession();
        Marker marker = new Marker();
        marker.setAbbreviation("reno");
        marker.setName("Reno Test Name");
        //should this be an enum?
        marker.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        marker.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(marker);
        return marker;
    }

    @Test
    public void retrieveSingleGeneFromClone() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the exisitng genes which might be merged
        Clone clone = markerRepository.getCloneById("ZDB-CDNA-040425-3060");
        Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(clone);
        assertEquals("Only one gene found", 1, genes.size());
        assertEquals("Found gene", "ZDB-GENE-040426-2113", genes.iterator().next().getZdbID());
    }

    @Test
    public void testGetSpecificMarkerRelationship() {
        Clone clone = markerRepository.getCloneById("ZDB-CDNA-040425-3060");
        Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(clone);
        assertEquals("Only one gene found", 1, genes.size());
        MarkerRelationship mrel = markerRepository.getMarkerRelationship(genes.iterator().next(), clone, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
        assertEquals("Found marker relationship", "ZDB-MREL-040426-3790", mrel.getZdbID());
    }


    @Test
    public void testRemoveRedundantDBLinks() {
        Session session = HibernateUtil.currentSession();

        SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
        ProfileRepository personRepository = RepositoryFactory.getProfileRepository();

        ReferenceDatabase refDb = sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.RNA,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.Type.ZEBRAFISH);


        Accession acc1 = new Accession();
        acc1.setNumber("BC:01");
        acc1.setDefline("defline leopard");
        acc1.setLength(12);
        acc1.setReferenceDatabase(refDb);
        session.save(acc1);

        Accession acc2 = new Accession();
        acc2.setNumber("BC:02");
        acc2.setDefline("defline leopard");
        acc2.setLength(1233);
        acc2.setReferenceDatabase(refDb);
        session.save(acc2);

        Marker segment = new Marker();
        segment.setAbbreviation("MGC:test");
        segment.setName("MGC:test");
        segment.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.CDNA.toString()));
        segment.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(segment);

        Marker gene = new Marker();
        gene.setAbbreviation("renogene");
        gene.setName("existing reno gene");
        gene.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.GENE.toString()));
        gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene);

        Marker segment2 = new Marker();
        segment2.setAbbreviation("MGC:test2");
        segment2.setName("MGC:test2");
        segment2.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.CDNA.toString()));
        segment2.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(segment2);

        Marker gene2 = new Marker();
        gene2.setAbbreviation("renogene2");
        gene2.setName("existing reno gene 2");
        gene2.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.GENE.toString()));
        gene2.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene2);


        String curationPubZdbID = "ZDB-PUB-020723-3";
        String journalPubZdbID = "ZDB-PUB-041006-7";

        //this case will get a deletion
        markerRepository.addDBLink(gene, acc1.getNumber(), refDb, curationPubZdbID);
        markerRepository.addDBLink(segment, acc1.getNumber(), refDb, curationPubZdbID);
        markerRepository.addSmallSegmentToGene(gene, segment, curationPubZdbID);

        //this case won't have the dblink deleted
        markerRepository.addDBLink(gene2, acc2.getNumber(), refDb, journalPubZdbID);
        markerRepository.addDBLink(segment2, acc2.getNumber(), refDb, curationPubZdbID);
        markerRepository.addSmallSegmentToGene(gene2, segment2, curationPubZdbID);

        //make sure it's all in the database before testing
        session.flush();

        //try the cleanup function
        Set<Accession> accessions = new HashSet<>();
        accessions.add(acc1);
        MarkerService.removeRedundantDBLinks(gene, accessions);

        accessions.remove(acc1);
        accessions.add(acc2);

        MarkerService.removeRedundantDBLinks(gene2, accessions);

        session.refresh(acc1);
        session.refresh(acc2);

        assertEquals("test accession acc1 should have one marker", acc1.getMarkers().size(), 1);
        assertEquals("test accession acc2 should have two markers", acc2.getMarkers().size(), 2);
    }


    //ZDB-MREL-021003-11
    @Test
    public void retrieveLinkageGroupFromClone() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the exisitng genes which might be merged
        Marker clone = markerRepository.getMarkerByID("ZDB-CDNA-040425-118");
        Set<LinkageGroup> groups = MarkerService.getLinkageGroups(clone);
        assertNotNull(groups);
        assertEquals("2 linkage groups found", 1, groups.size());
        LinkageGroup group = groups.iterator().next();
        assertEquals("First LG", "1", group.getName());
    }

    @Test
    public void retrieveLinkageGroupFromGene() {
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-990415-72");
        Set<LinkageGroup> groups = MarkerService.getLinkageGroups(gene);
        assertNotNull(groups);
        assertTrue(groups.size() > 1);
    }

    @Test
    public void retrieveLinkageGroupFromGeneFgf14() {
        Marker gene = markerRepository.getMarkerByID("ZDB-GENE-060506-1");
        Set<LinkageGroup> groups = MarkerService.getLinkageGroups(gene);
        assertNotNull(groups);
        assertEquals("1 linkage groups found", 1, groups.size());
        LinkageGroup group = groups.iterator().next();
        assertEquals("First LG", "9", group.getName());
    }

    @Test
    public void genedomEfgMarkers() {
        List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup("pax6", Marker.TypeGroup.GENEDOM_AND_EFG);
        assertNotNull(markers);
        assertTrue(markers.size() > 0);
        for (Marker thisMarker : markers) {
            assertTrue(thisMarker.isInTypeGroup(Marker.TypeGroup.GENEDOM_AND_EFG));
        }
    }

    @Test
    @Ignore("Just used for assessing performance")
    public void markerLookupPerformance() {
        List<Marker> markers = markerRepository.getMarkersByAbbreviation("fgf");
        long startTime = System.currentTimeMillis();
        for (Marker marker : markers) {
            markerRepository.getMarkersByAbbreviationAndGroup(marker.getAbbreviation(), Marker.TypeGroup.GENEDOM_AND_EFG);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(totalTime / 1000f);
    }


    @Test
    public void testClone() {
        Session session = HibernateUtil.currentSession();
        Marker marker1 = markerRepository.getMarkerByID("ZDB-CDNA-080114-111");
        assertTrue("Marker is a clone", marker1 instanceof Clone);
        Clone clone = (Clone) marker1;
        Integer rating = 3;
        clone.setRating(rating);
        // NOTE: must be CDNA or EST to set as a non-null problem type
        clone.setProblem(Clone.ProblemType.CHIMERIC);
        session.update(clone);
        session.flush();
        Clone clone2 = session.createQuery("from Clone c where c.zdbID = 'ZDB-CDNA-080114-111'", Clone.class).uniqueResult();
        assertEquals(clone2.getRating(), rating);
        assertEquals(clone2.getProblem(), Clone.ProblemType.CHIMERIC);
    }


    @Test
    public void getGeneClone() {
        String name = "fj17b12";
        Marker clone = markerRepository.getMarkerByAbbreviation(name);
        assertNotNull(clone);
        assertTrue(clone instanceof Clone);
    }

    @Test
    public void getESTClone() {
        String name = "eu815";
        Marker clone = markerRepository.getMarkerByAbbreviation(name);
        assertNotNull(clone);
    }

    @Test
    public void getMarkerAlias() {
        // acerebellar, alias for fgf8a
        String zdbID = "ZDB-DALIAS-070117-777";
        MarkerAlias alias = markerRepository.getMarkerAlias(zdbID);
        assertNotNull(alias);
    }

    @Test
    public void getPolymeraseNames() {
        List<String> polymeraseNames = markerRepository.getPolymeraseNames();
        assertNotNull(polymeraseNames);
    }

    @Test
    public void getCloneSites() {
        List<String> polymeraseNames = markerRepository.getCloneSites();
        assertNotNull(polymeraseNames);
    }

    @Test
    public void thisseProbesForAoTerm() {
        String aoTermName = "pancreas";
        Term term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-130");
        term.setTermName(aoTermName);

        Session session = HibernateUtil.currentSession();
        String hql = "select distinct stat.probe " +
                "     from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           and stat.subterm = :aoterm";
        Query<Marker> query = session.createQuery(hql, Marker.class);
        query.setParameter("aoterm", term);

        List<Marker> list = query.list();
        assertNotNull(list);
        assertTrue(list.size() > 0);

        hql = " " +
                "     from AntibodyAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           and stat.subterm = :aoterm";
        Query<AntibodyAOStatistics> query1 = session.createQuery(hql, AntibodyAOStatistics.class);
        query1.setParameter("aoterm", term);
        List<AntibodyAOStatistics> listStat = query1.list();
        assertNotNull(listStat);
        assertTrue(listStat.size() > 0);

    }

    @Test
    public void probesStatistics() {
        String aoTermName = "brain";
        GenericTerm term = new GenericTerm();
        term.setZdbID("ZDB-TERM-100331-8");
        term.setTermName(aoTermName);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        pagination.setFirstPageRecord(0);
        // without substructures
        PaginationResult<HighQualityProbe> result = markerRepository.getHighQualityProbeStatistics(term, pagination, false);
        assertNotNull(result);
        assertTrue(result.getTotalCount() > 0);

        // including substructures
        result = markerRepository.getHighQualityProbeStatistics(term, pagination, true);
        assertNotNull(result);
        assertTrue(result.getTotalCount() > 0);

    }

    @Test
    public void hibernateQueryIterate() {
        String aoTermName = "brain";
        Session session = HibernateUtil.currentSession();
        OntologyRepository anatomyRep = RepositoryFactory.getOntologyRepository();
        GenericTerm aoTerm = anatomyRep.getTermByName(aoTermName, Ontology.ANATOMY);
        String hql = " select distinct(stat.fstat_feat_zdb_id) as featureID, probe.mrkr_abbrev as probeAbbrev, gene.mrkr_zdb_id as geneID," +
                "                       gene.mrkr_abbrev as geneAbbrev,gene.mrkr_abbrev_order  as geneAbbrevOrder " +
                "from feature_stats as stat, marker as gene, marker as probe " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = gene.mrkr_zdb_id " +
                "           and fstat_feat_zdb_id = probe.mrkr_zdb_id " +
                "           and fstat_type = :type" +
                "     order by gene.mrkr_abbrev_order ";
        NativeQuery query = session.createNativeQuery(hql);
        // organism subdivision
        query.setParameter("aoterm", "ZDB-TERM-100331-1266");
        query.setParameter("type", "High-Quality-Probe");
        query.setFirstResult(0);
        query.setMaxResults(5);
        ScrollableResults results = query.scroll();
        List<HighQualityProbe> probes = new ArrayList<>();
        while (results.next()) {
            Marker probe = new Marker();
            Object[] objects = results.get();
            probe.setZdbID((String) objects[0]);
            probe.setAbbreviation((String) objects[1]);
            Marker gene = new Marker();
            gene.setZdbID((String) objects[2]);
            gene.setAbbreviation((String) objects[3]);
            HighQualityProbe hqp = new HighQualityProbe(probe, aoTerm);
            hqp.addGene(gene);
            probes.add(hqp);
        }
        results.last();

        hql = " select stat.* from feature_stats as stat, marker " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = mrkr_zdb_id " +
                "           and fstat_type = :type" +
                "     order by mrkr_abbrev_order ";

        query = session.createSQLQuery(hql);
        query.setParameter("aoterm", "ZDB-ANAT-010921-587");
        query.setParameter("type", "High-Quality-Probe");
        results = query.scroll();

        results.last();
        assertTrue(true);
    }

    @Test
    public void createNewOrthologyNote() {

        String geneName = "fgf8a";
        Marker gene = markerRepository.getMarkerByAbbreviation(geneName);
        assertNotNull(gene);

        markerRepository.createOrUpdateOrthologyExternalNote(gene, "This is a note");
    }

    @Test
    public void orthologyNote() {
        String geneName = "fgf8a";
        Marker gene = markerRepository.getMarkerByAbbreviation(geneName);
        assertNotNull(gene);

        Set<OrthologyNote> notes = gene.getOrthologyNotes();
        assertNotNull(notes);
    }

    /**
     * Check for an existent marker by abbreviation.
     * Check the the lookup is case insensitive.
     * <p>
     * Check for the non-existence as well.
     */
    @Test
    public void markerExists() {
        String geneName = "FGF8A";
        boolean markerExists = markerRepository.isMarkerExists(geneName);
        assertTrue(markerExists);

        geneName = "fgf88ga";
        markerExists = markerRepository.isMarkerExists(geneName);
        assertFalse(markerExists);
    }

    @Test
    public void createDataAlias() {
        String pubID = "ZDB-PUB-020723-5";
        String antibodyID = "ZDB-ATB-081002-16";
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(antibodyID);
        Publication publication = publicationRepository.getPublication(pubID);
        markerRepository.addMarkerAlias(antibody, "Bruno", publication);
    }

    @Test
    public void retrieveSingleTargetGeneFromMorpholino() {
        // MO1-adam8a has one target gene
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagentByAbbreviation("MO1-adam8a");
        List<Marker> targetGenes = markerRepository.getTargetGenesAsMarkerForSequenceTargetingReagent(sequenceTargetingReagent);
        assertNotNull(targetGenes);
        assertEquals(1, targetGenes.size());
//        assertEquals("adam8a", targetGenes.iterator().next().getAbbreviation());
        assertEquals("ZDB-GENE-030616-622", targetGenes.iterator().next().getZdbID());

    }

    @Test
    public void retrieveMultipleTargetGenesFromMorpholino() {
        // MO4-rbpja+rbpjb has two target genes
        SequenceTargetingReagent sequenceTargetingReagent = markerRepository.getSequenceTargetingReagentByAbbreviation("MO4-rbpja,rbpjb");
        List<Marker> targetGenes = markerRepository.getTargetGenesAsMarkerForSequenceTargetingReagent(sequenceTargetingReagent);
        assertNotNull(targetGenes);
        assertEquals(2, targetGenes.size());
        Iterator<Marker> iter = targetGenes.iterator();
//        assertEquals("rbpja", iter.next().getAbbreviation());
//        assertEquals("rbpjb", iter.next().getAbbreviation());
        assertEquals("ZDB-GENE-031117-1", iter.next().getZdbID());
        assertEquals("ZDB-GENE-070319-1", iter.next().getZdbID());

    }

    @Test
    public void testMiniGene() throws Exception {
        MiniGeneController miniGeneController = new MiniGeneController();
        Model model = new ExtendedModelMap();
        miniGeneController.getMiniGeneView(model, "ZDB-GENE-990603-12", null, null, null);
    }

    @Test
    public void markerHistoryView() {
        assertTrue(markerRepository.getHasMarkerHistory("ZDB-GENE-990603-12"));
    }

    @Test
    public void getPreviousNamesLight() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-010606-1");
        List<PreviousNameLight> previousNames = markerRepository.getPreviousNamesLight(m);
        assertTrue(previousNames.size() >= 3);
    }

    @Test
    public void getPreviousNamesLightMultipleAttributionTest() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-010504-1");
        List<PreviousNameLight> previousNames = markerRepository.getPreviousNamesLight(m);
        Collection<String> aliases = CollectionUtils.collect(previousNames, InvokerTransformer.getInstance("getAlias"));
        assertThat("ff1b and nr5a4 should show up as a previous names for nr5a1a",
                aliases, hasItems("<i>ff1b</i>", "<i>nr5a4</i>"));
    }

    @Test
    public void getMarkerSequences() {
        SNP m = markerRepository.getSNPByID("ZDB-SNP-060626-88");
        assertEquals("TATTTC", m.getSequence().getSequence().substring(0, 6));
    }

    @Test
    public void getMarkersByAlias() {
        List<Marker> markers = markerRepository.getMarkersByAlias("hab");
        assertEquals(1, markers.size());
        assertEquals("ZDB-GENE-010606-1", markers.get(0).getZdbID());

        markers = markerRepository.getMarkersByAlias("tcr-alpha");
        assertThat(markers.size(), greaterThan(100));
        assertThat(markers.size(), lessThan(200));
    }

    @Test
    public void getMarkerByAbbrevationIgnoreCase() {

        assertNotNull(markerRepository.getMarkerByAbbreviationIgnoreCase("DKEY-49F1"));
        assertNotNull(markerRepository.getMarkerByAbbreviationIgnoreCase("dkey-49f1"));
    }

    @Test
    public void getRelatedMarkerDisplayFastForTypes() {
        Marker marker = markerRepository.getMarkerByID("ZDB-TGCONSTRCT-100525-3");
        assertNotNull(marker);
        List<MarkerRelationshipPresentation> values = markerRepository.getRelatedMarkerOrderDisplayForTypes(marker, true, MarkerRelationship.Type.PROMOTER_OF, MarkerRelationship.Type.CODING_SEQUENCE_OF);
        assertEquals(2, values.size());
        values = markerRepository.getRelatedMarkerOrderDisplayForTypes(marker, true, MarkerRelationship.Type.PROMOTER_OF);
        assertEquals(1, values.size());
        values = markerRepository.getRelatedMarkerOrderDisplayForTypes(marker, true);
        assertEquals(2, values.size());

    }


    @Test
    public void markerDBLinkDisplay() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-990415-200"); // pax6a
        SummaryDBLinkDisplay summaryDBLinkDisplay = MarkerService.getMarkerDBLinkDisplay(m, DisplayGroup.GroupName.SUMMARY_PAGE);
        assertNotNull(summaryDBLinkDisplay);

        List<LinkDisplay> linkDisplayList = markerRepository.getMarkerDBLinksFast(m, DisplayGroup.GroupName.SUMMARY_PAGE);
        assertNotNull(linkDisplayList);


//        m = markerRepository.getTranscriptByName("gsc-002");
        m = markerRepository.getTranscriptByZdbID("ZDB-TSCRIPT-090929-193");
        summaryDBLinkDisplay = MarkerService.getMarkerDBLinkDisplay(m, DisplayGroup.GroupName.SUMMARY_PAGE);
        assertNotNull(summaryDBLinkDisplay);

        linkDisplayList = markerRepository.getMarkerDBLinksFast(m, DisplayGroup.GroupName.SUMMARY_PAGE);
        assertNotNull(linkDisplayList);
        assertThat(linkDisplayList.size(), greaterThan(1));
    }

    @Test
    public void getGeneProducts() {
        List<GeneProductsBean> geneProductsBean = markerRepository.getGeneProducts("ZDB-GENE-980526-561");
        assertNotNull(geneProductsBean);
        assertThat(geneProductsBean.size(), greaterThan(0));
        assertEquals("Q90477", geneProductsBean.get(0).getAccession());

        List<GeneProductsBean> geneProductsBean2 = markerRepository.getGeneProducts("ZDB-GENE-000405-1");
        assertNotNull(geneProductsBean2);
        assertTrue(geneProductsBean2.size() > 0);

        List<GeneProductsBean> geneProductsBean3 = markerRepository.getGeneProducts("ZDB-GENE-030131-2333");
        assertNotNull(geneProductsBean3);
    }

    @Test
    public void getRelatedMarkerDisplayForTypes() {
//        Marker m = markerRepository.getGeneByAbbreviation("alcama");
        Marker m = markerRepository.getGeneByID("ZDB-GENE-990415-30");
        List<MarkerRelationshipPresentation> relationshipPresentations =
                markerRepository.getRelatedMarkerDisplayForTypes(m, true, MarkerRelationship.Type.GENE_PRODUCT_RECOGNIZED_BY_ANTIBODY);
        assertEquals(5, relationshipPresentations.size());
    }

    @Test
    public void getKnockdownReagents() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-980526-403");
        List<MarkerRelationshipPresentation> knockdowns = markerRepository
                .getRelatedMarkerDisplayForTypes(m, false, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        assertTrue(knockdowns.size() > 5);
        assertTrue(knockdowns.size() < 20);
    }

    @Test
    public void getABRegID() {
        String id = markerRepository.getABRegID("ZDB-ATB-190924-2");
        assertNotNull(id);
        assertEquals("AB_11157684", id);
    }


    @Test
    public void getKnockdownReagentsWithMultiplePubs() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-000626-1");
        List<MarkerRelationshipPresentation> knockdowns = markerRepository
                .getRelatedMarkerDisplayForTypes(m, false, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        assertThat(knockdowns.size(), greaterThan(2));
        assertThat(knockdowns.size(), lessThan(20));

        Optional<MarkerRelationshipPresentation> mrpr = knockdowns.stream().filter(presentation -> presentation.getAbbreviation().equals("MO1-tnnt2a")).findFirst();
        assertTrue(mrpr.isPresent());
        MarkerRelationshipPresentation mrp = mrpr.get();
        assertEquals("MO1-tnnt2a", mrp.getAbbreviation());
        assertThat(mrp.getNumAttributions(), greaterThan(1));
        assertThat(mrp.getNumAttributions(), lessThan(4));

        String linkWithAttribution = mrp.getLinkWithAttribution();
        assertTrue(linkWithAttribution.contains("/action/infrastructure/data-citation-list/ZDB-MREL-060317-4\">2</a>)"));
    }

    @Test
    public void isFromChimericClone() {
        assertFalse(markerRepository.isFromChimericClone("ZDB-GENE-980526-403"));
        assertFalse(markerRepository.isFromChimericClone("ZDB-GENE-030131-350"));
        assertTrue(markerRepository.isFromChimericClone("ZDB-GENE-040625-82"));
    }

    @Test
    public void getProteinType() {
        Marker marker = markerRepository.getMarker("ZDB-GENE-980526-403");
        List<String> list = markerRepository.getProteinType(marker);
        assertNotNull(list);
    }

    @Test
    public void getSuppliersForMarker() {
        List<MarkerSupplier> markerSuppliers = markerRepository.getSuppliersForMarker("ZDB-ATB-081002-19");
        assertEquals(1, markerSuppliers.size());
        assertEquals("ZDB-ATB-081002-19", markerSuppliers.get(0).getMarker().getZdbID());
    }

    @Test
    public void markerExistsForZdbID() {
        assertTrue(markerRepository.markerExistsForZdbID("ZDB-GENE-980526-403"));
        assertFalse(markerRepository.markerExistsForZdbID("ZDB-GENE-109120-403"));
    }

    @Test
    public void getMarkerZdbIdsForType() {
        List<String> zdbIds = markerRepository.getMarkerZdbIdsForType(Marker.Type.GENEP);
        assertThat(zdbIds.size(), greaterThan(150));
    }

    @Test
    public void getGeoMarkerCandidates() {
        Map<String, String> candidates = markerRepository.getGeoMarkerCandidates();
        assertNotNull(candidates);
        assertThat(candidates.keySet().size(), greaterThan(30000));
        assertThat(candidates.keySet().size(), lessThan(300000));
    }

    @Test
    public void getAllTranscriptTypeStatusDefinitions() {
        List<TranscriptTypeStatusDefinition> defninitions = markerRepository.getAllTranscriptTypeStatusDefinitions();
        assertNotNull(defninitions);
    }

    @Test
    public void getMarkerFamilyName() {
        MarkerFamilyName familyName = markerRepository.getMarkerFamilyName("Zinc fingers");
        assertNotNull(familyName);
        assertEquals("Zinc fingers", familyName.getMarkerFamilyName());
    }

    @Test
    public void getMarkerFamilyNamesBySubstring() {
        List<MarkerFamilyName> familyNames = markerRepository.getMarkerFamilyNamesBySubstring("zinc fingers");
        assertNotNull(familyNames);
        assertEquals(18, familyNames.size());
    }

    @Test
    public void getConstructsForGene() {
        Marker m = markerRepository.getMarkerByID("ZDB-GENE-030710-1");
        List<Marker> constructs = markerRepository.getConstructsForGene(m);
        assertThat(constructs.size(), greaterThan(2));
        assertThat(constructs.size(), lessThan(4));
    }

    @Test
    public void getStrainForTranscript() {
        Genotype g = markerRepository.getStrainForTranscript("ZDB-TSCRIPT-090929-10104");
        assertNotNull(g);
        assertEquals("Tübingen", g.getName());
    }

    @Test
    public void getVegaGeneDBLinksTranscript() {
        Marker m = markerRepository.getMarkerByID("ZDB-GENE-980528-2060");
        List<LinkDisplay> links = markerRepository.getVegaGeneDBLinksTranscript(m, DisplayGroup.GroupName.SUMMARY_PAGE);
        assertNotNull(links);
        assertEquals(1, links.size());
        assertTrue(links.get(0).getAccession().startsWith("OTTDARG0000"));
    }

    @Test
    public void getSequenceTargetingReagent() {
        Marker marker = markerRepository.getMarkerByID("ZDB-MRPHLNO-100729-2");
        assertNotNull(marker);
        marker = markerRepository.getMarkerByID("ZDB-TSCRIPT-090929-193");
        assertNotNull(marker);
    }

    @Test
    public void shouldFindMorpholinoWithKnownSequence() {
        String sequenceForMO1adam8a = "TAAATAGTCCAGTGTATCGCATGGC";
        SequenceTargetingReagent results = markerRepository.getSequenceTargetingReagentBySequence(Marker.Type.MRPHLNO, sequenceForMO1adam8a);
        assertThat("Should find MO with sequence" + sequenceForMO1adam8a,
                results, not(nullValue()));
    }

    @Test
    public void shouldNotFindCrisprWithMorpholinoSequence() {
        String sequenceForMO1adam8a = "TAAATAGTCCAGTGTATCGCATGGC";
        SequenceTargetingReagent results = markerRepository.getSequenceTargetingReagentBySequence(Marker.Type.CRISPR, sequenceForMO1adam8a);
        assertThat("Should not find CRISPR with sequence" + sequenceForMO1adam8a,
                results, is(nullValue()));
    }

    @Test
    public void shouldNotFindTalenWithCrisprSequence() {
        String sequenceForCRISPR3th = "GGCGGCGGAGGCTGCAGGAC";
        SequenceTargetingReagent results = markerRepository.getSequenceTargetingReagentBySequence(Marker.Type.TALEN, sequenceForCRISPR3th);
        assertThat("Should not find TALEN with sequence" + sequenceForCRISPR3th,
                results, is(nullValue()));
    }

    @Test
    public void shouldFindTalenWithOneSequence() {
        String sequence1ForTALEN2ptpmt1 = "TGGTCCAAAATGAAAAAG";
        SequenceTargetingReagent results = markerRepository.getSequenceTargetingReagentBySequence(Marker.Type.TALEN, sequence1ForTALEN2ptpmt1);
        assertThat("Should find TALEN with sequence1 " + sequence1ForTALEN2ptpmt1,
                results, not(nullValue()));
    }

    @Test
    public void shouldFindTalenWithTwoSequences() {
        String sequence1ForTALEN2ptpmt1 = "TGGTCCAAAATGAAAAAG";
        String sequence2ForTALEN2ptpmt1 = "TCATATTCTTCATTCATG";
        SequenceTargetingReagent results = markerRepository.getSequenceTargetingReagentBySequence(
                Marker.Type.TALEN, sequence1ForTALEN2ptpmt1, sequence2ForTALEN2ptpmt1);
        assertThat("Should find TALEN with sequence1 = " + sequence1ForTALEN2ptpmt1 + " and sequence2 = " + sequence2ForTALEN2ptpmt1,
                results, not(nullValue()));
    }

    @Test
    public void shouldFindTalenWithTwoSequencesFlipped() {
        String sequence1ForTALEN2ptpmt1 = "TGGTCCAAAATGAAAAAG";
        String sequence2ForTALEN2ptpmt1 = "TCATATTCTTCATTCATG";
        SequenceTargetingReagent results = markerRepository.getSequenceTargetingReagentBySequence(
                Marker.Type.TALEN, sequence2ForTALEN2ptpmt1, sequence1ForTALEN2ptpmt1);
        assertThat("Should find TALEN with sequence1 = " + sequence2ForTALEN2ptpmt1 + " and sequence2 = " + sequence1ForTALEN2ptpmt1,
                results, not(nullValue()));
    }

    @Test
    public void getAllEngineeredRegions() {
        List<Marker> engineeredRegions = markerRepository.getAllEngineeredRegions();
        assertNotNull(engineeredRegions);
        assertThat(engineeredRegions.size(), greaterThan(10));
    }

    @Test
    public void getDifferentMarkerTypesByID() {
        String snpID = "ZDB-SNP-060626-100";
        SNP snp = markerRepository.getSNPByID(snpID);
        assertNotNull(snp);

        String strID = "ZDB-TALEN-131030-14";
        SequenceTargetingReagent str = markerRepository.getSequenceTargetingReagent(strID);
        assertNotNull(str);

        Marker marker = markerRepository.getMarkerByID(strID);
        assertTrue(marker instanceof SequenceTargetingReagent);
        assertNotNull(marker);
    }


    @Test
    public void getMarkersContainedIn() {
        String id = "ZDB-PAC-030616-4";
        Marker marker = markerRepository.getMarkerByID(id);
        assertNotNull(marker);
        List<Marker> list = markerRepository.getRelatedMarkersForTypes(marker, MarkerRelationship.Type.CLONE_CONTAINS_GENE);
        assertNotNull(list);

    }


    @Test
    public void getRelatedMarkerEFG() {
        String efgID = "ZDB-EFG-070117-1";
        Marker efg = markerRepository.getMarkerByID(efgID);
        assertNotNull(efg);
        Set<MarkerRelationship.Type> types = new HashSet<>();
        types.add(MarkerRelationship.Type.PROMOTER_OF);
        types.add(MarkerRelationship.Type.CODING_SEQUENCE_OF);
        types.add(MarkerRelationship.Type.CONTAINS_REGION);

        PaginationResult<Marker> relatedMarkerResult = markerRepository.getRelatedMarker(efg, types, new PaginationBean());
        assertNotNull(relatedMarkerResult);
    }

    @Test
    public void getOmimPhenotype() {
        String usherGeneID = "ZDB-GENE-040822-17";
        Marker usherGene = markerRepository.getMarkerByID(usherGeneID);
        assertNotNull(usherGene);
        List<OmimPhenotype> phenotype = markerRepository.getOmimPhenotype(usherGene);
        assertNotNull(phenotype);
    }

    @Test
    @Ignore
    // takes about half a minute to execute, so put to ignore
    public void getAllGenedom() {
        List<Marker> allGeneList = markerRepository.getMarkerByGroup(Marker.TypeGroup.GENEDOM, 0);
        assertNotNull(allGeneList);
    }

    @Test
    public void getZFinSoTermMapping() {
        Map<String, GenericTerm> zfinEntityMapping = markerRepository.getSoTermMapping();
        assertNotNull(zfinEntityMapping);
    }

    @Test
    public void setReplacedData() {
        Marker marker = markerRepository.getGeneByID("ZDB-GENE-990415-8");
        Set<SecondaryMarker> secondaryMarkers = marker.getSecondaryMarkerSet();
        assertNotNull(secondaryMarkers);
    }

    @Test
    public void getConstructComponent() {
        List<ConstructComponent> list = markerRepository.getConstructComponent("ZDB-ETCONSTRCT-070409-1");
        assertNotNull(list);
    }

    @Test
    public void getMarkerTypeByDisplayName() {
        MarkerType type = markerRepository.getMarkerTypeByDisplayName("TF Binding Site");
        assertNotNull(type);
        assertThat("", type.getName(), equalTo("TFBS"));
    }

    @Test
    public void getGeneDescByMkr() {
        Marker pax2a = markerRepository.getGeneByID("ZDB-GENE-990415-8");
        AllianceGeneDesc description = markerRepository.getGeneDescByMkr(pax2a);
        assertNotNull(description);
    }

    @Test
    public void getDbsnps() {
        String snps = markerRepository.getDbsnps("ZDB-EST-010427-276");
        assertNotNull(snps);
        assertTrue(snps.contains("rs3727477"));
        assertTrue(snps.contains("rs3727478"));
    }

    @Test
    public void getMarkerTypesforRelationship() {
        List<String> snps = markerRepository.getMarkerTypesforRelationship("gene encodes small segment");
        assertNotNull(snps);
        assertThat("", snps.size() == 7);
    }

    @Test
    public void getGenesforTranscript() {
        Marker pax2a = markerRepository.getMarkerByID("ZDB-TSCRIPT-090929-23374");
        List<Marker> transcripts = markerRepository.getGenesforTranscript(pax2a);
        assertNotNull(transcripts);
        assertThat("", transcripts.size() > 18);
    }

    @Test
    public void getSupplierNamesForString() {
        List<SupplierLookupEntry> lookupEntries = markerRepository.getSupplierNamesForString("Ac");
        assertNotNull(lookupEntries);
        assertThat("", lookupEntries.size() > 50);
    }

    @Test
    public void getSignafishLinkCount() {
        ReferenceDatabase signafishDb = RepositoryFactory.getSequenceRepository().getReferenceDatabase(
                ForeignDB.AvailableName.SIGNAFISH,
                ForeignDBDataType.DataType.OTHER,
                ForeignDBDataType.SuperType.SUMMARY_PAGE,
                Species.Type.ZEBRAFISH);

        Long number = markerRepository.getSignafishLinkCount(signafishDb);
        assertNotNull(number);
        assertTrue(number > 150);
    }

    @Test
    public void getWeakReferenceMarker() {
        List<MarkerRelationshipPresentation> markerRelationshipPresentationList = markerRepository.getWeakReferenceMarker("ZDB-GENE-010606-1"
                , MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                , MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
        );
        markerRelationshipPresentationList.sort(new MarkerRelationshipSupplierComparator());
        assertEquals(3, markerRelationshipPresentationList.size());
        assertEquals("BAC", markerRelationshipPresentationList.get(0).getMarkerType());
        assertEquals("BAC", markerRelationshipPresentationList.get(1).getMarkerType());
        assertEquals("Fosmid", markerRelationshipPresentationList.get(2).getMarkerType());
    }
}
