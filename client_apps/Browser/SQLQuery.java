import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;
import java.sql.*;


/**
   Issue an SQL query to the database server.  The calling class
   takes care of all the parsing of the results, by passing us
   a SQLResultBuilder instance to do the parsing.

   @author Arthur Kirkpatrick
   @version 1.0, Jan. 10, 1998.

   @see SQLResultBuilder
   @see QueryMarkers
 */
public class SQLQuery
{
    private int PORT = -1;
	private String host;
	private static final String SEPARATOR = "|";	// The character which will separate fields in the result

	/**
	   Prepare to execute the query.

	   @param app The applet making the query.
	*/

    public SQLQuery ( ) {
	
	}


    public SQLQuery (Applet app)
	{

		this.host = app.getDocumentBase().getHost();
	}


    public Vector selectAll(int numFields, String request) {
		Vector V = new Vector();
		Vector results = new Vector();
		String CC = "sswo";
		
		String C = "<!--|ZFIN_COOKIE|-->";
		C = cook(C);
		String newUrl = "jdbc:informix-sqli://<!--|DOMAIN_NAME|-->:<!--|INFORMIX_PORT|-->/<!--|DB_NAME|-->:INFORMIXSERVER=<!--|INFORMIX_SERVER|-->;user=zfinner;pa"+CC + "r" + "d="+ C;

		Connection conn = null;
		
/***		try { Class.forName("com.informix.jdbc.IfxDriver"); } 
		catch (Exception e) { System.err.println("ERROR: failed to load Informix JDBC driver. - " + e);	}

		try {  conn = DriverManager.getConnection(newUrl);  } 
		catch (SQLException e) { System.out.println("ERROR: failed to connect! - " + e); } 

	

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
				System.out.println("ERROR: Fetch statement failed: " + e.getMessage());
				}
*****/
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
