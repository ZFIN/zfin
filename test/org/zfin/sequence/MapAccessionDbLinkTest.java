package org.zfin.sequence;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.sequence.reno.Run;
import org.zfin.sequence.reno.Candidate;
import org.zfin.sequence.reno.RunCandidate;
import org.zfin.sequence.reno.RedundancyRun;
import org.zfin.sequence.blast.Query;
import org.zfin.sequence.blast.Hit;
import org.zfin.framework.HibernateUtil;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.TestConfiguration;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.orthology.Species;
import org.zfin.orthology.Orthologue;
import org.zfin.orthology.OrthoEvidence;
import org.zfin.repository.RepositoryFactory;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;
import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * This class tests setting AccessionLink
 */
public class MapAccessionDbLinkTest {
    private static Logger logger = Logger.getLogger(MapAccessionDbLinkTest.class);
    private static MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository();
    private static ProfileRepository personRepository = RepositoryFactory.getProfileRepository();
    private static SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository();

    ReferenceDatabase refSeqReferenceDatabase ;

    private static String  ACCESSION_NUM1 = "AC:TEST1" ; 
    private static String  ACCESSION_NUM2 = "AC:TEST2" ; 
    private static String  ACCESSION_NUM3 = "AC:TEST3" ; 
    private static String  ACCESSION_NUM4 = "AC:TEST4" ; 
    private static String  ACCESSION_NUM5 = "AC:TEST5" ; 

    private static String  CDNA_NAME = "MGC:test" ; 
    private static String  GENE_NAME = "renogene" ;
    private static String  TEST_DEFLINE = "defline jam" ;

    static{
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

        if(sessionFactory == null){
            new HibernateSessionCreator( TestConfiguration.getHibernateConfiguration() ) ;
        }
    }

    @Before
    public void setUp() {
        TestConfiguration.configure();

    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }



    /**
     * Makes the new candidate, run, accessions, run_candidate, and dblinks.
     * @note Dblinks have to be saved BEFORE their associated Accession or else
     *        a trigger will reassign the Dblink referenceDatabase to GenBank
     *        and assign the length. 
     * @return String runcandidate zdb_id
     */
    private String insertMarkerDBLinkRunCandidate() {
        Session session = HibernateUtil.currentSession();

        // create reference markers
        Marker gene = new Marker();
        gene.setAbbreviation(GENE_NAME);
        gene.setName(GENE_NAME);
        //should this be an enum?
        gene.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene);

        Marker cDNA = new Marker();
        cDNA.setAbbreviation(CDNA_NAME);
        cDNA.setName(CDNA_NAME);
        //should this be an enum?
        cDNA.setMarkerType(markerRepository.getMarkerTypeByName("CDNA"));
        cDNA.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(cDNA);

        // get reference DBs
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");

        // get references DBs
        ReferenceDatabase genBankRefDB =sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK.toString(),
                ReferenceDatabase.Type.GENOMIC,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);

        refSeqReferenceDatabase =sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.REFSEQ.toString(),
                ReferenceDatabase.Type.RNA,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);


        // ADD ACCESSIONS and associated DBLINKS
          
          
        // should find the cdna marker for accession1
        Accession accession1 = new Accession();
        accession1.setNumber(ACCESSION_NUM1);
        accession1.setDefline(TEST_DEFLINE);
        accession1.setLength(12);
        accession1.setReferenceDatabase(genBankRefDB);

        MarkerDBLink dblink1 = new MarkerDBLink() ;
        dblink1.setAccessionNumber(ACCESSION_NUM1);
        dblink1.setReferenceDatabase(genBankRefDB);
        dblink1.setMarker( cDNA) ;  
        session.save(dblink1);
        session.save(accession1);

        // should not find any associated dblink for accesion 2 because not the same reference database
        Accession accession2 = new Accession();
        accession2.setNumber(ACCESSION_NUM2);
        accession2.setDefline(TEST_DEFLINE);
        accession2.setLength(17);
        accession2.setReferenceDatabase(genBankRefDB);

        MarkerDBLink dblink2 = new MarkerDBLink() ;
        dblink2.setAccessionNumber(ACCESSION_NUM2);
        dblink2.setReferenceDatabase(refSeqReferenceDatabase);
        dblink2.setMarker( gene ) ;  
        session.save(dblink2);
        session.save(accession2);

        // should find 2 associated markers to accession3 
        Accession accession3 = new Accession();
        accession3.setNumber(ACCESSION_NUM3);
        accession3.setDefline(TEST_DEFLINE);
        accession3.setLength(5);
        accession3.setReferenceDatabase(genBankRefDB);

        MarkerDBLink dblink3a = new MarkerDBLink() ;
        dblink3a.setAccessionNumber(ACCESSION_NUM3);
        dblink3a.setReferenceDatabase(genBankRefDB);
        dblink3a.setMarker( cDNA ) ;  

        MarkerDBLink dblink3b = new MarkerDBLink() ;
        dblink3b.setAccessionNumber(ACCESSION_NUM3);
        dblink3b.setReferenceDatabase(genBankRefDB);
        dblink3b.setMarker( gene) ;  

        session.save(dblink3a);
        session.save(dblink3b);
        session.save(accession3);

        // should 1 associated marker, gene based on refseq
        Accession accession4 = new Accession();
        accession4.setNumber(ACCESSION_NUM4);
        accession4.setDefline(TEST_DEFLINE);
        accession4.setLength(13);
        accession4.setReferenceDatabase(refSeqReferenceDatabase);

        MarkerDBLink dblink4 = new MarkerDBLink() ;
        dblink4.setAccessionNumber(ACCESSION_NUM4);
        dblink4.setReferenceDatabase(refSeqReferenceDatabase);
        dblink4.setMarker( gene) ;  

        session.save(dblink4);
        session.save(accession4);

        // an associated dblink without a marker, 
        // so should not be found when discriminator added
        Accession accession5 = new Accession();
        accession5.setNumber(ACCESSION_NUM5);
        accession5.setDefline(TEST_DEFLINE);
        accession5.setLength(97);
        accession5.setReferenceDatabase(genBankRefDB);

        MarkerDBLink dblink5 = new MarkerDBLink() ;
        dblink5.setAccessionNumber(ACCESSION_NUM5);
        dblink5.setReferenceDatabase(refSeqReferenceDatabase);
        dblink5.setMarker( gene) ;  

        session.save(dblink5);
        session.save(accession5);


        // create Run
        RedundancyRun run = new RedundancyRun();
        run.setRelationPublication(publication);
        run.setNomenclaturePublication(publication);
        run.setName("TestRedunRun");
        run.setProgram("BLASTN");
        run.setBlastDatabase("zfin_cdna");
        Date date = new Date();
        logger.debug("date: "+date);
        run.setDate(date);
//        run.setType(Run.Type.REDUNDANCY);
        session.save(run);

        // create Candidate
        Candidate candidate = new Candidate();
        candidate.setRunCount(1);
        candidate.setLastFinishedDate(new Date());
        //candidate.setIdentifiedMarker(cDNA);
        candidate.setSuggestedName("novelGene");
        //should this be referencing an enum in markerType class or marker class?
        candidate.setMarkerType(Marker.Type.GENE.toString());

        //we should probably create a fake marker that can go away.
//        candidate.setIdentifiedMarker(markerRepository.getMarkerByID("ZDB-CDNA-040425-323"));
        session.save(candidate);

        // create RunCandidate
        RunCandidate runCandidate = new RunCandidate();
        runCandidate.setRun(run);
        runCandidate.setDone(false);
        runCandidate.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-1"));
        runCandidate.setCandidate(candidate);
        session.save(runCandidate);

        // create Query
        Query query = new Query();
        query.setRunCandidate(runCandidate);
        query.setAccession(accession1);
        runCandidate.getCandidateQueries().add(query);
        session.save(query);

        // create 5 Hits
        Hit hit1 = new Hit();
        hit1.setHitNumber(1);
        hit1.setQuery(query);
        hit1.setTargetAccession(accession1);
        query.getBlastHits().add(hit1);

        //when we have methods to create a gene, then create one instead of using exiting
        //that could be merged/deleted; or maybe we should modify the getMarkerByAbbreviation method
        //to check for aliases...

        hit1.setExpectValue(0.00);
        hit1.setScore(999);
        hit1.setPositivesNumerator(1);
        hit1.setPositivesDenominator(1);
        session.save(hit1);

        Hit hit2 = new Hit();
        hit2.setHitNumber(2);
        hit2.setQuery(query);
        hit2.setTargetAccession(accession2);
        query.getBlastHits().add(hit2);
        hit2.setExpectValue(1.3e-56);
        hit2.setScore(800);
        hit2.setPositivesNumerator(2);
        hit2.setPositivesDenominator(4);
        session.save(hit2);

        Hit hit3 = new Hit();
        hit3.setQuery(query);
        hit3.setHitNumber(3);
        hit3.setTargetAccession(accession3);
        hit3.setExpectValue(1.3e-26);
        hit3.setScore(500);
        query.getBlastHits().add(hit3);
        hit3.setPositivesNumerator(3);
        hit3.setPositivesDenominator(6);
        session.save(hit3);

        Hit hit4 = new Hit();
        hit4.setQuery(query);
        hit4.setHitNumber(4);
        hit4.setTargetAccession(accession4);
        hit4.setExpectValue(1.0e-16);
        hit4.setScore(300);
        query.getBlastHits().add(hit4);
        hit4.setPositivesNumerator(4);
        hit4.setPositivesDenominator(8);
        session.save(hit4);

        Hit hit5 = new Hit();
        hit5.setQuery(query);
        hit5.setHitNumber(5);
        hit5.setTargetAccession(accession5);
        hit5.setExpectValue(1.8e-6);
        hit5.setScore(100);
        hit5.setPositivesNumerator(5);
        hit5.setPositivesDenominator(10);
        query.getBlastHits().add(hit5);
        session.save(hit5);

        List<MarkerDBLink> linksEnd = session.createQuery("from MarkerDBLink mdbl where mdbl.accessionNumber like 'AC:TEST%'").list() ;
        logger.info("End number of links: " + linksEnd.size()) ; 
        for(MarkerDBLink aLink : linksEnd){
            logger.info(aLink.getAccessionNumber() +"   "+aLink.getReferenceDatabase().getZdbID() ) ;
        }


        return runCandidate.getZdbID();
    }


    @Test
    public void testAccessionsToMarkerDBLink() {

        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            insertMarkerDBLinkRunCandidate() ;
            // test accession1
            List<Hit> hits1  = session.createQuery("from Hit h where h.targetAccession.number='"+ACCESSION_NUM1+"'").list() ;
            assertEquals("hits1 for "+ACCESSION_NUM1+" is 1",1,hits1.size());


            Accession acc1 = hits1.get(0).getTargetAccession() ;
            Set<DBLink> dbLinks1 = acc1.getDbLinks() ;
            assertEquals("number of dblinks for "+ ACCESSION_NUM1,1,dbLinks1.size()) ;

            assertTrue("link is instance of MarkerDBLink" + ACCESSION_NUM1,dbLinks1.iterator().next() instanceof MarkerDBLink) ;
            MarkerDBLink markerLink1 = (MarkerDBLink) dbLinks1.iterator().next() ;
            assertEquals("marker name for " + ACCESSION_NUM1,CDNA_NAME,(markerLink1.getMarker().getName())) ;
            Set<MarkerDBLink> blastableMarkerDBLinks1 = hits1.get(0).getTargetAccession().getBlastableMarkerDBLinks() ;
            assertEquals("should be 0 because available from GenBank is Genomic anot not blastable: " + ACCESSION_NUM1,0,blastableMarkerDBLinks1.size()) ;

            List<Marker> markers1 = hits1.get(0).getTargetAccession().getMarkers() ;
            assertEquals("number of marker via dblinks "+ ACCESSION_NUM1,1,markers1.size()) ;
            

            // test accession2
            List<Hit> hits2  = session.createQuery("from Hit h where h.targetAccession.number='"+ACCESSION_NUM2+"'").list() ;
            assertEquals("hits2 for "+ACCESSION_NUM2+" is 1",1,hits2.size());

            Hit hit2 = hits2.get(0) ;
            Set<DBLink> dbLinks2 = hit2.getTargetAccession().getDbLinks() ;

            assertEquals("hit number is 2",2,hit2.getHitNumber()) ;
            assertEquals("should be 0 because not in refSeq "+ ACCESSION_NUM2,0,dbLinks2.size()) ;
            Set<MarkerDBLink> blastableMarkerDBLinks2 = hit2.getTargetAccession().getBlastableMarkerDBLinks() ;
            assertEquals("should be 1 blastable markers because we don't care about DB mismatch: "+ ACCESSION_NUM2,1,blastableMarkerDBLinks2.size()) ;

            List<Marker> markers2 = hits2.get(0).getTargetAccession().getMarkers() ;
            assertEquals("number of marker via dblinks should be 1"+ ACCESSION_NUM2,0,markers2.size()) ;

            // test accession3
            List<Hit> hits3  = session.createQuery("from Hit h where h.targetAccession.number='"+ACCESSION_NUM3+"'").list() ;
            Hit hit3 = hits3.get(0) ;
            Set<DBLink> dbLinks3 = hit3.getTargetAccession().getDbLinks() ;

            assertEquals("hit number is 3",3,hit3.getHitNumber()) ;
            assertEquals("should be 2 markers with GenBank for "+ ACCESSION_NUM3,2,dbLinks3.size()) ;
            assertTrue("link is instance of MarkerDBLink" + ACCESSION_NUM3,dbLinks3.iterator().next() instanceof MarkerDBLink) ;
            Set<MarkerDBLink> blastableMarkerDBLinks3 = hit3.getTargetAccession().getBlastableMarkerDBLinks() ;
            assertEquals("should be 2 because is in GenBank "+ ACCESSION_NUM3,0,blastableMarkerDBLinks3.size()) ;

            List<Marker> markers3 = hits3.get(0).getTargetAccession().getMarkers() ;
            assertEquals("number of marker via dblinks "+ ACCESSION_NUM3,2,markers3.size()) ;

            // test accession4
            List<Hit> hits4  = session.createQuery("from Hit h where h.targetAccession.number='"+ACCESSION_NUM4+"'").list() ;
            Hit hit4 = hits4.get(0) ;
            Set<DBLink> dbLinks4 = hit4.getTargetAccession().getDbLinks() ;
            assertEquals("hit number is 4",4,hit4.getHitNumber()) ;
            assertEquals("should be 1 markers for "+ ACCESSION_NUM4,1,dbLinks4.size()) ;
            assertTrue("link is instance of MarkerDBLink" + ACCESSION_NUM4,dbLinks4.iterator().next() instanceof MarkerDBLink) ;
            assertEquals("is refSeq database for "+ ACCESSION_NUM4,refSeqReferenceDatabase.getZdbID(),dbLinks4.iterator().next().getReferenceDatabase().getZdbID()) ;
            Set<MarkerDBLink> blastableMarkerDBLinks4 = hit4.getTargetAccession().getBlastableMarkerDBLinks() ;
            logger.info("hit 4 accession: "+ hit4.getTargetAccession().getID()) ;
            assertEquals("should be 1 blastable marker for "+ ACCESSION_NUM4,1,blastableMarkerDBLinks4.size()) ;

            List<Marker> markers4 = hits4.get(0).getTargetAccession().getMarkers() ;
            assertEquals("number of marker via dblinks "+ ACCESSION_NUM4,1,markers4.size()) ;


            // test accession5
            List<Hit> hits5  = session.createQuery("from Hit h where h.targetAccession.number='"+ACCESSION_NUM5+"'").list() ;
            Hit hit5 = hits5.get(0) ;
            Set<DBLink> dbLinks5 = hit5.getTargetAccession().getDbLinks() ;
            assertEquals("hit number is 5",5,hit5.getHitNumber()) ;
            assertEquals("should be 0 marker DBLink for because of db mismatch"+ ACCESSION_NUM5,0,dbLinks5.size()) ;
            Set<MarkerDBLink> blastableMarkerDBLinks5 = hit5.getTargetAccession().getBlastableMarkerDBLinks() ;
            assertEquals("one available DBLink  "+ ACCESSION_NUM5,1,blastableMarkerDBLinks5.size()) ;

            List<Marker> markers5 = hits5.get(0).getTargetAccession().getMarkers() ;
            assertEquals("number of marker is 1 via dblinks "+ ACCESSION_NUM5,0,markers5.size()) ;

        }
        catch (Exception e) {
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            e.printStackTrace();
            fail(errorString);
        }
        finally {
            // rollback on success or exception
            session.getTransaction().rollback();
//            session.getTransaction().commit();
        }
    }


    // this class is not used as of yet
//    @Test
//    public void testAnatomyItemDBLink(){
//
//    }

    @Test
    public void testOrthologueDBLink(){
        Session session = HibernateUtil.currentSession();
        session.beginTransaction();
        try {
            insertOrthologueDBLinkRunCandidate() ;

            List<Hit> hits1  = session.createQuery("from Hit h where h.targetAccession.number='"+ACCESSION_NUM1+"'").list() ;
            assertEquals("hits1 for "+ACCESSION_NUM1+" is 1",1,hits1.size());
            Accession acc1 = hits1.get(0).getTargetAccession() ;
            Set<DBLink> dbLinks1 = acc1.getDbLinks() ;
            assertEquals("number of orthologues for "+ ACCESSION_NUM1,1,dbLinks1.size()) ;
            assertTrue("link is instance of OrthologueDBLink" + ACCESSION_NUM1,dbLinks1.iterator().next() instanceof OrthologueDBLink) ;
            assertFalse("link is instance of MarkerDBLink" + ACCESSION_NUM1,dbLinks1.iterator().next() instanceof MarkerDBLink) ;

            List<Marker> markers1 = hits1.get(0).getTargetAccession().getMarkers() ;
            assertEquals("orthologues should not be returned as markers "+ ACCESSION_NUM1,0,markers1.size()) ;

        }
        catch(Exception e){
            java.lang.StackTraceElement[] elements = e.getStackTrace();
            String errorString = "";
            for (StackTraceElement element : elements) {
                errorString += element + "\n";
            }
            e.printStackTrace();
            fail(errorString);
        }
        finally{
            session.getTransaction().rollback() ;
        }
    }


    private void insertOrthologueDBLinkRunCandidate(){
        Session session = HibernateUtil.currentSession();
        PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
        Publication publication = publicationRepository.getPublication("ZDB-PUB-070122-15");


        // get references DBs
        ForeignDB genBankForeignDB = sequenceRepository.getForeignDBByName("GenBank");
        ReferenceDatabase genBankRefDB =sequenceRepository.getReferenceDatabaseByAlternateKey(
                genBankForeignDB,
                ReferenceDatabase.Type.GENOMIC,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);

        ForeignDB refSeqForeignDB = sequenceRepository.getForeignDBByName("RefSeq");
        refSeqReferenceDatabase =sequenceRepository.getReferenceDatabaseByAlternateKey(
                refSeqForeignDB,
                ReferenceDatabase.Type.GENOMIC,
                ReferenceDatabase.SuperType.SEQUENCE,
                Species.ZEBRAFISH);

        session.flush();

        // create reference markers
        Marker gene = new Marker();
        gene.setAbbreviation(GENE_NAME);
        gene.setName(GENE_NAME);
        //should this be an enum?
        gene.setMarkerType(markerRepository.getMarkerTypeByName("GENE"));
        gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene);

        Marker cDNA = new Marker();
        cDNA.setAbbreviation(CDNA_NAME);
        cDNA.setName(CDNA_NAME);
        //should this be an enum?
        cDNA.setMarkerType(markerRepository.getMarkerTypeByName("CDNA"));
        cDNA.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(cDNA);

        Orthologue orthology1 = new Orthologue();
        orthology1.setOrganism(Species.HUMAN);
        orthology1.setGene(gene);
        OrthoEvidence evidence1 = new OrthoEvidence();
        evidence1.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidence1.setPublication(publication);
        Set<OrthoEvidence> evidences = new HashSet<OrthoEvidence>();
        evidences.add(evidence1);
        orthology1.setEvidence(evidences);
        session.save(orthology1);

        Orthologue orthology2 = new Orthologue();
        orthology2.setOrganism(Species.MOUSE);
        orthology2.setGene(cDNA);
        OrthoEvidence evidenceThree = new OrthoEvidence();
        evidenceThree.setOrthologueEvidenceCode(OrthoEvidence.Code.AA);
        evidenceThree.setPublication(publication);
        Set<OrthoEvidence> evidences2 = new HashSet<OrthoEvidence>();
        evidences2.add(evidenceThree);
        orthology2.setEvidence(evidences2);
        session.save(orthology2);

        Set<Orthologue> orthologs = new HashSet<Orthologue>();
        orthologs.add(orthology1);
        orthologs.add(orthology2);

        // should find the cdna marker for accession1
        Accession accession1 = new Accession();
        accession1.setNumber(ACCESSION_NUM1);
        accession1.setDefline(TEST_DEFLINE);
        accession1.setLength(12);
        accession1.setReferenceDatabase(genBankRefDB);

        OrthologueDBLink dblink1 = new OrthologueDBLink() ;
        dblink1.setAccessionNumber(ACCESSION_NUM1);
        dblink1.setReferenceDatabase(genBankRefDB);
        dblink1.setOrthologue( orthology1 ) ;

        session.save(dblink1);
        session.save(accession1);

        // should find the cdna marker for accession2
        Accession accession2 = new Accession();
        accession2.setNumber(ACCESSION_NUM2);
        accession2.setDefline(TEST_DEFLINE);
        accession2.setLength(22);
        accession2.setReferenceDatabase(genBankRefDB);

        OrthologueDBLink dblink2 = new OrthologueDBLink() ;
        dblink2.setAccessionNumber(ACCESSION_NUM2);
        dblink2.setReferenceDatabase(genBankRefDB);
        dblink2.setOrthologue( orthology2 ) ;

        session.save(dblink2);
        session.save(accession2);

        // create Run
        RedundancyRun run = new RedundancyRun();
        run.setRelationPublication(publication);
        run.setNomenclaturePublication(publication);
        run.setName("TestRedunRun");
        run.setProgram("BLASTN");
        run.setBlastDatabase("zfin_cdna");
        Date date = new Date();
        logger.debug("date: "+date);
        run.setDate(date);
        session.save(run);

        // create Candidate
        Candidate candidate = new Candidate();
        candidate.setRunCount(1);
        candidate.setLastFinishedDate(new Date());
        //candidate.setIdentifiedMarker(cDNA);
        candidate.setSuggestedName("novelGene");
        //should this be referencing an enum in markerType class or marker class?
        candidate.setMarkerType(Marker.Type.GENE.toString());

        //we should probably create a fake marker that can go away.
        //        candidate.setIdentifiedMarker(markerRepository.getMarkerByID("ZDB-CDNA-040425-323"));
        session.save(candidate);

        // create RunCandidate
        RunCandidate runCandidate = new RunCandidate();
        runCandidate.setRun(run);
        runCandidate.setDone(false);
        runCandidate.setLockPerson(personRepository.getPerson("ZDB-PERS-030520-1"));
        runCandidate.setCandidate(candidate);
        session.save(runCandidate);

        // create Query
        Query query = new Query();
        query.setRunCandidate(runCandidate);
        query.setAccession(accession1);
        runCandidate.getCandidateQueries().add(query);
        session.save(query);

        // create 5 Hits
        Hit hit1 = new Hit();
        hit1.setHitNumber(1);
        hit1.setQuery(query);
        hit1.setTargetAccession(accession1);
        query.getBlastHits().add(hit1);

        //when we have methods to create a gene, then create one instead of using exiting
        //that could be merged/deleted; or maybe we should modify the getMarkerByAbbreviation method
        //to check for aliases...

        hit1.setExpectValue(0.00);
        hit1.setScore(999);
        hit1.setPositivesNumerator(1);
        hit1.setPositivesDenominator(1);
        session.save(hit1);

    }

}
