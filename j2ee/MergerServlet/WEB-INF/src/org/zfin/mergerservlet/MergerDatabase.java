package org.zfin.mergerservlet;

import org.zfin.mergerservlet.*;
import javax.sql.*;
import java.sql.*;
import javax.servlet.http.*;
import java.util.*;


/**
 * Information about the database in which this code is doing the 
 * merge.  This class encapsulates all database access done when looking
 * for matching records in the database.
 */

public class MergerDatabase 
{

    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Metadata object describing the ZDB Object Types, tables and 
     * relationships in the database
     */
    private Metadata metadata;


    /** 
     * Connection to the database through which all db access happens.
     */
    private Connection dbConnection;




    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Create an object to encapsulate the database the merge is happening in.
     * Creates a database connection for SQL access.
     *
     * @param meta   Metadata about tables and relationships in the database
     */

    public MergerDatabase(Metadata meta)
	throws SQLException
    {
	metadata = meta;

	// open a connection
	dbConnection = metadata.openConnection();

	return;
    }



    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Get the metadata object describing tables and relationships.
     *
     * @return Metadata object for this database.
     */

    public Metadata getMetadata()
    {
	return metadata;
    }


    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
     * PUBLIC JDBC SQL ISOLATION METHODS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  
     * 
     * These methods exist here so that knowledge of JDBC SQL calls are 
     * isolated in one place.  You can also argue that since we use the 
     * metadata to generate most of our SQL, that this is a natural place
     * to put these methods.
     */


    /**
     * Given a ZDB ID, find the record for that ZDB ID in the database
     * and return a primary key for that record
     *
     * :TODO: If the ZDB ID does not exist in the database, then this
     *        routine behaves badly.
     *
     * @param zdbId ZDB ID to look up in the database and then build
     *              a primary key definition for.
     * @return Primary key definition for ZDB ID.
     */

    public KeyValue buildPrimaryKeyForZdbId(String zdbId)
	throws SQLException
    {
	// Determine the object type of the ZDB ID
	ZdbObjectType objType = metadata.getObjectTypeFromZdbId(zdbId);
	Table table = objType.getHomeTable();
	String zdbIdColumnName = objType.getZdbIdColumnName();

	// build primary key
	PreparedStatement stmt = dbConnection.prepareStatement(
		"select " + zdbIdColumnName + 
		"  from " + table.getName() +
		"  where " + zdbIdColumnName + " = ?;");
	stmt.setObject(1, zdbId);
	ResultSet zadRs = stmt.executeQuery();
	zadRs.next();
	Object value = zadRs.getObject(1);
	KeyValue primaryKeyValue = new KeyValue();
	primaryKeyValue.addColumnNameValuePair(this, zdbIdColumnName, value);
	return primaryKeyValue;
    }


    /** 
     * Given a ZDB ID, get the name associated with that ZDB ID in the
     * database.
     *
     * @param zdbId ZDB ID to get name of
     *
     * @return Name of ZDB ID in the database.
     */

    public String getNameFromZdbId(String zdbId)
	throws SQLException
    {
	Statement nameStmt = dbConnection.createStatement();
	ResultSet nameRs = 
	    nameStmt.executeQuery("select get_obj_name('" + zdbId + "') as obj_name" +
				  "  from single");
	nameRs.next();
	return nameRs.getString("obj_name");
    }


    /**
     * Given a matching record, with no dependent records, find that matching
     * record's dependent records and add them to the matching record.
     *
     * @param parentRecord Record to find dependent records for.  Dependent 
     *                     records are added to this object.
     */

    public void getDependentRecords(MatchedRecord parentRecord)
	throws SQLException
    {
	// for each foreign key that depends on the matched record
	Table parentTable = parentRecord.getTable();
	KeyValue parentPkValue = parentRecord.getPrimaryKeyValue();
	ExportedForeignKeyDefinitionList fkDefs = 
	    parentTable.getAllExportedForeignKeyDefs();
	Iterator /*<ForeignKeyDefinition>*/ fkDefIter = fkDefs.iterator();

	while (fkDefIter.hasNext()) {

	    // get dependent records for the current FK definition.
	    ForeignKeyDefinition childFkDef = 
		(ForeignKeyDefinition) fkDefIter.next();
	    Table childTable = childFkDef.getChildTable();

	    String selectString = 
		childFkDef.getSqlSelectPkWhereParameterizedFk();
	    PreparedStatement select = 
		dbConnection.prepareStatement(selectString);

	    // bind the column values from the parent primary key value to
	    // the foreign key condition in the query.
	    Iterator parentPkValueIter = parentPkValue.iterator();
	    int colNum = 1;
	    while (parentPkValueIter.hasNext()) {
		ColumnNameValuePair parentPkColumnNvp = 
		    (ColumnNameValuePair) parentPkValueIter.next();
		select.setObject(colNum, parentPkColumnNvp.getValue());
		colNum++;
	    }

	    ResultSet dependentRecords = select.executeQuery();
	    addResultSetAsDependentRecords(parentRecord, dependentRecords,
					   childTable);
	    // clean up
	    dependentRecords.close();
	    select.close();

	} // end while parentRecord still has FK defs to proccess

	return;
    }


    /* --------------------------------------------------------------------
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

    /** 
     * Given a result set, add the records in it to the dependent records list
     * of the matched record that was also passed in.
     *
     * @param parentRecord Record that records in result set are dependent on
     * @param rs           Result set containing records that are dependent
     *                     on the parent record.  These will be added to
     *                     the parent record as dependents, AND this routine
     *                     recursively gets all records that are dependent
     *                     on the dependents.
     * @param childTable   Table that dependent records in RS occur in.
     */

    private void addResultSetAsDependentRecords(MatchedRecord parentRecord,
						ResultSet rs,
						Table childTable)
	throws SQLException
    {
	MergerDatabase db = parentRecord.getDatabase();
	PrimaryKeyDefinition childPkDef = childTable.getPrimaryKeyDefinition();
	while (rs.next()) {
	    // build PK value for child record
	    KeyValue childPkValue = new KeyValue();
	    Iterator childPkColIter = childPkDef.iterator();
	    int colNum = 1;
	    while (childPkColIter.hasNext()) {
		String childPkColName = (String) childPkColIter.next();
		Object value = rs.getObject(colNum);
		childPkValue.addColumnNameValuePair(db, childPkColName, value);
		colNum++;
	    }
	    // :TODO: At this point we should build FK value that caused the 
	    //        record to match.
	    MatchedRecord childRecord = 
		new MatchedRecord(parentRecord, childTable, childPkValue);
	    parentRecord.addDependentRecord(childRecord);
	}
	return;
    }

}
