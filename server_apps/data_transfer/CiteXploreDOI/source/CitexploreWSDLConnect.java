/**
 *  Class CitexploreWSDLConnect.
 */
package org.zfin.datatransfer ; 

import javax.xml.ws.WebServiceRef;
import uk.ac.ebi.cdb.webservice.*;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.text.NumberFormat;

public class CitexploreWSDLConnect {

	@WebServiceRef(wsdlLocation="http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl")
	static WSCitationImplService service = new WSCitationImplService();


    public CitexploreWSDLConnect(){

    }


    /** getDOIsForPubmedID: 
     * @INPUT HashMap<ZDB_ID,PUBMEDID>
     * @OUTPUT HashMap<ZDB_ID,DOI>
     *
    */
    public HashMap<String,String> getDOIsForPubmedID(HashMap<String,String> pubmedIds){
        // ZDBID,DOI
        HashMap<String,String> doiList = new HashMap<String,String>(pubmedIds) ; 
        try {
            WSCitationImpl port = service.getWSCitationImplPort();

            System.out.println(" Invoking doc2loc operation on wscitationImpl port ");
            Iterator iter = doiList.keySet().iterator() ; 
            Doc2LocResultListBean doc2LocResultListBean ; 
            List<Doc2LocResultBean> doc2LocResultBeanCollection ; 
            String key ; 
            String pmid ; 
            String urlString  ; 
            String doiURLString = "http://dx.doi.org" ;
            String doiValue ; 
            int counter = 0 ; 
            boolean hasDOI ; 
            NumberFormat nf = NumberFormat.getPercentInstance() ; 
            while(iter.hasNext()){
                key = iter.next().toString() ; 
                pmid = doiList.get(key).toString()  ; 
                doc2LocResultListBean = port.doc2Loc("pmid", pmid );
                doc2LocResultBeanCollection = doc2LocResultListBean.getDoc2LocResultBeanCollection();


                hasDOI = false ; 
                for (Doc2LocResultBean doc2LocResultBean: doc2LocResultBeanCollection) {
                    urlString = doc2LocResultBean.getUrl() ; 
                    if(urlString.contains( doiURLString ) ){
                        doiValue = urlString.substring( doiURLString.length()+1 ) ; 
                        System.out.println("added doi[" + doiValue + "]  for pmid["+ pmid+"]") ; 
                        doiList.put(key,doiValue) ; 
                        hasDOI = true ; 
                    }
                }

                if(hasDOI == false ){
                    System.out.println("doi not found for pmid[" + pmid  + "]") ; 
                    iter.remove() ; 
                }

                ++counter ; 

                if( counter%100==0){
                    double percent =  (( (double) counter / ( (double) doiList.size()-1.0 ) )) ; 
                    System.out.println( counter + " of " + doiList.size() + " = " + nf.format(percent) ) ; 
                }
            }
        }catch (QueryException_Exception qex) {
            System.out.printf("Caught QueryException_Exception: %s\n", qex.getFaultInfo().getMessage());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        return doiList ; 
    }

} 

// -*- java -*-

// (C) 2007 by Nathan Dunn, <ndunn@mac.com>


