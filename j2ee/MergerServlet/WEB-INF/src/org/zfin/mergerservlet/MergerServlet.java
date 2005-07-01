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
     * and saving it, and establishing a datasource for subsquent requests
     * to use.
     */

    public void init()
	throws ServletException
    {
	// Get a datasource and connection to the database.
	// The connection is used only by init method, but the datasource
	// is used to get connections for each request that comes in.
	try {
	    Context initContext = new InitialContext();
	    Context jdbcContext = (Context) initContext.lookup("java:comp/env");
	    DataSource dataSource = 
		(DataSource) jdbcContext.lookup("jdbc/zfinDatabase");
	    databaseName = getServletContext().getInitParameter("db_name");

	    // Gather metadata on current database.
	    metadata = new Metadata(dataSource, databaseName);
	}
	catch (Exception exception) {
	    throw new ServletException (exception);
	}

	return;
    }


    /* -------------------------------------------------------------------- 
     * PROTECTED METHODS
     * -------------------------------------------------------------------- */

    /**
     * Handle HTTP GET requests.
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

	// This code explicitly throws up header and footer.  Not sure
	// if it would be better to have the matched.jsp do that.

	// show header
	RequestDispatcher dispatcher =
	    request.getRequestDispatcher("/WEB-INF/header.jsp");
	dispatcher.include(request,response);

	PrintWriter out = response.getWriter();

	// Get Database object so we can get data.
	try {
	    // A MergerDatabase object encapsulates all the DB / SQL
	    // access that is done when looking for matching records.
	    MergerDatabase db = new MergerDatabase(metadata);

	    // get matching records for the provided ZDB ID
	    String zdbId = request.getParameter("zdbId");
	    MatchedRecord root = new MatchedRecord(db, zdbId);

	    // Show matched record.
	    // Add this to the request so JSP can pick it up.
	    // With the current implementation, this is just a (bad) proof
	    // of concept.  The matched.jsp does very little.
	    request.setAttribute("matchedRecord", root);
	    dispatcher = request.getRequestDispatcher("/WEB-INF/matched.jsp");
	    dispatcher.include(request,response);

	    // Done, show footer
	    dispatcher = request.getRequestDispatcher("/WEB-INF/footer.jsp");
	    dispatcher.include(request, response);
	}
	catch (Exception exception) {
	    throw new ServletException (exception);
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

}
