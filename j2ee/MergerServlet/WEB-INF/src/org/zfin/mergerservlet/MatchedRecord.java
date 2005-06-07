package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;
import org.zfin.mergerservlet.*;


/**
 * Define a class to represent actual records in the database that we have
 * matched on.  these form a tree of matching records.
 */


public class MatchedRecord
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /** 
     * Database that matched record exists in.  
     */
    MergerDatabase database;

    /**
     * Table the matched record occurs in 
     */
    private Table table;

    /** 
     * Primary key for this record.  This will be non-null if table has 
     * a primay key
     */
    private KeyValue primaryKeyValue;

    /** 
     * Parent matched record, if this record was matched as the result of
     * a foreign key relationship.  This will be null
     * for the root of the matched record tree.  For everything else, this will
     * have a value. 
     *
     * :TODO: Might also want to store the foreign key that caused this record
     * to match?
     */
    private MatchedRecord parentRecord;

    /**
     * List of dependent matched records.  In other words, the list of records
     * that have a foreign key pointing to this record.  If this list is 
     * null then we have not yet checked for the existence of dependent records.
     * If the list exists but is empty then we have checked for the existence
     * of dependent records, but none existed.
     */
    private ArrayList /*<MatchedRecord>*/ dependentRecords;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Build a matched record given the ZDB ID of the record
     *
     * @param db     Database to search for record.
     * @param zdbId  ZDB ID value to look up.
     */

    public MatchedRecord(MergerDatabase db,
			 String zdbId)
	throws SQLException
    {
	// :TODO: Move the JDBC calls out of this object.

	database = db;
	Connection conn = database.getConnection();
	Metadata meta = database.getMetadata();

	// Determine the object type of the ZDB ID
	ZdbObjectType objType = meta.getObjectTypeFromZdbId(zdbId);
	table = objType.getHomeTable();
	String zdbIdColumnName = objType.getZdbIdColumnName();

	// build primary key
	PreparedStatement stmt = 
	    conn.prepareStatement(
				  "select " + zdbIdColumnName +
				  "  from " + table.getName() +
				  "  where " + zdbIdColumnName + " = ?;");
	stmt.setObject(1, zdbId);
	ResultSet zadRs = stmt.executeQuery();
	zadRs.next();
	Object value = zadRs.getObject(1);
	primaryKeyValue = new KeyValue();
	primaryKeyValue.addColumnNameValuePair(zdbIdColumnName, value);

	// No parent record in case where ZDB ID is provided.
	parentRecord = null;

	// Gather dependent records as well
	// :TODO: Is automatic recursion a good thing?  Should we force a
	//        separate explicit call after construction to do this?
	dependentRecords = new ArrayList /*<MatchedRecord>*/ ();
	gatherDependentRecords();
	
	return;
    }



    /** Constructor, given only the table and primary key value
     * 
     * @param db      Database that matched record exists in.
     * @param tbl     Table primary key value is from
     * @param pkValue Primary key value 
     */

    public MatchedRecord (MergerDatabase db,
			  Table tbl,
			  KeyValue pkValue)
	throws SQLException
    {
	database = db;
	table = tbl;
	primaryKeyValue = pkValue;
	parentRecord = null;

	// Gather dependent records as well
	// :TODO: Is automatic recursion a good thing?  Should we force a
	//        separate explicit call after construction to do this?
	dependentRecords = new ArrayList /*<MatchedRecord>*/ ();
	gatherDependentRecords();

	return;
    }

    /** 
     * Constructor, given the parent record, and table and primary key value 
     * of the dependent record.
     *
     * @param parentRecord Parent record of the object to create.
     * @param tbl          Table primary key value value is from
     * @param pkValue      Primary key of the matching record.
     */

    public MatchedRecord (MatchedRecord parentRecord,
			  Table tbl,
			  KeyValue pkValue)
	throws SQLException
    {
	database = parentRecord.getDatabase();
	table = tbl;
	primaryKeyValue = pkValue;
	dependentRecords = new ArrayList /*<MatchedRecord>*/ ();
	gatherDependentRecords();

	return;
    }


    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */



    /**
     * Get database of matching record
     */

    public MergerDatabase getDatabase()
    {
	return database;
    }


    /**
     * Get the primary key value of the matched record.
     */

    public KeyValue getPrimaryKeyValue()
    {
	return primaryKeyValue;
    }


    /**
     * Return Table matched record is in.
     */

    public Table getTable()
    {
	return table;
    }


    /**
     * Convert the matched record to a string for debugging purposes
     *
     * @return Name of the table, followed by PK value.
     */

    public String toString()
    {
	return table.getName() + ":" + primaryKeyValue.toString() + "," +
	    dependentRecords.toString();
    }


    /**
     * Convert the matched record to HTML.
     * :TODO:  Not sure if this will be just for debugging, or if this 
     *         will eventually be used in the interface.  Seems unlikely 
     *         to be used in the interface, as that is what JSP is for.
     *
     * @return String with HTML reprentation of matched record.
     */

    public String toHtml()
    {
	String header = 
	    "<div style=\"padding-left: 2em; " +
	    "border-width: 2px; border-color: blue; border-style: groove\">\n" +
	    "<br><b>Matched Record</b>:\n" +
	    "<br>Table: " + table.getName() + "\n";

	String pkString = "<br>Primary Key Value:" + primaryKeyValue.toHtml();

	Iterator drIter = dependentRecords.iterator();
	String drString = "";
	while (drIter.hasNext()) {
	    MatchedRecord dr = (MatchedRecord) drIter.next();
	    drString += dr.toHtml();
	}
	String footer = "</div>\n";

	return header + pkString + drString + footer;
    }




    /**
     * Add a record to this record's dependent records list.
     *
     * @param childRecord Record that depends on this records primary key.
     */

    public void addDependentRecord(MatchedRecord childRecord)
    {
	dependentRecords.add(childRecord);
	return;
    }



    /* -------------------------------------------------------------------- 
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

    /**
     * Given a matched record, find all the records that depend on it through
     * foreign key relationships.
     */

    private void gatherDependentRecords()
	throws SQLException
    {
	database.getMetadata().getDependentRecords(this);
	return;
    }

}


    
