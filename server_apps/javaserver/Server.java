// Simple server for SQL statements from an Illustra database.
// The server can only accept one connection at a time.
//
// Modified version of example 7-5 of _Java in a Nutshell_ by David Flanagan.
// Written by David Flanagan.  Copyright (c) 1996 O'Reilly & Associates.
// You may study, use, modify, and distribute this example for any purpose.
// This example is provided WITHOUT WARRANTY either expressed or implied.
//
// Modified by Ted Kirkpatrick for the ZFIN project.

import java.io.*;
import java.net.*;
import java.util.Date;

public class Server extends Thread
{
    public final static int DEFAULT_PORT = 7365;

    protected int port;
    protected ServerSocket listen_socket;

	// The amount of output to write to Server.out
	// (in order of increasing amount of output)
	public static final int NO_OUTPUT = 0;
	public static final int CONNECTION_OUTPUT = 1;
	public static final int QUERY_OUTPUT = 2;
	public int outputLevel = CONNECTION_OUTPUT;

	// To ensure only 1 thread accesses milib at a time
	Object semaphore = new Object ();
	boolean activeConnection [] = new boolean [1];
    
    // Start the server up, listening on an optionally specified port
    public static void main(String[] args)
	{
        int port = 0;
        if (args.length == 1)
		{
            try { port = Integer.parseInt(args[0]);  }
            catch (NumberFormatException e) { port = 0; }
        }
        new Server(port);
    }

    // Create a ServerSocket on which to listen for connections;  start the thread.
    public Server(int port) {
		System.out.println ("\n\n\n------------------ Starting Java Illustra server -----------------");
		System.out.println ("Started at " + new Date ());
		activeConnection [0] = false;
        if (port == 0) port = DEFAULT_PORT;
        this.port = port;
        try { listen_socket = new ServerSocket(port); }
        catch (IOException e) { fail(e, "Exception creating server socket"); }
        System.out.println("Server: listening on port " + port);
        this.start();
    }
    
    // The body of the server thread.  Loop forever, listening for and
    // accepting connections from clients.  For each connection, 
    // create a DBConnection object to handle communication through the
    // new Socket.
    public void run() {
        try {
            while(true)
			{
                Socket client_socket = listen_socket.accept();
                DBConnection c = new DBConnection (this, client_socket, semaphore);
            }
        }
        catch (IOException e) { 
            fail(e, "Exception while listening for connections");
        }
    }

    // Exit with an error message, when an exception occurs.
    protected static void fail(Exception e, String msg) {
        System.err.println(msg + ": " +  e);
        System.exit(1);
    }
}

class DBConnection extends Thread
{
	/**
	   A synchronized counter.  There is only one copy
	   for the entire DBConnection class, and we synchronize
	   the accesses to it.
	*/
	static class SyncCounter
	{
		public SyncCounter (int i)
		{
			counter = i;
		}
				
		public synchronized int next ()
		{
			return counter ++;
		}
		
		private int counter = 0;
	};

    protected Socket client;
    protected DataInputStream in;
    protected PrintStream out;
	protected Object semaphore;
	protected static SyncCounter nextConnectNum = new SyncCounter (1);
	protected int connectNum;
	protected Server server;

	private final static int WAIT_TIME = 60000;	// Time to wait for IO, in milliseconds

	/**
	   A timeout function for this DBConnection.  If the parent DBConnection
	   doesn't get a request from its counterpart before WAIT_TIME has
	   elapsed, we kill the DBConnection by interrupting it.

	   The same functionality is offered more succinctly by
	   the Java 1.1 Socket API, which includes setSoTimeout (),
	   but I didn't find out about that until after I'd written
	   this.
	 */
	private class IOTimeout extends Thread
	{
		public void run ()
		{
			try
			{
				DBConnection.this.join (WAIT_TIME);
				DBConnection.this.interrupt ();
			}
			catch (InterruptedException e)
			{
				// Our parent finished its operation before WAIT_TIME
			}
		}
	}
	

    public DBConnection (Server serv, Socket client_socket, Object sem)
	{
		server = serv;
		connectNum = nextConnectNum.next ();
		if (server.outputLevel >= Server.CONNECTION_OUTPUT)
			System.out.println ("\nDBConnection " + connectNum + " for host " +
							client_socket.getInetAddress ().getHostName () +
							" created at " + new Date ());

		semaphore = sem;
		client = client_socket;
		try
		{ 
			in = new DataInputStream(client.getInputStream());
			out = new PrintStream(client.getOutputStream());
		}
		catch (IOException e)
		{
			try { client.close(); } catch (IOException e2) { ; }
			System.err.println("Exception while getting socket streams: " + e);
			return;
		}
		this.start();
		if (server.outputLevel >= Server.CONNECTION_OUTPUT)
			System.out.println ("Exiting DBConnection constructor for " + connectNum);
    }
    
    // Read an SQL DML statement, execute it, and return the results.
	// The statement is preceded by the count of the number of fields expected to be returned.
	// Synchronize so that only one Thread is ever accessing an MIConnection at a time.
    public void run()
	{
        String line;
		if (server.outputLevel >= Server.CONNECTION_OUTPUT)
			System.out.println ("Just entered run for DBConnection " + connectNum);
		synchronized (semaphore)
		{
			if (server.outputLevel >= Server.CONNECTION_OUTPUT)
				System.out.println ("Just entered sync block for DBConnection " + connectNum);
			try
			{
				MIConnection DB = new MIConnection ("template1");
				Statement si = DB.createStatement ();
		
					/*
					  Read in a line of SQL, terminated by a ';'
					  The first character of the line is the separator character to be used
					  to separate the fields in the returned line.
					  The separator char is followed by an integer specifying the number of
					  fields expected.  The integer is followed by the separator character.
					  The actual SQL line follows the separator.

					  For now, as a security measure, I only permit SELECT statements.
					  Any statement which does not begin with that word (possibly preceded
					  by whitespace) will be rejected with an SQLException.  I also check
					  to make sure that there is only a single statement in the line,
					  and that the line ends with a ';'.

					  Example:  /3/select ID, project, object from webPages;
					*/
				IOTimeout watchDog = new IOTimeout ();
				watchDog.start ();
				line = in.readLine();
				watchDog.interrupt ();
				if (server.outputLevel >= Server.QUERY_OUTPUT)
					System.out.println (line);

				char separator = line.charAt (0);
				String sepString = String.valueOf (separator);
				int stmtBegin = line.substring (1).indexOf (separator) + 1;
				int fieldCount = new Integer (line.substring (1, stmtBegin)).intValue ();
				String SQLstmt = line.substring (stmtBegin + 1).trim ();

				// Handle special requests to the Server
				if (SQLstmt.equals ("NO_OUTPUT"))
				{
					server.outputLevel = Server.NO_OUTPUT;
					return;
				}
				else if (SQLstmt.equals ("CONNECTION_OUTPUT"))
				{
					server.outputLevel = Server.CONNECTION_OUTPUT;
					return;
				}
				else if (SQLstmt.equals ("QUERY_OUTPUT"))
				{
					server.outputLevel = Server.QUERY_OUTPUT;
					return;
				}
					
				// Check that the SQL statement is acceptable.
				String update = "update notifications set last_notified";
				if (! SQLstmt.regionMatches (true, 0, update, 0, update.length ()) && 
					! SQLstmt.regionMatches (true, 0, "select", 0, 6))
					throw new SQLException ("*** Not an acceptable statement type ***");
				if (SQLstmt.indexOf (';') != SQLstmt.length () - 1)
					throw new SQLException ("*** More than one statement or missing ';' ***");

				ResultSet r = si.executeQuery (SQLstmt);
				while (r.next ())
				{
					String result = r.getString (1);
					for (int i = 2; i <= fieldCount; i++)
					{
						result += sepString + r.getString (i);
					}
					if (server.outputLevel >= Server.QUERY_OUTPUT)
						System.out.println ("Sending " + result);
					out.println (result);
				}
				r.close ();
				DB.finalize ();
			}
			catch (InterruptedIOException e)
			{
				System.out.println ("**** Interrupted by watchdog timer ****");
				e.printStackTrace ();
			}
			catch (IOException e) { e.printStackTrace (); }
			catch (SQLException e) { e.printStackTrace (); }
			finally
			{
				out.close ();
				if (server.outputLevel >= Server.CONNECTION_OUTPUT)
					System.out.println ("About to close the client");
				try
				{
					client.close();
				}
				catch (IOException e2)
				{
					System.out.println ("Exception while closing the client");
				}
			}
			if (server.outputLevel >= Server.CONNECTION_OUTPUT)
				System.out.println ("Just left sync block for " + connectNum);
		}
    }
}
