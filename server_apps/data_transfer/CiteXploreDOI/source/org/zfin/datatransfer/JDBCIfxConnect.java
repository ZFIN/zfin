/**
 *  Class JDBCIfxConnect.
 */
package org.zfin.datatransfer ; 

import java.sql.*;
import com.informix.jdbc.*;

import java.util.Vector;
import java.util.HashMap;
import java.util.*;

public class JDBCIfxConnect {

	public Connection connect(){
        String CC = "sswo";
        String cookie  = cook("st4mwtR") ;
        String informixServer = System.getProperty("INFORMIXSERVER") ; 
        String dbName = System.getProperty("DBNAME") ; 


        // from client_app/BrowserSQLQuery
		String newUrl = "jdbc:informix-sqli://embryonix.cs.uoregon.edu:2002/"+dbName+":INFORMIXSERVER="+informixServer+";user=zfinner;pa"+CC + "r" + "d="+ cookie;
		
		Connection conn = null;
		
		try { Class.forName("com.informix.jdbc.IfxDriver"); } 
		catch (Exception e) { System.err.println("ERROR: failed to load Informix JDBC driver. - " + e);	}

		try {  conn = DriverManager.getConnection(newUrl);  } 
		catch (SQLException e) { System.err.println("ERROR: failed to connect! - " + e); } 

		return conn;
	}

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



    public Vector selectAll(int numFields, String request) {
		Vector<String> results = new Vector<String>();
		Connection conn = connect();

        try {
            Statement select = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            select.setFetchSize(200);
            select.setFetchDirection(ResultSet.FETCH_FORWARD);	
            ResultSet r = select.executeQuery(request);

            int i;
            while(r.next()) {
                for (i=1 ; i <= numFields ; i++) {
                    results.add(r.getString(i));
                }
            }
            r.close();
            select.close();
        } catch (SQLException e) {
            System.err.println("ERROR: Fetch statement failed: " + e.getMessage());
        }
		
		return results;
		
	}


    /**  updateDOIs:  sets DOI for ZDB_ID
     *  @Input HashMap<ZDB_ID,DOI>
     *  @Output boolean  (true if successful)
     *
     */
    public boolean updateDOIs(HashMap<String,String> doiList){
		Connection conn = connect();
        Statement update = null ; 
		int result = -1;

        if(doiList==null || doiList.keySet()==null ){
            System.out.println("No publications to udpate") ; 
            return false ; 
        }

        Iterator iter = doiList.keySet().iterator() ; 
        String queryStart = "update publication set pub_doi = " ; 
        String queryEnd = "where zdb_id = " ; 
        String key ; 
        String query ; 
        int numUpdates ; 
        while(iter.hasNext()){
            key = iter.next().toString() ; 
            query = queryStart + "'" + doiList.get(key) +"' " + queryEnd + "'" + key +"'" ; 
            try {
                update = conn.createStatement();
                result = update.executeUpdate(query);
//                System.out.println("query: " + query ) ; 
                if(result <= 0){
                    System.err.println(  "failed to udpate: zdbid["+ key + "] doi[" + doiList.get(key) +"]" ) ; 
                }
            } catch (SQLException e) {
                System.err.println("ERROR: Insert/Update/Delete statement failed: " + e.getMessage());
                // reset connection
                if(update != null){
                    try{
                        update.close() ; 
                        conn = connect();
                    }
                    catch(Throwable t){
                        t.printStackTrace() ; 
                    }
                    return false ; 
                }
            }
        }
        // close connection if still open
       if(update!=null){
           try{
                update.close() ; 
           }
           catch(SQLException sqle){
               sqle.printStackTrace() ; 
           }
    }

        return true ; 
    }

	

    public int update(String query)  {
		int result = -1;
		Connection conn = connect();
        Statement update = null ; 

        try {
            update = conn.createStatement();
            result = update.executeUpdate(query);
            update.close() ; 
        } catch (SQLException e) {
            System.err.println("ERROR: Insert/Update/Delete statement failed: " + e.getMessage());
        }
        finally{
            if(update != null){
                try{
                    update.close() ; 
                }
                catch(Throwable t){
                    t.printStackTrace() ; 
                }
                return -1 ; 
            }
        }

		
		return result;
	}

} 


