package org.zfin.mergerservlet;

import org.zfin.mergerservlet.*;
import javax.sql.*;
import java.sql.*;


/**
 * Information about the database in which this code is doing the 
 * merge.  This class was created when the number of thing we needed to
 * keep track of about a database reached two items: Metadata, and
 * connection.  Rather than pass around 2 things, we put them together.
 * There has to be a better name for this class.
 */

public class MergerDatabase 
{

    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Metadata object describing the tables and relationships in the database
     */
    Metadata metadata;

    /** 
     * Connection to the database through which all db access happens.
     */
    Connection dbConnection;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Create an object to describe the database the merge is happening in.
     *
     * @param meta   Metadata about tables and relationships in the database
     * @param dbConn Connection to the database.
     */

    public MergerDatabase(Metadata meta,
			  Connection dbConn)
    {
	metadata = meta;
	dbConnection = dbConn;

	return;
    }



    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Get the metadata object describing tables and relationships.
     */

    public Metadata getMetadata()
    {
	return metadata;
    }


    /**
     * Get the JDBC database connection 
     */
    
    public Connection getConnection()
    {
	return dbConnection;
    }

}
