// Simple server for SQL statements from an Illustra database.
// The server can only accept one connection at a time.
//	Usage:	java Server [portnum [username [password]]]
//		where username and password are a Unix id and password used to sign on to Informix
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

public class Server extends Thread
{
    public final static int DEFAULT_PORT = 0;

	protected String dbname;
    protected int port;
    protected ServerSocket listen_socket;
	protected String user;
	protected String password;

	// The amount of output to write to Server.out
	// (in order of increasing amount of output)
	public static final int NO_OUTPUT = 0;
	public static final int CONNECTION_OUTPUT = 1;
	public static final int QUERY_OUTPUT = 2;
	public int outputLevel = CONNECTION_OUTPUT;
	// Uncomment the following and comment the above to produce debugging output
	//public int outputLevel = QUERY_OUTPUT;

	// To ensure only 1 thread accesses milib at a time
	Object semaphore = new Object ();
	boolean activeConnection [] = new boolean [1];
    
    // Start the server up, listening on an optionally specified port
    public static void main(String[] args)
	{
		if (args.length >4)
		{
			System.err.println ("Usage:  java Server dbname portnum [username [password]]\n");
			System.exit (1);
		}

		String dbname = null;
		if (args.length >= 1)
			dbname = args[0];

		int port = 0;
		if (args.length >= 2)
            try { port = Integer.parseInt(args[1]);  }
            catch (NumberFormatException e) { port = 0; }


		String user = null;
		if (args.length >= 3)
			user = args [2];
		
		String password = null;
        if (args.length == 4)
			password = args [3];

        new Server(dbname, port, user, password);
    }

    // Create a ServerSocket on which to listen for connections;  start the thread.
    public Server(String dbname, int port, String user, String pass) {
		System.out.println ("\n\n\n------------------ Starting Java Illustra server -----------------");
		System.out.println ("Started at " + new Date ());
		activeConnection [0] = false;
        if (port == 0) port = DEFAULT_PORT;
		this.dbname = dbname;
        this.port = port;
		this.user = user;
		this.password = pass;
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
                DBConnection c = new DBConnection (this, client_socket, semaphore, dbname, user, password);
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
