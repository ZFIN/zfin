package org.zfin.marker.repository;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zfin.TestConfiguration;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.*;
import org.zfin.marker.presentation.HighQualityProbe;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.orthology.Species;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;



public class MarkerRepositoryTest {

    private Logger logger = Logger.getLogger(MarkerRepositoryTest.class) ;
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

    static {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if (sessionFactory == null) {
            new HibernateSessionCreator(false);
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();
        TestConfiguration.setAuthenticatedUser();
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }




    @Test
    public void testMarkerLoad() {
        Session session = HibernateUtil.currentSession();
        Criteria criteria = session.createCriteria(Marker.class);
        criteria.setMaxResults(1);
        Marker marker = (Marker) criteria.uniqueResult();
        assertNotNull("Contains at least one marker", marker);
    }

    @Test
    public void testMarkerTypeAndGroup() {
        Marker pax2a = markerRepository.getMarkerByAbbreviation("pax2a");
        assertTrue("pax2a has type GENE", pax2a.getMarkerType().getType() == Marker.Type.GENE);
        assertFalse("pax2a doesn't have type BAC", pax2a.getMarkerType().getType() == Marker.Type.BAC);
        assertTrue("pax2a is in the type group GENEDOM", pax2a.isInTypeGroup(Marker.TypeGroup.GENEDOM));
        assertFalse("pax2a is not in the type group BAC", pax2a.isInTypeGroup(Marker.TypeGroup.BAC));
    }


    @Test
    public void testMarkersByAbbreviation() {
        List<Marker> paxs = markerRepository.getMarkersByAbbreviation("pax");
        assertNotNull("pax should not be null", paxs);
        assertTrue("pax should have multiple markers", paxs.size() > 1);
    }

    /**
     * Test that a new Marker record takes the Marker type in the sequence
     * that is given by the Marker
     */

    @Test
    public void testZdbSequenceGenerator() {
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            MarkerType type = mr.getMarkerTypeByName(Marker.Type.BAC.toString());
            Marker marker = new Marker();
            marker.setMarkerType(type);
            marker.setName("test marker");
            marker.setAbbreviation("test Abbreviation");
            marker.setAbbreviationOrder("test Abbreviation");
            marker.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
            session.save(marker);

            String zdbID = marker.getZdbID();
            assertTrue("non-null ZDB ID", zdbID != null);
            assertTrue("ID contains BAC", zdbID.indexOf(Marker.Type.BAC.toString()) > -1);
            session.getTransaction().rollback();
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            e.printStackTrace();
            fail("failed creating a marker");
        }
    }

    @Test
    public void testGetLG() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the exisitng genes which might be merged
        try {
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker marker1 = mr.getMarkerByID("ZDB-EST-000426-1181");
            assertTrue("marker lg list contains all self panel mappings", mr.getLG(marker1).contains("13") && mr.getLG(marker1).contains("23"));
            Marker marker2 = mr.getMarkerByID("ZDB-GENE-990415-72");
            assertTrue("gene lg list contains its est's panel mapping", mr.getLG(marker2).contains("23"));
            assertFalse("gene lg list contains no bogus mapping", mr.getLG(marker2).contains("1"));
            Marker marker3 = mr.getMarkerByID("ZDB-GENE-060526-178");
            assertTrue("gene lg list contains clone's panel mapping", mr.getLG(marker3).contains("13"));

            Marker marker4 = mr.getMarkerByID("ZDB-RAPD-980526-288");
            assertTrue("marker lg list contains self linkage group mapping", mr.getLG(marker4).contains("12"));
            Marker marker5 = mr.getMarkerByID("ZDB-BAC-030616-45");
            assertTrue("marker lg list contains linkage mapping of contained marker/segment", mr.getLG(marker5).contains("9"));
            Marker marker6 = mr.getMarkerByID("ZDB-GENE-030616-611");
            assertTrue("gene lg list contains clone's linkage mapping", mr.getLG(marker6).contains("19"));

            Marker marker7 = mr.getMarkerByID("ZDB-GENE-070117-36");
            assertTrue("gene lg list contains allele's linkage group mapping", mr.getLG(marker7).contains("23"));

            Marker marker8 = mr.getMarkerByID("ZDB-GENE-070117-2287");
            assertTrue("marker lg list contains allele's panel mapping", mr.getLG(marker8).contains("7"));

        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void runRegenNamesMarkerProcedure() {
        boolean success = true;
        Session session = HibernateUtil.currentSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Marker marker = markerRepository.getMarkerByAbbreviation("pax2a");
            markerRepository.runMarkerNameFastSearchUpdate(marker);
        } catch (Exception e) {
            success = false;
        } finally {
            if (tx != null)
                tx.rollback();
        }
        assertTrue("Successful execution of stored procedure", success);
    }

    @Test
    public void testRenameMarker() {

        Session session = HibernateUtil.currentSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Marker marker = insertTestMarker();
            Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");
            marker.setName("test1 name");
            marker.setAbbreviation("test1");
            markerRepository.renameMarker(marker, publication, MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES);
            MarkerHistory mhist = markerRepository.getLastMarkerHistory(marker, MarkerHistory.Event.REASSIGNED);
            assertTrue(mhist.getReason().equals(MarkerHistory.Reason.RENAMED_TO_CONFORM_WITH_ZEBRAFISH_GUIDELINES));
            assertNotNull(mhist.getMarkerAlias());
            assertNotNull(infrastructureRepository.getRecordAttribution(
                    mhist.getMarkerAlias().getZdbID(),
                    publication.getZdbID(), null)
            );
        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            tx.rollback();
        }
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
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Clone clone = mr.getCloneById("ZDB-CDNA-040425-3060");
            Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(clone);
            assertEquals("Only one gene found", 1, genes.size());
            assertEquals("Found gene", "ZDB-GENE-040426-2113", genes.iterator().next().getZdbID());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }

    @Test
    public void testGetSpecificMarkerRelationship() {
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Clone clone = mr.getCloneById("ZDB-CDNA-040425-3060");
            Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(clone);
            assertEquals("Only one gene found", 1, genes.size());
            MarkerRelationship mrel = mr.getMarkerRelationship(genes.iterator().next(), clone, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
            assertEquals("Found marker relationship", "ZDB-MREL-040426-3790", mrel.getZdbID());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }





    @Test
    public void testRemoveRedundantDBLinks() {
        Session session = HibernateUtil.currentSession();

        try {

            MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
            SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();
            ProfileRepository personRepository = RepositoryFactory.getProfileRepository();

            session.beginTransaction();

            ReferenceDatabase refDb = sequenceRepository.getReferenceDatabase(
                    ForeignDB.AvailableName.GENBANK,
                    ForeignDBDataType.DataType.RNA,
                    ForeignDBDataType.SuperType.SEQUENCE,
                    Species.ZEBRAFISH);


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
            Set<Accession> accessions = new HashSet<Accession>();
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
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            session.getTransaction().rollback();
        }

    }



    //ZDB-MREL-021003-11
    @Test
    public void retrieveLinkageGroupFromClone() {
        // when the gene has method of creating/adding linkage group information and
        // adding relationship, it would be better to create the test cases rather
        // than using the exisitng genes which might be merged
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker clone = mr.getMarkerByID("ZDB-CDNA-040425-118");
            Set<LinkageGroup> groups = MarkerService.getLinkageGroups(clone);
            assertTrue(groups != null);
            assertEquals("2 linkage groups found", 1, groups.size());
            LinkageGroup group = groups.iterator().next();
            assertEquals("First LG", "1", group.getName());

        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }

    @Test
    public void retrieveLinkageGroupFromGene() {
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker gene = mr.getMarkerByID("ZDB-GENE-990415-72");
            Set<LinkageGroup> groups = MarkerService.getLinkageGroups(gene);
            assertTrue(groups != null);
            assertTrue(groups.size() > 1);
            //            assertEquals("linkage groups found", 3, groups.size());
            //LinkageGroup group = groups.get(0);
            //assertEquals("First LG", "13", group.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }

    @Test
    public void retrieveLinkageGroupFromGeneFgf14() {
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            Marker gene = mr.getMarkerByID("ZDB-GENE-060506-1");
            Set<LinkageGroup> groups = MarkerService.getLinkageGroups(gene);
            assertTrue(groups != null);
            assertEquals("1 linkage groups found", 1, groups.size());
            LinkageGroup group = groups.iterator().next();
            assertEquals("First LG", "9", group.getName());
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }

    @Test
    public void genedomEfgMarkers() {

        Marker marker = markerRepository.getMarkerByAbbreviation("fgf8a");
        List<Marker> markers = markerRepository.getMarkersByAbbreviationAndGroup("gfp", Marker.TypeGroup.GENEDOM_AND_EFG);
        assertTrue(markers != null);
    }


    @Test
    public void testClone(){
        Transaction tx = null;
        Session session = HibernateUtil.currentSession() ;
        try {
            tx = session.beginTransaction();
            Marker marker1 = RepositoryFactory.getMarkerRepository().getMarkerByID("ZDB-CDNA-080114-111") ;
            assertTrue("Marker is a clone", marker1 instanceof Clone);
            Clone clone = (Clone) marker1 ;
//            logger.info(clone.toString());
            Integer rating = 3 ;
            clone.setRating(rating);
            // NOTE: must be CDNA or EST to set as a non-null problem type
            clone.setProblem(Clone.ProblemType.CHIMERIC);
            session.update(clone);
            session.flush();
            Clone clone2 = (Clone) session.createQuery("from Clone c where c.zdbID = 'ZDB-CDNA-080114-111'").uniqueResult() ;
            assertEquals(clone2.getRating(),rating);
            assertEquals(clone2.getProblem(),Clone.ProblemType.CHIMERIC);
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            tx.rollback();
        }
    }



    @Test
    public void getGeneClone() {
        String name = "cfhr2";
        Marker clone = markerRepository.getMarkerByAbbreviation(name);
        assertTrue(clone != null);
/*
        assertTrue(clone instanceof Clone);
        Clone cl = (Clone) clone;
*/

    }

    @Test
    public void getESTClone() {
        String name = "eu815";
        Marker clone = markerRepository.getMarkerByAbbreviation(name);
        assertTrue(clone != null);
/*
        assertTrue(clone instanceof Clone);
        Clone cl = (Clone) clone;
*/

    }

    @Test
    public void getMarkerAlias() {
        // acerebellar, alias for fgf8a
        String zdbID = "ZDB-DALIAS-070117-777";
        MarkerAlias alias = markerRepository.getMarkerAlias(zdbID);
        assertTrue(alias != null);
    }

    @Test
    public void thisseProbesForAoTerm() {
        String aoTermName = "pancreas";
        Term term = new GenericTerm();
        term.setID("ZDB-TERM-100331-130");
        term.setTermName(aoTermName);

        Session session = HibernateUtil.currentSession();
        String hql = "select distinct stat.probe " +
                "     from HighQualityProbeAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           and stat.subterm = :aoterm";
        Query query = session.createQuery(hql);
        query.setParameter("aoterm", term);

        List<AntibodyAOStatistics> list = query.list();
        assertTrue(list != null);
        assertTrue(list.size() > 0);

        hql = " " +
                "     from AntibodyAOStatistics stat " +
                "     where stat.superterm = :aoterm " +
                "           and stat.subterm = :aoterm";
        query = session.createQuery(hql);
        query.setParameter("aoterm", term);
        List<AntibodyAOStatistics> listStat = query.list();
        assertTrue(list != null);
        assertTrue(list.size() > 0);

    }

    @Test
    public void ProbesStatistics() {
        String aoTermName = "brain";
        Term term = new GenericTerm();
        term.setID("ZDB-TERM-100331-8");
        term.setTermName(aoTermName);

        PaginationBean pagination = new PaginationBean();
        pagination.setMaxDisplayRecords(5);
        pagination.setFirstPageRecord(0);
        // without substructures
        PaginationResult<HighQualityProbe> result = markerRepository.getHighQualityProbeStatistics(term, pagination, false);
        assertTrue(result != null);
        assertTrue(result.getTotalCount() > 0);

        // including substructures
        result = markerRepository.getHighQualityProbeStatistics(term, pagination, true);
        assertTrue(result != null);
        assertTrue(result.getTotalCount() > 0);

    }

    @Test
    public void hibernateQueryIterate() {
        String aoTermName = "brain";
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem aoTerm = anatomyRep.getAnatomyItem(aoTermName);
        String name = aoTerm.getStart().getName();
        String hql = " select distinct(stat.fstat_feat_zdb_id), probe.mrkr_abbrev, gene.mrkr_zdb_id," +
                "                       gene.mrkr_abbrev,gene.mrkr_abbrev_order  " +
                "from feature_stats as stat, marker as gene, marker as probe " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = gene.mrkr_zdb_id " +
                "           and fstat_feat_zdb_id = probe.mrkr_zdb_id " +
                "           and fstat_type = :type" +
                "     order by gene.mrkr_abbrev_order ";
        SQLQuery query = session.createSQLQuery(hql);
        query.setString("aoterm", "ZDB-ANAT-010921-587");
        query.setString("type", "High-Quality-Probe");
        query.setFirstResult(0);
        query.setMaxResults(5);
        ScrollableResults results = query.scroll();
        List<HighQualityProbe> probes = new ArrayList<HighQualityProbe>();
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
        int num = results.getRowNumber();

        hql = " select stat.* from feature_stats as stat, marker " +
                "     where fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_superterm_zdb_id = :aoterm " +
                "           and fstat_gene_zdb_id = mrkr_zdb_id " +
                "           and fstat_type = :type" +
                "     order by mrkr_abbrev_order ";

        query = session.createSQLQuery(hql);
        query.setString("aoterm", "ZDB-ANAT-010921-587");
        query.setString("type", "High-Quality-Probe");
        //query.addScalar("aoterm", Hibernate.STRING);
        results = query.scroll();

        results.last();
        num = results.getRowNumber();
        assertTrue(true);
        session.getTransaction().rollback();
    }

    @Test
    public void lazyLoadAnatomy() {
        String aoTermName = "brain";
        Session session = HibernateUtil.currentSession();
        AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem aoTerm = anatomyRep.getAnatomyItem(aoTermName);
        aoTerm.getZdbID();
        aoTerm.getName();
        DevelopmentStage start = aoTerm.getStart();
        start.getName();
    }

    @Test
    public void createNewOrthologyNote() {

        String geneName = "fgf8a";
        MarkerRepository markerRep = RepositoryFactory.getMarkerRepository();
        Marker gene = markerRep.getMarkerByAbbreviation(geneName);
        assertTrue(gene != null);


        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = RepositoryFactory.getMarkerRepository();
            mr.createOrUpdateOrthologyExternalNote(gene, "This is a note");
            tx.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        finally {
            if (tx != null)
                try {
                    tx.rollback();
                } catch (HibernateException e) {
                    
                }
        }
    }

    @Test
    public void orthologyNote() {
        String geneName = "fgf8a";
        MarkerRepository markerRep = RepositoryFactory.getMarkerRepository();
        Marker gene = markerRep.getMarkerByAbbreviation(geneName);
        assertTrue(gene != null);

        Set<OrthologyNote> notes = gene.getOrthologyNotes();
        assertTrue(notes != null);
    }

    @Test
    public void createDataAlias(){
        String pubID = "ZDB-PUB-020723-5";
        String antibodyID = "ZDB-ATB-081002-16";
        HibernateUtil.createTransaction();
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(antibodyID);
        Publication publication = publicationRepository.getPublication(pubID);
        markerRepository.addMarkerAlias(antibody, "Bruno", publication);
        HibernateUtil.rollbackTransaction();
    }
}
