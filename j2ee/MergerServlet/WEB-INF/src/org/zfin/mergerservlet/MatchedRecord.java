package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;
import org.zfin.mergerservlet.*;


/**
 * Defines a record in the database that we have matched on, and all
 * of that record's dependent records, arranged as a tree of matched 
 * records.
 */


public class MatchedRecord
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /** 
     * Database that matched record and dependents exist in.  
     */
    MergerDatabase database;

    /**
     * Table the matched record occurs in 
     */
    private Table table;

    /** 
     * Primary key for this record.  This will be non-null if table has 
     * a primary key
     */
    private KeyValue primaryKeyValue;

    /** 
     * Parent matched record, if this record was matched as the result of
     * a foreign key relationship.  This will be null
     * for the root of the matched record tree.  For everything else, this will
     * have a value. 
     */
    private MatchedRecord parentRecord;

    /*
     * :TODO: Also need to store foreign key value that caused this record
     * to match.  This will allow us to update the foreign key when we
     * actually do the merge.
     */

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
	database = db;
	Metadata metadata = database.getMetadata();

	// get home table for ZDB ID
	ZdbObjectType objType = metadata.getObjectTypeFromZdbId(zdbId);
	table = objType.getHomeTable();

	// build PK for this ZDB ID
	primaryKeyValue = database.buildPrimaryKeyForZdbId(zdbId);

	// No parent record in case where ZDB ID is provided.
	parentRecord = null;

	// Gather dependent records as well
	// :TODO: Is automatic recursion a good thing?  Should we force a
	//        separate explicit call after construction to do this?
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
     * Show the matched record in HTML format.
     */

    public String show()
    {
	String thisRec = 
	    "<div style=\"padding-left: 2em;\">\n" +
	    primaryKeyValue.show() + "\n";

	// show each of the dependent records
	Iterator drIter = dependentRecords.iterator();
	String childRecs = "";
	while (drIter.hasNext()) {
	    MatchedRecord dr = (MatchedRecord) drIter.next();
	    childRecs += dr.show();
	}
	String footer = "</div>\n";
	return thisRec + childRecs + footer;
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
	dependentRecords = new ArrayList /*<MatchedRecord>*/ ();
	database.getDependentRecords(this);
	return;
    }

}


    
