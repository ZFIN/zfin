package org.zfin.datatransfer;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.* ;

import org.zfin.marker.Marker;
import org.zfin.marker.repository.MarkerRepository;
import org.zfin.sequence.MarkerDBLink;
import org.zfin.sequence.ReferenceDatabase;
import org.zfin.sequence.ForeignDB;
import org.zfin.sequence.ForeignDBDataType;
import org.zfin.sequence.repository.SequenceRepository;
import org.zfin.framework.HibernateUtil;
import org.zfin.repository.RepositoryFactory;
import org.zfin.people.repository.ProfileRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.zfin.publication.Publication;
import org.zfin.orthology.Species;
import org.zfin.TestConfiguration;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.apache.log4j.Logger;

import java.util.*;

/**
 *  Related to FogBugz 909, 2008, 2009.
 *  Requirements at: https://www.cs.uoregon.edu/~zfinadmn/68f1abb51ad0820a3bb035dfe71007ad/index.cgi?n=Projects.MicroArray
 *   We want to test UpdateMicroArrayMain for the following cases:
    * a new GEO/GenBank Accession appears (tested):
          o an accession refers to a gene marker:
               1. adds a MarkerDBLink to that gene
          o an accession refers to a CDNA or EST. If encoded by a gene:
               1. dblink added to that CDNA/EST
               2. dblink added to gene that encodes this CDNA/EST
    * a GEO/GenBank accession disappears (test dropped from GenBank, test dropped from GEO):
          o the accession refers to a gene marker (dblink removed):
               1. removes the MarkerDBLink that had previously existed
          o the accession refers to a CDNA/EST:
               1. removes the MarkerDBLink associated with that CDNA/EST
               2. if the CDNA/EST had encoded a gene marker, and that gene marker did not have a GEO accession, than its DBLink is dropped
    * a GenBank accession changes markers (test moved in GenBank):
         1. process dropped accessions
         2. process added accessions
 *
 */
public class MicroArrayTest {

    private Logger logger = Logger.getLogger(MicroArrayTest.class) ;

    // get reference DBs
    PublicationRepository publicationRepository = RepositoryFactory.getPublicationRepository();
    MarkerRepository markerRepository = RepositoryFactory.getMarkerRepository() ;
    ProfileRepository personRepository = RepositoryFactory.getProfileRepository() ;
    SequenceRepository sequenceRepository = RepositoryFactory.getSequenceRepository() ;


    private final String ACCESSION_NUM1= "ACCESSION_NUM1" ;  // GENE
    private final String ACCESSION_NUM2= "ACCESSION_NUM2" ;  // CDNA
    private final String ACCESSION_NUM3= "ACCESSION_NUM3" ;  // EST

    private static String  GENE_NAME = "gene_name" ;
    private static String  CDNA_NAME = "cdna_name" ;
    private static String  EST_NAME = "est_name" ;


    private Marker gene ;
    private String geneGEOAccession = ACCESSION_NUM1;
    private MarkerDBLink geneDBLink ;

    private Marker cDNA ;
    private MarkerDBLink cDNADBLink ;
    private String cDNAGEOAccession = ACCESSION_NUM2;

    private Marker est ;
    private MarkerDBLink estDBLink ;
    private String estGEOAccession = ACCESSION_NUM3;


    // other reference variables
    private ReferenceDatabase geoDatabase ;
    private UpdateMicroArrayMain driver ;
    private ReferenceDatabase genBankRefDB ; 



    public MicroArrayTest(){
        try{
            driver = new UpdateMicroArrayMain() ;
            driver.init();  // this creates the session so I don't need to do twice
        }catch(Exception e){
            logger.fatal("failed to init UpdateMicroArrayMain",e);
        }
    }



    @Before
    public void setUp() {
        TestConfiguration.configure();
        initTestData() ; 
    }

    @After
    public void closeSession() {
        HibernateUtil.closeSession();
    }


     /**
      *  Add new MicroArray links to current set of microarray accessions. Here we use the encoded links.
      */
     @Test
     public void addNewLinksOnGene(){ 
        Session session = null ;
        try{
            logger.info("start addNewLinksOnGene");
            session = HibernateUtil.currentSession() ;
            session.beginTransaction() ;

            cleanDBLinks();
            insertGeneMarker() ;

            Set<String> newMicroArrayAccessions= new HashSet<String>() ;
            newMicroArrayAccessions.add( ACCESSION_NUM1 ) ;

            Map<String,Set<MarkerDBLink>> microArrayLinks = sequenceRepository.getMarkerDBLinks(geoDatabase ) ;   // 0 - load microarray


            driver.loadGenBankDBs();
            driver.processNewLinks(newMicroArrayAccessions,  microArrayLinks, geoDatabase);

            Criteria criteria = session.createCriteria(MarkerDBLink.class) ;
            criteria.add(Restrictions.eq("accessionNumber",ACCESSION_NUM1)) ;
            criteria.add(Restrictions.eq("referenceDatabase",geoDatabase)) ;
            
            // should have added a DBLink for GEO for this accession # and marker
            MarkerDBLink link = (MarkerDBLink) criteria.uniqueResult() ;
            assertNotNull("should have a valid link",link) ;
            assertEquals("reference DB should be GEO",ForeignDB.AvailableName.GEO.toString(),link.getReferenceDatabase().getForeignDB().getDbName()) ;
            assertEquals("accession is "+ACCESSION_NUM1,link.getAccessionNumber(),ACCESSION_NUM1) ;
            assertEquals("marker should be named "+gene.getName(),link.getMarker().getName(),gene.getName()) ;
            logger.info("end addNewLinksOnGene");            
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
            if(session!=null){
                // rollback on success or exception
                session.getTransaction().rollback();
            }
        }
     }


     /**  Test removal of links from a gene
      *
      */
     @Test
     public void removeLinksOnGene(){ 
        Session session = null ;
        try{
            session = HibernateUtil.currentSession() ;
            session.beginTransaction() ;

            cleanDBLinks();            
            insertGeneMarker() ; 

            MarkerDBLink geneGEODBLink = new MarkerDBLink() ;
            geneGEODBLink.setAccessionNumber(ACCESSION_NUM1);
            geneGEODBLink.setReferenceDatabase(geoDatabase);
            geneGEODBLink.setMarker( gene) ;
            session.save(geneGEODBLink) ; 
            session.flush() ; 


            List<String> currentMicroArrayAccessions = new ArrayList<String>() ; // empty
            List<String> newMicroArrayAccessions= new ArrayList<String>() ;
            currentMicroArrayAccessions.add( ACCESSION_NUM1 ) ;

            driver.loadGenBankDBs();
            driver.cleanupOldLinks( newMicroArrayAccessions , geoDatabase);
            session.flush() ; 

            Criteria criteria = session.createCriteria(MarkerDBLink.class) ;
            criteria.add(Restrictions.eq("accessionNumber",ACCESSION_NUM1)) ;
            criteria.add(Restrictions.eq("referenceDatabase",geoDatabase)) ;
            // should have added a DBLink for GEO for this accession # and marker
            MarkerDBLink link = (MarkerDBLink) criteria.uniqueResult() ;
            logger.info("link: " + link) ; 

            assertNull("should not exist",link) ;

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
            if(session!=null){
                // rollback on success or exception
                session.getTransaction().rollback();
            }
        }
     
     }



    /**
     * Add a gene accession when no previous accession was found.
     */
//    @Test
    public void fullTestAddGeoLinks(){
        Session session = null ;
        try{

            session = HibernateUtil.currentSession() ;
            session.beginTransaction() ;

            cleanDBLinks() ; 
            insertGeneMarker() ; 
            insertReferencedMarkers() ;

            Criteria criteria = session.createCriteria(MarkerDBLink.class) ;
            criteria.add(Restrictions.eq("referenceDatabase",geoDatabase)) ;
//            criteria.add(Restrictions.eq("marker",geneDBLink.getMarker())) ;
            // should have added a DBLink for GEO for this accession # and marker
            List<MarkerDBLink> links = criteria.list() ;
            assertEquals(0,links.size()) ; 

            Set<String> newAccessions = new HashSet<String>() ;

            newAccessions.add(geneGEOAccession) ;  // 1 link with  ACCESSION_NUM1 to this gene
            newAccessions.add(cDNAGEOAccession) ;  // 2 links with ACCESSION_NUM2 go to the CDNA and the encoding gene
            newAccessions.add(estGEOAccession) ;   // 2 links with ACCESSION_NUM3 go to the EST  and the encoding gene
            driver.init() ;
            Map<String,Set<MarkerDBLink>> microarrayLinks = sequenceRepository.getMarkerDBLinks(geoDatabase ) ;   // 0 - load microarray
            driver.processNewLinks(newAccessions,microarrayLinks,geoDatabase) ;
            session.flush() ; 

            links = criteria.list() ;

            assertEquals(5,links.size()) ; 
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
            if(session!=null){
                // rollback on success or exception
                session.getTransaction().rollback();
            }
        }
    }
     


    private void initTestData(){

        // get references DBs
        genBankRefDB =sequenceRepository.getReferenceDatabase(
                ForeignDB.AvailableName.GENBANK,
                ForeignDBDataType.DataType.GENOMIC,
                ForeignDBDataType.SuperType.SEQUENCE,
                Species.ZEBRAFISH);

        geoDatabase = sequenceRepository.getReferenceDatabase(ForeignDB.AvailableName.GEO,
                ForeignDBDataType.DataType.OTHER,ForeignDBDataType.SuperType.SUMMARY_PAGE, Species.ZEBRAFISH);

    }

    // Remove dblinks, but will be rolled back.
    private void cleanDBLinks(){
        Session session = HibernateUtil.currentSession() ;
        String hql = "delete from MarkerDBLink link where link.referenceDatabase=:referenceDatabase" ; 
        Query query = session.createQuery(hql); 
        query.setParameter("referenceDatabase",geoDatabase) ;
        int update = query.executeUpdate() ; 
        logger.info("records removed: " + update) ; 
    }


    private void insertGeneMarker(){
        Session session = HibernateUtil.currentSession() ;
        // create reference markers
        gene = new Marker();
        gene.setAbbreviation(GENE_NAME);
        gene.setName(GENE_NAME);
        //should this be an enum?
        gene.setMarkerType(markerRepository.getMarkerTypeByName(Marker.Type.GENE.toString()));
        gene.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(gene);
         
        geneDBLink = new MarkerDBLink() ;
        geneDBLink.setAccessionNumber(ACCESSION_NUM1);
        geneDBLink.setReferenceDatabase(genBankRefDB);
        geneDBLink.setMarker( gene) ;

        session.save(geneDBLink);

    }


    private void insertReferencedMarkers(){

        Session session = HibernateUtil.currentSession() ;

        cDNA = new Marker();
        cDNA.setAbbreviation(CDNA_NAME);
        cDNA.setName(CDNA_NAME);
        //should this be an enum?
        cDNA.setMarkerType(markerRepository.getMarkerTypeByName("CDNA"));
        cDNA.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(cDNA);
        markerRepository.addSmallSegmentToGene(gene, cDNA, "ZDB-PUB-070122-15" );

        est = new Marker();
        est.setAbbreviation(EST_NAME);
        est.setName(EST_NAME);
        //should this be an enum?
        est.setMarkerType(markerRepository.getMarkerTypeByName("EST"));
        est.setOwner(personRepository.getPerson("ZDB-PERS-030520-1"));
        session.save(est);
        markerRepository.addSmallSegmentToGene(gene, est, "ZDB-PUB-070122-15" );


        // set to cDNA
        cDNADBLink = new MarkerDBLink() ;
        cDNADBLink.setAccessionNumber(ACCESSION_NUM2);
        cDNADBLink.setReferenceDatabase(genBankRefDB);
        cDNADBLink.setMarker( cDNA) ;

        session.save(cDNADBLink);

        estDBLink = new MarkerDBLink() ;
        estDBLink.setAccessionNumber(ACCESSION_NUM3);
        estDBLink.setReferenceDatabase(genBankRefDB);
        estDBLink.setMarker( est) ;

        session.save(estDBLink);
    }
}
