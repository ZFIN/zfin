package zmapper;

/*
  Retrieves marker data and builds Marker objects

*/

import java.util.*;
import java.sql.*;
import com.informix.jdbc.*;

public class MarkerBuilder {

	String host_id;
	String host_port;

	SQLQuery SQL;
	
	public MarkerBuilder (String host_id, String host_port)  {
		this.host_id = host_id;
		this.host_port = host_port;

		SQL = new SQLQuery(host_id, host_port);
	}	

	public MarkerBuilder() {
		
	}
	
	public Vector buildMarkers(String data) {
		StringTokenizer sTok = new StringTokenizer(data, "|");
		Vector V = new Vector();
		while(sTok.hasMoreTokens()) {
			try 
			{
				V.addElement(new Marker((String)sTok.nextToken() ,
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken()));
			}
			catch (java.util.NoSuchElementException e) 
			{
				System.err.println("MarkerBuilder.buildMarkers - " + e);
			}
			
		}	
		return V;
	}

	public Vector getMarkers(String query_string) {
		//return getMarkersJDBC(query_string);
		return getMarkersJS(query_string);
	}
	
	
	public Vector getMarkersJDBC(String query_string) {
		Vector V = new Vector();
		Vector results = new Vector();
		String CC = "sswo";
		
		String C = "<!--|ZFIN_COOKIE|-->";
		C = cook(C);
		String newUrl = "jdbc:informix-sqli://<!--|DOMAIN_NAME|-->:<!--|INFORMIX_PORT|-->/<!--|DB_NAME|-->:INFORMIXSERVER=<!--|INFORMIX_SERVER|-->;user=zfinner;pa"+CC + "r" + "d="+ C;

    	Connection conn = null;
		
		try { Class.forName("com.informix.jdbc.IfxDriver"); } 
		catch (Exception e) { System.err.println("ERROR: failed to load Informix JDBC driver. - " + e);	}

		try {  conn = DriverManager.getConnection(newUrl);  } 
		catch (SQLException e) { System.out.println("ERROR: failed to connect! - " + e); } 



	try {
		Statement selectMarkers = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		selectMarkers.setFetchSize(200);
		selectMarkers.setFetchDirection(ResultSet.FETCH_FORWARD);	
		ResultSet r = selectMarkers.executeQuery(query_string);
			
		String zdb_id, abbrev, mtype, target_abbrev, metric, lg_location, OR_lg, mghframework;
//		int lg_location, OR_lg;
//		boolean mghframework;
		Marker M;
		
		while(r.next()) {
			zdb_id = r.getString(1);
			abbrev = r.getString(2);
			mtype = r.getString(3);
			target_abbrev = r.getString(4);
			lg_location = r.getString(5);
			OR_lg = r.getString(6);
			mghframework = r.getString(7);
			metric = r.getString(8);

			M = new Marker(zdb_id, abbrev, mtype, target_abbrev, lg_location, OR_lg, mghframework, metric);
			results.add(M);
		}


	    r.close();
	    selectMarkers.close();
	} catch (SQLException e) {
		System.out.println("ERROR: Fetch statement failed: " + e.getMessage());
	}

	if (query_string.indexOf("desc;") >= 0) { //they're ordered descending and have to be reversed..
		Vector VV = new Vector();
		int i = results.size() - 1; //fencepost...
		while(i >= 0) 	{
			VV.addElement(results.elementAt(i));
			i--;
			
			}
		

		results = VV;
		
		}
	
	return results;
  }
	
	
	public Vector getMarkersJS(String query_string) 	{
		Vector V = SQL.selectAll(8, query_string);
		Vector results = new Vector();
		Marker M;
		
		Enumeration E = V.elements();
		while(E.hasMoreElements()) {
			M = new Marker((String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement());
			results.addElement(M);

		}

		if (query_string.indexOf("desc;") >= 0) { //they're ordered descending and have to be reversed..
			Vector VV = new Vector();
			int i = results.size() - 1; //fencepost...
			while(i >= 0) 	{
				VV.addElement(results.elementAt(i));
				i--;

			}
			

			results = VV;
			
		}
		
		return results;
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
	

}
