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


    public boolean updateDOIs(HashMap<String,String> doiList){


        return false ; 
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
	

    public int update(String query)  {
		int result = -1;
		Connection conn = connect();

        try {
            Statement update = conn.createStatement();
            result = update.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("ERROR: Insert/Update/Delete statement failed: " + e.getMessage());
        }
		
		return result;
	}

} 

// -*- java -*-

// (C) 2007 by Nathan Dunn, <ndunn@mac.com>


