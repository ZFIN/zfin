package org.zfin.datatransfer ; 

import java.util.List;


/**  DOILookupClient is is the driver class that updates Publication DOIs which have existing pubmed IDs (accession numbers).
 *
 */
public class DOILookupClient {

	private InformixPublicationAccessor jdbcClient ;
    private int maxProcesses = -1 ; 
    private static final String MAX_DOI_PROCESS = "MAX_DOI_PROCESS" ; 

    public DOILookupClient(){
        jdbcClient = new InformixPublicationAccessor() ;
        String maxDOIProcesses = System.getProperty( MAX_DOI_PROCESS) ; 
        if(maxDOIProcesses!=null){
            try{
                maxProcesses = (new Integer(maxDOIProcesses)).intValue()  ; 
            }
            catch(Exception e){
                System.err.println("Scanning All Dois") ; 
//                e.printStackTrace() ; 
            }
        }
    }
		


    /**  Gets publications that have pubmedIds with no IDS from the database.
     */
    public List<Publication> getPubmedIdsWithNoDOIs(){
        String queryString ; 
        if(maxProcesses >=0){
            queryString = "select first " + maxProcesses + " zdb_id,accession_no,pub_doi from publication where accession_no is not null and pub_doi is null and accession_no !='none'" ; 
        }
        else{
            queryString = "select zdb_id,accession_no,pub_doi from publication where accession_no is not null and pub_doi is null and accession_no !='none'" ; 
        }

        List<Publication> publicationList = jdbcClient.selectPublications(queryString) ; 

        System.out.println("number of dois to populate:  " + publicationList.size() ) ; 
        return publicationList ; 
    }



    /**
     * Finds pumbed IDS with no DOI.  Accesses Citexplore via webservice to get DOIS for  pubmed IDS and finally updates pubmed IDS that it does find.
     */
    public void findAndUpdateDOIs(){
        try{
            System.out.println("START - getting publication list") ; 
			List<Publication> publicationList = getPubmedIdsWithNoDOIs() ; 
            System.out.println("END - getting publication list") ; 
            System.out.println("START - connect to CiteXploreWSDL") ; 
            CitexploreWSDLConnect wsdlConnect = new CitexploreWSDLConnect() ; 
            System.out.println("END - connect to CiteXploreWSDL") ; 
            System.out.println("START - receive updates from wsdl client") ; 
            publicationList = wsdlConnect.getDOIsForPubmedID(publicationList) ; 
            System.out.println("END - receive updates from wsdl client") ; 
            System.out.println("START - update pubmed DOIS in DB") ; 
            jdbcClient.updateDOIs(publicationList) ; 
            System.out.println("END - update pubmed DOIS in DB") ; 
        }
        catch(Exception e){
            System.err.println(e) ; 
        }

    }

	public static void main(String[] args) {
		try {
			DOILookupClient client = new DOILookupClient();
            client.findAndUpdateDOIs() ; 
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
