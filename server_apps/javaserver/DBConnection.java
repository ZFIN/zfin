// DBConnection class for the javaserver.
// This used to be part of Server.java but was split into its own file.
//
// Modified version of example 7-5 of _Java in a Nutshell_ by David Flanagan.
// Written by David Flanagan.  Copyright (c) 1996 O'Reilly & Associates.
// You may study, use, modify, and distribute this example for any purpose.
// This example is provided WITHOUT WARRANTY either expressed or implied.
//
// Modified by Ted Kirkpatrick for the ZFIN project.
//
// $Id$
// $Source$

import java.io.*;
import java.net.*;
import java.util.Date;

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
	protected String dbname;
	protected String user;
	protected String password;

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
	

    public DBConnection (Server serv, Socket client_socket, Object sem, String dbname, String user, String password)
	{
		server = serv;
		this.dbname = dbname;
		this.user = user;
		this.password = password;
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
				System.out.println ("Connecting to " + dbname);
				MIConnection DB = new MIConnection (dbname, user, password);
				Statement si = DB.createStatement ();
		
					/*
					  Read in a line of SQL, terminated by a ';'
					  The first character of the line is the separator character to be used
					  to separate the fields in the returned line.
					  The separator char is followed by an integer specifying the number of
					  fields expected.  The integer is followed by the separator character.
					  The actual SQL line follows the separator.

					  For now, as a security measure, I only permit SELECT statements and
					  the following restricted data-modification statements:
					      UPDATE NOTIFICATIONS (which is only used for debugging)
						  INSERT INTO INT_PERSON_PUB
						  DELETE FROM INT_PERSON_PUB
					  
					  Any statement which does not begin with those words (possibly preceded
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
				String insert = "insert into int_person_pub";
				String delete = "delete from int_person_pub";
				if (! SQLstmt.regionMatches (true, 0, update, 0, update.length ()) &&
					! SQLstmt.regionMatches (true, 0, insert, 0, insert.length ()) &&
					! SQLstmt.regionMatches (true, 0, delete, 0, delete.length ()) &&
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
