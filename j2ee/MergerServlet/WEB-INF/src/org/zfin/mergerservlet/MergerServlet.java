package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;
import org.zfin.mergerservlet.*;
import javax.servlet.*;
import javax.servlet.http.*;
import com.informix.jdbc.*;
import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import java.io.*;

/**
 * Controlling servlet for Merger app
 */


public class MergerServlet extends HttpServlet
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */


    /* :TODO: Not sure if these will just be for the servlet initialization,
     * or if they will be reused with each call.  Probably can't be reused with
     * each call, as servlet can handle multiple, simultaneous calls, in which
     * case these can be moved into the init routine.
     */

    private Context initContext;
    private Context jdbcContext;

    /** JDBC data source.  Database connections are obtained with this 
     * data source.  Note that the servlet does not maintain an open connection
     * itself between invocations.
     */
    private DataSource dataSource;

    /** 
     * Database name.  All connections within this servlet use the same
     * database.
     */
    private String databaseName;

    /**
     * ZFIN Metadata object.  It encapsulates all knowledge about the ZFIN 
     * data dictionary, and about JDBC metadata.  The metadata is populated
     * when the servlet is first initialized, and is not updated after that.
     * Therefore, if the data dictionary in the database changes, then the
     * servlet will need to be restarted.
     */
    public Metadata metadata;


    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */
    
    // Use defualt constructor.  Interesting work done by init method.

    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    // all of these methods override method

    /**
     * Initialize the servlet.  This consists of reading the data dictionary
     * and saving it.
     */

    public void init()
	throws ServletException
    {
	// I think it is worth the effort to catch each excpetion 
	// individually.  Unfortunately, this renders this method
	// unreadable.

	// Get a datasource and connection to the database.
	// The connection is used only by init method, but the datasource
	// is used to get connections for each request that comes in.
	try {
	    initContext = new InitialContext();
	}
	catch (NamingException nameException) {
	    throw new ServletException ("Failed to get initial context.",
					nameException);
	}
	try {
	    jdbcContext = (Context) initContext.lookup("java:comp/env");
	}
	catch (NamingException nameException) {
	    throw new ServletException ("Failed to get JDBC env context.",
					nameException);
	}
	try {
	    dataSource = (DataSource) jdbcContext.lookup("jdbc/zfinDatabase");
	}
	catch (NamingException nameException) {
	    throw new ServletException ("Failed to lookup jdbc/zfinDatabase.",
					nameException);
	}
	Connection dbConn;
	databaseName = getServletContext().getInitParameter("db_name");
	try {
	    dbConn = openConnection();
	}
	catch (SQLException sqlException) {
	    throw new ServletException ("Database Connection open failed.",
					sqlException);
	}

	// Gather metadata on current database.
	try {
	    metadata = new Metadata(dbConn, databaseName);
	}
	catch (SQLException sqlException) {
	    throw new ServletException ("Failed to gather metadata.",
					sqlException);
	}

	try {
	    dbConn.close();  // done with connection, hang on to dataSource.        						  
	}
	catch (SQLException sqlException) {
	    throw new ServletException ("Failed to close database connect.",
					sqlException);
	}

	return;
    }


    /**
     * Destroy the servlet.  This frees the resources allocated by init.
     */

    public void destroy()
    {
	try {
	    jdbcContext.close();
	    initContext.close();
	}
	catch (NamingException closeException) {
	    // This method can't throw an exception, so the best we can
	    // do is write an error to the log
	    System.out.println("MergerServlet got an exception when closing\n" +
			       "  contexts.\n" + closeException);
	}
	return;
    }


    /* -------------------------------------------------------------------- 
     * PROTECTED METHODS
     * -------------------------------------------------------------------- */

    /**
     * Handle HTTP GET requests.  I expect the only GET request that will 
     * come in is the initial one that results in an empty form being thrown
     * up.
     *
     * @param request  An HttpServletRequest object that contains the request 
     *                 the client has made of the servlet
     * @param response An HttpServletResponse object that contains the response 
     *                 the servlet sends to the client.
     */

    protected void doGet(HttpServletRequest request,
			 HttpServletResponse response)
	throws ServletException,
	       java.io.IOException
    {
	response.setContentType("text/html");

	// show header
	RequestDispatcher dispatcher =
	    request.getRequestDispatcher("/WEB-INF/header.jsp");
	dispatcher.include(request,response);

	PrintWriter out = response.getWriter();

	// Get Database object so we can get data.
	Connection dbConn;
	try {
	    dbConn = openConnection();
	}
	catch (SQLException sqlException) {
	    throw new ServletException ("Database Connection open failed.",
					sqlException);
	}
	MergerDatabase db = new MergerDatabase(metadata, dbConn);

	// get matching records for the provided ZDB ID
	String dataZdbId = request.getParameter("dataZdbId");
	MatchedRecord root;
	try {
	    root = new MatchedRecord(db, dataZdbId);
	}
	catch (SQLException sqlException) {
	    throw new ServletException ("Failed to create matched record.",
					sqlException);
	}

	// Show matched record.
	out.println(root.toHtml());

	// Done, show footer
	dispatcher = request.getRequestDispatcher("/WEB-INF/footer.jsp");
	dispatcher.include(request, response);

	try {
	    dbConn.close();
	}
	catch (SQLException sqlException) {
	    throw new ServletException ("Failed to close connection.", 
					sqlException);
	}

	return;
    }


    /**
     * Handle HTTP POST requests.  Most of the requests this servlet will
     * get will be POST requests.
     *
     * @param request  An HttpServletRequest object that contains the request 
     *                 the client has made of the servlet
     * @param response An HttpServletResponse object that contains the response 
     *                 the servlet sends to the client.
     */

    protected void doPost(HttpServletRequest request,
			  HttpServletResponse response)
	throws ServletException,
	       java.io.IOException
    {
	return;
    }



    /* -------------------------------------------------------------------- 
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

    /** 
     * Open a database connection to the database.
     *
     * @return JDBC connectoin to the database
     */
    private Connection openConnection()
	throws SQLException
    {
	Connection conn = dataSource.getConnection();
	Statement dbStmt = conn.createStatement();
	dbStmt.executeUpdate("database " + databaseName);
	dbStmt.close();

	// Force callers to do explicit commits. The setAutoCommit method
	// cannot be called until after you established the database, b/c
	// until then you don't know if the database supports transactions.
       	conn.setAutoCommit(false);

	Statement setParams = conn.createStatement();
	setParams.executeUpdate("execute procedure set_session_params()");
	setParams.close();

	return conn;
    }
}

    
