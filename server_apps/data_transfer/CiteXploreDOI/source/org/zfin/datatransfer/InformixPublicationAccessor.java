
/**
 *  Class InformixPublicationAccessor.  This class handles JDBC connections to informix and knows about the Publication object.  This should be replaced with hibernate when it comes into the java branch.
 */
package org.zfin.datatransfer ; 

import java.sql.*;

import java.util.ArrayList ;
import java.util.*;


public class InformixPublicationAccessor {

    /** getConnection.
     * Grabs a connection to the informix database.   
     *
     */
    public Connection getConnection(){
        String CC = "sswo";
        String password = cook("st4mwtR") ;
        String informixServer = System.getProperty("INFORMIXSERVER") ; 
        String dbName = System.getProperty("DBNAME") ; 


        // from client_app/BrowserSQLQuery
		String newUrl = "jdbc:informix-sqli://embryonix.cs.uoregon.edu:2002/"+dbName+":INFORMIXSERVER="+informixServer+";user=zfinner;pa"+CC + "r" + "d="+ password ;
		
		Connection conn = null;
		
		try { Class.forName("com.informix.jdbc.IfxDriver"); } 
		catch (Exception e) { System.err.println("ERROR: failed to load Informix JDBC driver. - " + e);	}

		try {  conn = DriverManager.getConnection(newUrl);  } 
		catch (SQLException e) { System.err.println("ERROR: failed to connect! - " + e); } 

		return conn;
	}

    /** Legacy code to convert password string.
     *
     *
     */
	public String cook(String C) {
		int f, l;
		String fS, lS;
		char fC,lC;
		f = 0;
		l = C.length() - 1;
		
		
		int i = C.length()/2;

		char[] arr = C.toCharArray();
		
		while (i > 0) {
			fC = arr[f];
			lC = arr[l];
			arr[f] = lC;
			arr[l] = fC;
			i--;
			f++;
			l--;
		}
		C = String.valueOf(arr);
		return C;
	}


    /**  Grabs a list of publications based on a query.  SQL is passed in because it will be replaced with a hibernate client DOILokoupClient.
     * @param request
     * @return List<Publication>
     */
    public List<Publication> selectPublications(String request) {
		List<Publication> publicationList = new ArrayList<Publication>();
		Connection conn = getConnection();
        ResultSet resultSet =  null ; 
        Statement select = null ; 

        try {
            select = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            select.setFetchSize(200);
            select.setFetchDirection(ResultSet.FETCH_FORWARD);	
            resultSet  = select.executeQuery(request);
            while( resultSet.next() ){
                Publication publication = new Publication() ; 
                publication.setZdbID( resultSet.getString(1) ) ;  
                publication.setAccessionNumber( resultSet.getString(2) ) ;  
                publication.setPubDOI( resultSet.getString(3) ) ;  
                publicationList.add(publication) ; 
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Fetch statement failed: " + e.getMessage());
        }
        finally{
            try{
                if(select!=null){
                    select.close();
                }
                if(resultSet!=null){
                    resultSet.close() ; 
                }
            }catch(Exception e){
                e.printStackTrace() ; 
            }

        }
	
        return publicationList ; 
		
	}




    /**  updateDOIs:  sets DOI for ZDB_ID
     *  @Input HashMap<ZDB_ID,DOI>
     *  @Output boolean  (true if successful)
     *
     */
    public boolean updateDOIs(List<Publication> publicationList){

        if(publicationList==null || publicationList.size()==0 ){
            System.out.println("No publications to udpate") ; 
            return true ; 
        }

		int result = -1;
        boolean returnState = false ; 

		Connection conn = getConnection();
        PreparedStatement updateDOIStatement = null ; 
        try {
            conn.setAutoCommit(false) ; 
            updateDOIStatement = conn.prepareStatement("update publication set pub_doi = ? where zdb_id = ? ") ; 

            for( Publication publication : publicationList  ){ 
                if(publication.getPubDOI()!=null){
                    updateDOIStatement.setString(1,publication.getPubDOI()) ; 
                    updateDOIStatement.setString(2,publication.getZdbID()) ; 
                    result = updateDOIStatement.executeUpdate();
                    if(result <= 0){
                        System.err.println(  "failed to update: zdbid["+ publication.getZdbID()+ "] doi[" + publication.getPubDOI() +"]" ) ; 
                    }
                    else
                    if(result > 1){
                        System.err.println(  "zdbid["+ publication.getZdbID() + "] has multiple hits[" +result +"]" ) ; 
                        returnState = false ;
                    }
                    else{ // there should only be on result!
                        conn.commit() ; 
                    }
                }
            }
            returnState = true ; 
        } catch (SQLException sqle) {
            System.err.println("ERROR: Update statement: \n" + updateDOIStatement + "  \nfailed: " + sqle.getMessage());
            returnState = false ; 
        } catch (Exception e) {
            System.err.println("Exception: \n" + updateDOIStatement  + "  \nfailed: " + e.getMessage());
            returnState = false ; 
        }
        finally{
            // close connection if still open
           if(updateDOIStatement!=null){
               try{
                    updateDOIStatement.close() ; 
                    conn.close() ; 
               }
               catch(SQLException sqle){
                   sqle.printStackTrace() ; 
                   returnState = false ; 
               }
            }
        }
        return returnState ; 
    }
} 





