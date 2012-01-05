package org.zfin.marker.repository;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.junit.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.zfin.AbstractDatabaseTest;
import org.zfin.anatomy.AnatomyItem;
import org.zfin.anatomy.DevelopmentStage;
import org.zfin.anatomy.repository.AnatomyRepository;
import org.zfin.antibody.Antibody;
import org.zfin.antibody.presentation.AntibodyAOStatistics;
import org.zfin.expression.service.ExpressionService;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.presentation.PaginationBean;
import org.zfin.framework.presentation.PaginationResult;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.marker.*;
import org.zfin.marker.presentation.*;
import org.zfin.marker.service.MarkerService;
import org.zfin.mutant.Genotype;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.Term;
import org.zfin.orthology.Species;
import org.zfin.people.MarkerSupplier;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.repository.RepositoryFactory;
import org.zfin.sequence.*;
import org.zfin.sequence.repository.SequenceRepository;

import java.util.*;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static org.zfin.framework.HibernateUtil.currentSession;


@SuppressWarnings({"FeatureEnvy"})
public class MarkerRepositoryTest extends AbstractDatabaseTest {

    private Logger logger = Logger.getLogger(MarkerRepositoryTest.class);
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private static PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    private static InfrastructureRepository infrastructureRepository = RepositoryFactory.getInfrastructureRepository();

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
//        Marker pax2a = markerRepository.getMarkerByAbbreviation("pax2a");
        Marker pax2a = markerRepository.getGeneByID("ZDB-GENE-990415-8");
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
        assertNull(markerRepository.getGeneByID("ZDB-TSCRIPT-090929-6229"));
        assertNotNull(markerRepository.getGeneByID("ZDB-GENE-990415-200"));
    }

    @Test
    public void geneByAbbreviation() {
        assertNull(markerRepository.getGeneByAbbreviation("pax3"));
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
        session.beginTransaction();
        try {
            MarkerRepository mr = markerRepository;
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
            MarkerRepository mr = markerRepository;
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

        } catch (Exception e) {
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
            Marker marker = markerRepository.getGeneByID("ZDB-GENE-990415-8"); // pax2a
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
        } catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            fail(errorString);
        } finally {
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
            MarkerRepository mr = markerRepository;
            Clone clone = mr.getCloneById("ZDB-CDNA-040425-3060");
            Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(clone);
            assertEquals("Only one gene found", 1, genes.size());
            assertEquals("Found gene", "ZDB-GENE-040426-2113", genes.iterator().next().getZdbID());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void testGetSpecificMarkerRelationship() {
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = markerRepository;
            Clone clone = mr.getCloneById("ZDB-CDNA-040425-3060");
            Set<Marker> genes = MarkerService.getRelatedSmallSegmentGenesFromClone(clone);
            assertEquals("Only one gene found", 1, genes.size());
            MarkerRelationship mrel = mr.getMarkerRelationship(genes.iterator().next(), clone, MarkerRelationship.Type.GENE_ENCODES_SMALL_SEGMENT);
            assertEquals("Found marker relationship", "ZDB-MREL-040426-3790", mrel.getZdbID());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tx.rollback();
        }
    }


    @Test
    public void testRemoveRedundantDBLinks() {
        Session session = HibernateUtil.currentSession();

        try {
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


        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
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
            MarkerRepository mr = markerRepository;
            Marker clone = mr.getMarkerByID("ZDB-CDNA-040425-118");
            Set<LinkageGroup> groups = MarkerService.getLinkageGroups(clone);
            assertTrue(groups != null);
            assertEquals("2 linkage groups found", 1, groups.size());
            LinkageGroup group = groups.iterator().next();
            assertEquals("First LG", "1", group.getName());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void retrieveLinkageGroupFromGene() {
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = markerRepository;
            Marker gene = mr.getMarkerByID("ZDB-GENE-990415-72");
            Set<LinkageGroup> groups = MarkerService.getLinkageGroups(gene);
            assertTrue(groups != null);
            assertTrue(groups.size() > 1);
            //            assertEquals("linkage groups found", 3, groups.size());
            //LinkageGroup group = groups.get(0);
            //assertEquals("First LG", "13", group.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tx.rollback();
        }
    }

    @Test
    public void retrieveLinkageGroupFromGeneFgf14() {
        Transaction tx = null;
        try {
            tx = HibernateUtil.currentSession().beginTransaction();
            MarkerRepository mr = markerRepository;
            Marker gene = mr.getMarkerByID("ZDB-GENE-060506-1");
            Set<LinkageGroup> groups = MarkerService.getLinkageGroups(gene);
            assertTrue(groups != null);
            assertEquals("1 linkage groups found", 1, groups.size());
            LinkageGroup group = groups.iterator().next();
            assertEquals("First LG", "9", group.getName());
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tx.rollback();
        }
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

    //    Just used for assessing performance
//    @Test
    public void markerLookupPerformance() {
        List<Marker> markers = markerRepository.getMarkersByAbbreviation("fgf");
        System.out.println(markers.size());
        long startTime = System.currentTimeMillis();
        for (Marker marker : markers) {
            List<Marker> groupMarkerList = markerRepository.getMarkersByAbbreviationAndGroup(marker.getAbbreviation(), Marker.TypeGroup.GENEDOM_AND_EFG);
//            assertNotNull(groupMarkerList);
//            assertTrue(groupMarkerList.size()>0);
        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println(totalTime / 1000f);
    }


    @Test
    public void getMarkersByAbbreviationGroupAndAttribution() {
        // not, that is an lower-case 'L' not the number one.
        String markerAbbreviation = "Tg(kdrl:GRCFP)";
        String pub = "ZDB-PUB-030527-16";
        List<Marker> markers = null;
        markers = markerRepository.getMarkersByAbbreviationGroupAndAttribution("Tg(", Marker.TypeGroup.CONSTRUCT, pub);
        assertTrue(containsMarkerName(markerAbbreviation, markers));
        markers = markerRepository.getMarkersByAbbreviationGroupAndAttribution("tg(", Marker.TypeGroup.CONSTRUCT, pub);
        assertTrue(containsMarkerName(markerAbbreviation, markers));
        markers = markerRepository.getMarkersByAbbreviationGroupAndAttribution("GRCFP", Marker.TypeGroup.CONSTRUCT, pub);
        assertTrue(containsMarkerName(markerAbbreviation, markers));
        markers = markerRepository.getMarkersByAbbreviationGroupAndAttribution("cfp", Marker.TypeGroup.CONSTRUCT, pub);
        assertTrue(containsMarkerName(markerAbbreviation, markers));
        markers = markerRepository.getMarkersByAbbreviationGroupAndAttribution("drl:g", Marker.TypeGroup.CONSTRUCT, pub);
        assertTrue(containsMarkerName(markerAbbreviation, markers));

    }

    private boolean containsMarkerName(String markerName, List<Marker> markers) {
        if (markers == null || markers.size() == 0) {
            return false;
        }
        for (Marker marker : markers) {
            if (marker.getAbbreviation().equals(markerName)) {
                return true;
            }
        }
        return false;
    }


    @Test
    public void testClone() {
        Transaction tx = null;
        Session session = HibernateUtil.currentSession();
        try {
            tx = session.beginTransaction();
            Marker marker1 = markerRepository.getMarkerByID("ZDB-CDNA-080114-111");
            assertTrue("Marker is a clone", marker1 instanceof Clone);
            Clone clone = (Clone) marker1;
//            logger.info(clone.toString());
            Integer rating = 3;
            clone.setRating(rating);
            // NOTE: must be CDNA or EST to set as a non-null problem type
            clone.setProblem(Clone.ProblemType.CHIMERIC);
            session.update(clone);
            session.flush();
            Clone clone2 = (Clone) session.createQuery("from Clone c where c.zdbID = 'ZDB-CDNA-080114-111'").uniqueResult();
            assertEquals(clone2.getRating(), rating);
            assertEquals(clone2.getProblem(), Clone.ProblemType.CHIMERIC);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            tx.rollback();
        }
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
        assertNotNull(alias);
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
        HibernateUtil.createTransaction();
        Session session = HibernateUtil.currentSession();
        AnatomyRepository anatomyRep = RepositoryFactory.getAnatomyRepository();
        AnatomyItem aoTerm = anatomyRep.getAnatomyItem(aoTermName);
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
            HighQualityProbe hqp = new HighQualityProbe(probe, aoTerm.createGenericTerm());
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
        query.setString("aoterm", "ZDB-ANAT-010921-587");
        query.setString("type", "High-Quality-Probe");
        //query.addScalar("aoterm", Hibernate.STRING);
        results = query.scroll();

        results.last();
        assertTrue(true);
        session.getTransaction().rollback();
    }

    @Test
    public void lazyLoadAnatomy() {
        String aoTermName = "brain";
        AnatomyItem aoTerm = RepositoryFactory.getAnatomyRepository().getAnatomyItem(aoTermName);
        aoTerm.getZdbID();
        aoTerm.getTermName();
        DevelopmentStage start = aoTerm.getStart();
        start.getName();
    }

    @Test
    public void createNewOrthologyNote() {

        String geneName = "fgf8a";
        Marker gene = markerRepository.getMarkerByAbbreviation(geneName);
        assertNotNull(gene);

        try {
            HibernateUtil.createTransaction();
            markerRepository.createOrUpdateOrthologyExternalNote(gene, "This is a note");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            HibernateUtil.rollbackTransaction();
        }
    }

    @Test
    public void orthologyNote() {
        String geneName = "fgf8a";
        Marker gene = markerRepository.getMarkerByAbbreviation(geneName);
        assertTrue(gene != null);

        Set<OrthologyNote> notes = gene.getOrthologyNotes();
        assertTrue(notes != null);
    }

    /**
     * Check for an existent marker by abbreviation.
     * Check the the lookup is case insensitive.
     * <p/>
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
        HibernateUtil.createTransaction();
        Antibody antibody = RepositoryFactory.getAntibodyRepository().getAntibodyByID(antibodyID);
        Publication publication = publicationRepository.getPublication(pubID);
        markerRepository.addMarkerAlias(antibody, "Bruno", publication);
        HibernateUtil.rollbackTransaction();
    }

    @Test
    public void retrieveSingleTargetGeneFromMorpholino() {
        // MO1-adam8a has one target gene
        MarkerRepository markerRep = markerRepository;
        Marker morpholino = markerRep.getMarkerByID("ZDB-MRPHLNO-100729-2");
        Set<Marker> targetGenes = markerRepository.getTargetGenesForMorpholino(morpholino);
        assertNotNull(targetGenes);
        assertEquals(1, targetGenes.size());
//        assertEquals("adam8a", targetGenes.iterator().next().getAbbreviation());
        assertEquals("ZDB-GENE-030616-622", targetGenes.iterator().next().getZdbID());

    }

    @Test
    public void retrieveMultipleTargetGenesFromMorpholino() {
        // MO4-rbpja+rbpjb has two target genes
        MarkerRepository markerRep = markerRepository;
        Marker morpholino = markerRep.getMarkerByID("ZDB-MRPHLNO-060626-6");
        Set<Marker> targetGenes = markerRepository.getTargetGenesForMorpholino(morpholino);
        assertNotNull(targetGenes);
        assertEquals(2, targetGenes.size());
        Iterator<Marker> iter = targetGenes.iterator() ;
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

    // this is a destructive test, so don't want to run
//    @Test
    public void deleteMarker() throws Exception {
        DeleteMarkerController deleteMarkerController = new DeleteMarkerController();
        DeleteBean deleteBean = new DeleteBean();
        deleteBean.setZdbIDToDelete("ZDB-ATB-081002-17");
        deleteMarkerController.deleteMarker("ZDB-SNP-060626-88", deleteBean);
    }

    @Test
    public void markerHistoryView() {
        assertTrue(markerRepository.getHasMarkerHistory("ZDB-GENE-990603-12"));
    }

    @Test
    public void getPreviousNamesLight() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-010606-1");
        List<PreviousNameLight> previousNames = markerRepository.getPreviousNamesLight(m);
        assertEquals(3, previousNames.size());
    }

    @Test
    public void getVariantForSnp() {
        assertEquals("A/T", markerRepository.getVariantForSnp("ZDB-SNP-060626-88"));
    }

    @Test
    public void getMarkerSequences() {
        Marker m = markerRepository.getMarkerByID("ZDB-SNP-060626-88");
        assertEquals(1, markerRepository.getMarkerSequences(m).size());
        assertEquals("TATTTC", markerRepository.getMarkerSequences(m).iterator().next().getSequence().substring(0, 6));
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
        assertEquals(2,linkDisplayList.size());
    }

    @Test
    public void getGeneProducts() {
        List<GeneProductsBean> geneProductsBean = markerRepository.getGeneProducts("ZDB-GENE-980526-561");
        assertNotNull(geneProductsBean);
        assertEquals(1, geneProductsBean.size());
        assertEquals("Q90477", geneProductsBean.get(0).getAccession());
        assertTrue(geneProductsBean.get(0).getComment().startsWith("FUNCTION"));
        assertTrue(geneProductsBean.get(0).getComment().endsWith("domain."));

        List<GeneProductsBean> geneProductsBean2 = markerRepository.getGeneProducts("ZDB-GENE-000405-1");
        assertNotNull(geneProductsBean2);
        assertEquals(2, geneProductsBean2.size());

        List<GeneProductsBean> geneProductsBean3 = markerRepository.getGeneProducts("ZDB-GENE-030131-2333");
        assertNotNull(geneProductsBean3);
        assertEquals(0, geneProductsBean3.size());
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
    public void getKnockdownReagentsWithMultiplePubs() {
        Marker m = markerRepository.getGeneByID("ZDB-GENE-000626-1");
        List<MarkerRelationshipPresentation> knockdowns = markerRepository
                .getRelatedMarkerDisplayForTypes(m, false, MarkerRelationship.Type.KNOCKDOWN_REAGENT_TARGETS_GENE);
        assertThat(knockdowns.size(), greaterThan(2));
        assertThat(knockdowns.size(), lessThan(8));

        MarkerRelationshipPresentation mrp = knockdowns.get(0);
        assertEquals("MO1-tnnt2a", mrp.getAbbreviation());
        assertThat(mrp.getNumAttributions(), greaterThan(1));
        assertThat(mrp.getNumAttributions(), lessThan(4));

        String linkWithAttribution = mrp.getLinkWithAttribution();
        logger.debug(linkWithAttribution);

        assertTrue(linkWithAttribution.startsWith("<a href=\"/action/marker/view/ZDB-MRPHLNO-060317-4\">"));
        assertTrue(linkWithAttribution.contains("/webdriver?MIval=aa-showpubs.apg&orgOID=ZDB-MRPHLNO-060317-4&rtype=marker&recattrsrctype=standard&OID=ZDB-MREL-060317-4\">2</a>)"));
    }

    @Test
    public void isFromChimericClone() {
        assertFalse(markerRepository.isFromChimericClone("ZDB-GENE-980526-403"));
        assertFalse(markerRepository.isFromChimericClone("ZDB-GENE-030131-350"));
        assertTrue(markerRepository.isFromChimericClone("ZDB-GENE-031118-133"));

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
        assertThat(zdbIds.size(), lessThan(200));
    }

    @Test
    public void getGeoMarkerCandidates() {
        Map<String, String> candidates = markerRepository.getGeoMarkerCandidates();
        assertNotNull(candidates);
        assertThat(candidates.keySet().size(), greaterThan(30000));
        assertThat(candidates.keySet().size(), lessThan(300000));
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
        assertEquals("Tuebingen", g.getName());
    }

//    @Test
    public void getVegaGeneDBLinksTranscript() {
        Marker m = markerRepository.getMarkerByID("ZDB-GENE-980528-2060");
        List<LinkDisplay> links = markerRepository.getVegaGeneDBLinksTranscript(m,DisplayGroup.GroupName.SUMMARY_PAGE);
        assertNotNull(links);
        assertEquals(1,links.size());
        assertTrue(links.get(0).getAccession().startsWith("OTTDARG0000"));
    }

    @Test
    public void getAllEngineeredRegions(){
        List<Marker> engineeredRegions = markerRepository.getAllEngineeredRegions();
        assertNotNull(engineeredRegions);
        assertThat(engineeredRegions.size(), greaterThan(10));
    }



    @Test
    public void getWeakReferenceMarker(){
        List<MarkerRelationshipPresentation> markerRelationshipPresentationList = markerRepository.getWeakReferenceMarker("ZDB-GENE-010606-1"
                ,MarkerRelationship.Type.CLONE_CONTAINS_TRANSCRIPT
                ,MarkerRelationship.Type.GENE_PRODUCES_TRANSCRIPT
        );
        Collections.sort(markerRelationshipPresentationList, new MarkerRelationshipSupplierComparator());
        assertEquals(3,markerRelationshipPresentationList.size());
        assertEquals("BAC",markerRelationshipPresentationList.get(0).getMarkerType());
        assertEquals("BAC",markerRelationshipPresentationList.get(1).getMarkerType());
        assertEquals("Fosmid",markerRelationshipPresentationList.get(2).getMarkerType());
//        for(MarkerRelationshipPresentation mrp : markerRelationshipPresentationList){
//            System.out.println(mrp.getLinkWithAttributionAndOrderThis());
//        }
    }
}
