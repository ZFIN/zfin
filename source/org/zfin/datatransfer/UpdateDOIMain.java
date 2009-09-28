package org.zfin.datatransfer ;

import org.apache.log4j.Logger;
import org.zfin.datatransfer.webservice.Citexplore;
import org.zfin.framework.HibernateSessionCreator;
import org.zfin.framework.HibernateUtil;
import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.hibernate.Session;

import java.util.List;


/**  UpdateDOIMain is is the driver class that updates Publication DOIs which have existing pubmed IDs (accession numbers).
 *
 */
public class UpdateDOIMain {

    private int maxProcesses = -1 ;
    private static final String MAX_PROCESS_NULL_STRING = "-1" ;
    private static final String MAX_DOI_PROCESS = "MAX_DOI_PROCESS" ;
    private PublicationRepository publicationRepository = null ;

    // Logging data
    private Logger fullLogger = null ;
    private Logger lightLogger = null ;


    public UpdateDOIMain() {
        initLoggers();


        // instatiates method
        String[] confFiles = {
                "filters.hbm.xml",
                "antibody.hbm.xml",
                "anatomy.hbm.xml",
                "publication.hbm.xml",
                "expression.hbm.xml",
                "marker.hbm.xml",
                "orthology.hbm.xml",
                "mutant.hbm.xml",
                "people.hbm.xml",
                "sequence.hbm.xml",
                "infrastructure.hbm.xml",
                "blast.hbm.xml",
                "reno.hbm.xml",
                "mapping.hbm.xml",

        };
        new HibernateSessionCreator(false, confFiles) ;

        String maxDOIProcesses = System.getProperty( MAX_DOI_PROCESS, MAX_PROCESS_NULL_STRING ) ;
        publicationRepository = new HibernatePublicationRepository() ;
        try{
//            maxProcesses = (new Integer(maxDOIProcesses)).intValue()  ;
            maxProcesses = new Integer(maxDOIProcesses)  ;
            fullLogger.info("Max processes:"  + maxProcesses) ;
        }
        catch(NumberFormatException e){
            fullLogger.info("Scanning All Available Dois") ;
        }
    }

    private void initLoggers(){
        fullLogger = Logger.getLogger( ZfinProperties.FULL_UPDATE_DOI) ;
        lightLogger = Logger.getLogger( ZfinProperties.LIGHT_UPDATE_DOI ) ;
    }


    /**  Gets publications that have pubmedIds with no IDS from the database.
     * @return List<Publication> Returns list of publications without a DOI.
     */
    private List<Publication> getPubmedIdsWithNoDOIs(){
        List<Publication> publicationList =  publicationRepository.getPublicationsWithAccessionButNoDOI(maxProcesses) ;
        fullLogger.info("number of dois to populate:  " + publicationList.size() ) ;
        return publicationList ;
    }



    /**
     * Finds pumbed IDS with no DOI.  Accesses Citexplore via webservice to get DOIS for  pubmed IDS and finally updates pubmed IDS that it does find.
     */
    private void findAndUpdateDOIs(){
        try{
            List<Publication> publicationList = getPubmedIdsWithNoDOIs() ;
            Citexplore wsdlConnect = new Citexplore() ;
            publicationList = wsdlConnect.getDoisForPubmedID(publicationList) ;
            DOIHTTPTester httpTester = new DOIHTTPTester() ;
            publicationList = httpTester.testDOIList(publicationList) ;
            for(Publication publication: publicationList){
                lightLogger.info("added doi["+publication.getDoi()+"] for publication["+publication.getZdbID()+"]") ;
            }
            updateDOIs(publicationList) ;
        }
        catch(Exception e){
            fullLogger.info(e) ;
        }

    }


    /**  updateDOIs:  sets DOI for ZDB_ID
     *  @param  publicationList   A list of publications.
     *
     */
    private void updateDOIs(List<Publication> publicationList){

        if(publicationList==null || publicationList.size()==0 ){
            fullLogger.info("No sources to udpate") ;
            return ;
        }
        Session session = HibernateUtil.currentSession() ;
        session.beginTransaction() ;
        try{
            publicationRepository.updatePublications( publicationList) ;
            session.getTransaction().commit();
        }
        catch(Exception e){
            fullLogger.error(e) ;
            session.getTransaction().rollback();
        }

    }


    public static void main(String[] args) {
        try {
            UpdateDOIMain driver = new UpdateDOIMain();
            driver.findAndUpdateDOIs() ;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}
