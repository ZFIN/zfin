/**
 *  Class CitexploreWSDLConnect.  Collects DOIs from the CiteXplorer webservice.
 */
package org.zfin.datatransfer ; 

import javax.xml.ws.WebServiceRef;
import uk.ac.ebi.cdb.webservice.*;

import java.util.List;
import java.text.NumberFormat;

public class CitexploreWSDLConnect {

	@WebServiceRef(wsdlLocation="http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl")
	static WSCitationImplService service = new WSCitationImplService();
    private final String PMID_TOKEN = "pmid" ; 
    private final String DOI_URL = "http://dx.doi.org" ;



    /** getDoisForPubmedID.
     * @INPUT List<Publication>
     * @OUTPUT List<Publication>
     * Note:  This is a destructive method which changes the input structure.
     * Iterates through the Publication list and updates the DOI, if it exists, from the CiteXplorer Webservice.
     * The DOI is the key and doc2loc is the webservice method from the compiled client-side webservice code.
     * Many IDs are returned, one of which may be the DOI, which always contains the DOI_URL link.
     *
    */
    public List<Publication> getDOIsForPubmedID(List<Publication> publicationList){
        try {
            System.err.println(" Invoking doc2loc operation on wscitationImpl port ");
            Doc2LocResultListBean doc2LocResultListBean ;
            List<Doc2LocResultBean> doc2LocResultBeanCollection ; 
            String urlString , doiValue ;
            int counter = 0 ; 
            boolean hasDOI ; 
            int initSize = publicationList.size() ; 

            WSCitationImpl port = service.getWSCitationImplPort();
            for( Publication publication: publicationList  ){
                doc2LocResultListBean = port.doc2Loc(PMID_TOKEN, publication.getAccessionNumber() );
                doc2LocResultBeanCollection = doc2LocResultListBean.getDoc2LocResultBeanCollection();
                hasDOI = false ; 
                for (Doc2LocResultBean doc2LocResultBean: doc2LocResultBeanCollection) {
                    urlString = doc2LocResultBean.getUrl() ; 
                    if(urlString.contains( DOI_URL ) ){
                        doiValue = urlString.substring( DOI_URL.length()+1 ) ; 
                        System.out.println("added doi[" + doiValue + "]  for pmid["+ publication.getAccessionNumber() +"]") ; 
                        publication.setPubDOI(doiValue) ; 
                        hasDOI = true ; 
                    }
                }

                if(hasDOI == false ){
                    System.err.println("doi not found for pmid[" + publication.getAccessionNumber() + "]") ; 
                    publication.setPubDOI(null) ; 
                }

                ++counter ; 
                if( counter%100==0){
                    printStatus( counter,initSize) ; 
                }
            }
        }catch (QueryException_Exception qex) {
            System.out.printf("Caught QueryException_Exception: %s\n", qex.getFaultInfo().getMessage());
        }
        catch(Exception e) {
            System.out.println("Unable to access dois:\n"+ e.getMessage() ) ; 
            return publicationList ; 
        }

        return publicationList; 
    }



    public void printStatus( int counter, int initSize){
        NumberFormat nf = NumberFormat.getPercentInstance() ; 
        double percent =  (( (double) counter / ( (double) initSize-1.0 ) )) ; 
        System.out.println( counter + " of " + initSize  + " = " + nf.format(percent) ) ; 
    }

} 



