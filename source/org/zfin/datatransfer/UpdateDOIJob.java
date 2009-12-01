package org.zfin.datatransfer ;

import org.apache.log4j.Logger;
import org.apache.commons.collections.CollectionUtils;
import org.zfin.datatransfer.webservice.Citexplore;
import org.zfin.framework.HibernateUtil;
import org.zfin.publication.Publication;
import org.zfin.publication.repository.HibernatePublicationRepository;
import org.zfin.publication.repository.PublicationRepository;
import org.hibernate.Session;

import java.util.List;


/**  UpdateDOIMain is is the driver class that updates Publication DOIs which have existing pubmed IDs (accession numbers).
 *
 */
public class UpdateDOIJob {

    private int maxProcesses = -1 ;
    private PublicationRepository publicationRepository = null ;
    private Logger logger = Logger.getLogger(UpdateDOIJob.class);

    private boolean reportAll = false ;
    private boolean doisUpdated = false ;

    private StringBuilder message ;

    public UpdateDOIJob(boolean reportAll) {
        this.reportAll = reportAll ;
        message = new StringBuilder() ;
        publicationRepository = new HibernatePublicationRepository() ;
    }


    /**  Gets publications that have pubmedIds with no IDS from the database.
     * @return List<Publication> Returns list of publications without a DOI.
     */
    private List<Publication> getPubmedIdsWithNoDOIs(){
        List<Publication> publicationList =  publicationRepository.getPublicationsWithAccessionButNoDOI(maxProcesses) ;
        if(reportAll || CollectionUtils.isNotEmpty(publicationList)){
            message.append("number of dois to populate:  ").append(publicationList.size() ).append("\n") ;
        }
        return publicationList ;
    }



    /**
     * Finds pumbed IDS with no DOI.  Accesses Citexplore via webservice to get DOIS for  pubmed IDS and finally updates pubmed IDS that it does find.
     */
    public void findAndUpdateDOIs(){
        try{
            List<Publication> publicationList = getPubmedIdsWithNoDOIs() ;
            Citexplore wsdlConnect = new Citexplore() ;
            publicationList = wsdlConnect.getDoisForPubmedID(publicationList) ;
            DOIHTTPTester httpTester = new DOIHTTPTester() ;
            publicationList = httpTester.testDOIList(publicationList) ;
            for(Publication publication: publicationList){
                message.append("added doi["+publication.getDoi()+"] for publication["+publication.getZdbID()+"]") ;
            }
            updateDOIs(publicationList) ;
            HibernateUtil.closeSession();
        }
        catch(Exception e){
            logger.error(e);
            message.append(e) ;
        }
    }



    /**  updateDOIs:  sets DOI for ZDB_ID
     *  @param  publicationList   A list of publications.
     *
     */
    private void updateDOIs(List<Publication> publicationList){

        if(publicationList==null || publicationList.size()==0 ){
            message.append("No sources to udpate") ;
            doisUpdated=false ;
            return ;
        }
        else{
            doisUpdated=true;
        }
        Session session = HibernateUtil.currentSession() ;
        session.beginTransaction() ;
        try{
            publicationRepository.updatePublications( publicationList) ;
            session.getTransaction().commit();
        }
        catch(Exception e){
            logger.error(e) ;
            message.append(e);
            session.getTransaction().rollback();
        }

    }

    public StringBuilder getMessage() {
        return message;
    }

    public boolean isDoisUpdated() {
        return doisUpdated;
    }

    public static void main(String[] args) {
        try {
            UpdateDOIJob driver = new UpdateDOIJob(true);
            driver.findAndUpdateDOIs() ;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


}
