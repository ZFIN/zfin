package zmapper;

import java.io.*;
import java.net.*;
import java.util.*;

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
    private int PORT = 7365;
	private String host;
	private static final String SEPARATOR = "|";	// The character which will separate fields in the result

	/**
	   Prepare to execute the query.

	   @param app The applet making the query.
	*/

    public SQLQuery (String host, String port)
	{
	  
	  this.host = host;
	  this.PORT = (new Integer(port)).intValue();

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
	public Vector select(int numFields, String request)
	{
        int port = PORT;
        Socket s = null;
	StringTokenizer sTok; 
	System.err.println("Query: " + request);

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
	      System.out.println("no connection");
            
            // Tell the user that we've connected
/*            System.out.println("Connected to " + s.getInetAddress()
			  + ":"+ s.getPort());*/
			// Send it to the server
			sout.println(SEPARATOR + numFields + SEPARATOR + request);
			// Read a line from the server.  
			String line = sin.readLine();
			while (line != null)
			{
			  //      System.out.println (line);
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
	System.out.println(j + " " + result.elementAt(j)); */
	return result; 

    }


}
