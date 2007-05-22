import java.io.*;
import java.net.*;
import java.applet.*;
import java.util.*;

import java.sql.*;
import com.informix.jdbc.*;

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

    public SQLQuery (Applet app)
	{

		this.host = app.getDocumentBase().getHost();
	}

	public SQLQuery(String host, String port)
	{

	  this.host = host;
//	  this.PORT = (new Integer(port)).intValue();

	}

    public Connection connect() {

		String CC = "sswo";
		
		String C = "<!--|ZFIN_COOKIE|-->";
		C = cook(C);
		String newUrl = "jdbc:informix-sqli://<!--|SQLHOSTS_HOST|-->:<!--|INFORMIX_PORT|-->/<!--|DB_NAME|-->:INFORMIXSERVER=<!--|INFORMIX_SERVER|-->;user=zfinner;pa"+CC + "r" + "d="+ C;

		Connection conn = null;
		
		try { Class.forName("com.informix.jdbc.IfxDriver"); } 
		catch (Exception e) { System.err.println("ERROR: failed to load Informix JDBC driver. - " + e);	}

		try {  conn = DriverManager.getConnection(newUrl);  } 
		catch (SQLException e) { System.err.println("ERROR: failed to connect! - " + e); } 

		return conn;

		}

    public int update(String query)  {
		int result = -1;
		Connection conn = connect();

		if (conn != null) {
		
			try {
				Statement update = conn.createStatement();
				result = update.executeUpdate(query);
			} catch (SQLException e) {
				System.err.println("ERROR: Insert/Update/Delete statement failed: " + e.getMessage());
			}
		} else {
			selectAll_javaserver(1,query);
	    }
		
		
		return result;
	}

 
    public Vector selectAll(int numFields, String request) {
		Vector V = new Vector();
		Vector results = new Vector();

		Connection conn = connect();

		if (conn != null) 	{
			

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
		} else {
			results = selectAll_javaserver(numFields, request);
			
		}
		
		
		
		return results;
		
	}



	public Vector selectAll_javaserver(int numFields, String request)
	{
        int port = PORT;
        Socket s = null;
	StringTokenizer sTok; 
	//System.err.println("Query: " + request);

	Vector result = new Vector ();
        try {
            // Create a socket to communicate to the specified host and port
            s = new Socket(host, port);
            // Create streams for reading and writing lines of text
            // from and to this socket.
            DataInputStream sin = new DataInputStream(s.getInputStream());
            PrintStream sout = new PrintStream(s.getOutputStream());

	    if ((sin == null) || (sout == null))
	      System.err.println("no connection");
            
            // Tell the user that we've connected
		//System.out.println("Connected to " + s.getInetAddress()  + ":"+ s.getPort());
			// Send it to the server
			sout.println(SEPARATOR + numFields + SEPARATOR + request);
			// Read a line from the server.  
			String line = sin.readLine();
			while (line != null)
			{
			  //      System.out.println (line);
			  sTok = new StringTokenizer(line,SEPARATOR);
			  while (sTok.hasMoreElements())
				  result.addElement((String)sTok.nextElement());

			  line = sin.readLine ();
			}
        }
        catch (IOException e) { System.err.println("Data read: " + e); }
        // Always be sure to close the socket
        finally {
            try { if (s != null) s.close(); } catch (IOException e2) { ; }
        }

	/*		int j = 0;
	for (j = 0; j < result.size() ; j ++) 
	System.out.println(j + " " + result.elementAt(j)); */
		System.err.println("result size: " + result.size()); 
		return result; 

    }

	/**
	   Execute a select statement.

	   @param numFields The number of fields returned by the SELECT.  Ragged results
	       are not supported---every row must have exactly this number of fields.
	   @param request The SQL SELECT statement to be executed.  The terminating ";" should
	       be included.
	   @param builder An instance of a class which can parse the delimited String returned
	       by the query and break it up into units which are meaningful to the caller.
	   @return A Vector of Objects, each Object representing a row returned by the
	       Select statement.
	*/
	public Vector xx_selectAll_javaserver (int numFields, String request)
	{
        int port = PORT;
        Socket s = null;
	StringTokenizer sTok; 

	Vector result = new Vector ();
        try {
            // Create a socket to communicate to the specified host and port
            s = new Socket(host, port);
            // Create streams for reading and writing lines of text
            // from and to this socket.
            DataInputStream sin = new DataInputStream(s.getInputStream());
            PrintStream sout = new PrintStream(s.getOutputStream());

	    if ((sin == null) || (sout == null))
	      System.err.println("no connection");
            
            // Tell the user that we've connected
            System.err.println("Connected to " + s.getInetAddress()
			  + ":"+ s.getPort());
			// Send it to the server
			sout.println(SEPARATOR + numFields + SEPARATOR + request);
			// Read a line from the server.  
			String line = sin.readLine();
			while (line != null)
			{
//			  System.err.println (line);
			  sTok = new StringTokenizer(line,SEPARATOR);
			  result.addElement((String)sTok.nextElement());//name 
			  if (numFields > 2) {
			    result.addElement(new Integer((String)sTok.nextElement()));//stage
			    result.addElement(new Integer((String)sTok.nextElement()));//level
			    result.addElement(new Integer((String)sTok.nextElement()));//seq_num 
			  } else if (numFields == 2) {
			    result.addElement((String)sTok.nextElement()); //for item labels in listselector
			  } 
			  
	

			  line = sin.readLine ();
			}
        }
        catch (IOException e) { System.err.println("Data read: " + e); }
        // Always be sure to close the socket
        finally {
            try { if (s != null) s.close(); } catch (IOException e2) { ; }
        }

	/*		int j = 0;
	for (j = 0; j < result.size() ; j ++) 
	System.err.println(j + " " + result.elementAt(j)); */
	return result; 

    }
	

	/**
	   Execute a select statement.

	   @param numFields The number of fields returned by the SELECT.  Ragged results
	       are not supported---every row must have exactly this number of fields.
	   @param request The SQL SELECT statement to be executed.  The terminating ";" should
	       be included.
	   @param builder An instance of a class which can parse the delimited String returned
	       by the query and break it up into units which are meaningful to the caller.
	   @return A Vector of Objects, each Object representing a row returned by the
	       Select statement.
	*/
	public Vector selectConstrained(int numFields, String request)
	{
        int port = PORT;
        Socket s = null;
	StringTokenizer sTok; 

	Vector result = new Vector ();
        try {
            // Create a socket to communicate to the specified host and port
            s = new Socket(host, port);
            // Create streams for reading and writing lines of text
            // from and to this socket.
            DataInputStream sin = new DataInputStream(s.getInputStream());
            PrintStream sout = new PrintStream(s.getOutputStream());

	    if ((sin == null) || (sout == null))
	      System.err.println("no connection");
            
            // Tell the user that we've connected
/*            System.err.println("Connected to " + s.getInetAddress()
			  + ":"+ s.getPort());*/
			// Send it to the server
			sout.println(SEPARATOR + numFields + SEPARATOR + request);
			// Read a line from the server.  
			String line = sin.readLine();
			while (line != null)
			{
			  //      System.err.println (line);
			  sTok = new StringTokenizer(line,SEPARATOR);
			  result.addElement(new Integer((String)sTok.nextElement()));//seq_num
			  line = sin.readLine ();
			}
        }
        catch (IOException e) { System.err.println("Data read: " + e); }
        // Always be sure to close the socket
        finally {
            try { if (s != null) s.close(); } catch (IOException e2) { ; }
        }

	/*		int j = 0;
	for (j = 0; j < result.size() ; j ++) 
	System.err.println(j + " " + result.elementAt(j)); */
	return result; 

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
