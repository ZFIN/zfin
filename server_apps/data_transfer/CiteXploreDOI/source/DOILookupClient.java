package org.zfin.datatransfer ; 

import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator ;



public class DOILookupClient {

	private JDBCIfxConnect jdbcClient ;
	private CitexploreWSDLConnect wsdlConnect ; 
    private Integer maxQuery = Integer.MAX_VALUE ; 
    private static final String MAX_QUERY_PROPERTY = "MAX_QUERY_PROPERTY" ; 

    public DOILookupClient(){
        jdbcClient = new JDBCIfxConnect() ; 
        wsdlConnect = new CitexploreWSDLConnect() ; 
        String maxQueryProperty = System.getProperty( MAX_QUERY_PROPERTY) ; 
        if(maxQueryProperty!=null){
            try{
                maxQuery = new Integer(maxQueryProperty) ; 
            }
            catch(Exception e){
                e.printStackTrace() ; 
            }
        }
    }
		


    public HashMap<String,String> getPubmedIdsWithNoDOIs(){
        String queryString = "select zdb_id,accession_no from publication where accession_no is not null and pub_doi is null and accession_no !='none'" ; 
        Vector results = jdbcClient.selectAll( 2,queryString) ; 
        Enumeration e = results.elements() ; 
        HashMap<String,String> pubmedIds =new HashMap<String,String>() ; 

        int count = 0 ; 
        while(e.hasMoreElements()){
            pubmedIds.put( e.nextElement().toString(), e.nextElement().toString() ) ; 
            ++count ; 
            // for debugging
           if(count > maxQuery.intValue() ){
               System.out.println("number of dois to populate:  " + count) ; 
               return pubmedIds  ; 
           }
        }
        System.out.println("number of dois to populate:  " + count) ; 

        return pubmedIds ; 
    }


    /**  updateDOIs:  sets DOI for ZDB_ID
     *  @Input HashMap<ZDB_ID,DOI>
     *  @Output boolean  (true if successful)
     *
     */
    public boolean updateDOIs(HashMap<String,String> doiList){
        Iterator iter = doiList.keySet().iterator() ; 
        String queryStart = "update publication set pub_doi = " ; 
        String queryEnd = "where zdb_id = " ; 
        String key ; 
        String query ; 
        int numUpdates ; 
        while(iter.hasNext()){
            key = iter.next().toString() ; 
            if( key.equalsIgnoreCase( doiList.get(key )) == false ){
                 
            }

            query = queryStart + "'" + doiList.get(key) +"' " + queryEnd + "'" + key +"'" ; 
            numUpdates = jdbcClient.update( query) ; 
            if(numUpdates <=0){
                System.err.println(  "failed to udpate: zdbid["+ key + "] doi[" + doiList.get(key) +"]" ) ; 
                return false ; 
            }
        }

        return true ; 
    }


    public void findAndUpdateDOIs(){
        try{
			HashMap<String,String> pubmedIds = getPubmedIdsWithNoDOIs() ; 
			HashMap<String,String> doiList = wsdlConnect.getDOIsForPubmedID(pubmedIds) ; 
			updateDOIs(doiList) ; 
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
