package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;
import org.zfin.mergerservlet.*;

public class Metadata
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */


    /**
     * List of tables to ignore completely. These are mostly fast search tables.
     * :TODO: I would like to make this list a class variable instead of an
     *        instance variable, but I can't figure out how to initialize it.
     *        Doesn't matter that much since there is only one Metadata object.
     */
    private static HashSet /*<String>*/ ignoreTables;


    /**
     * JDBC metadata object.  All JDBC metadata calls use this object
     */

    private DatabaseMetaData jdbcMetadata;


    /** 
     * Name of database this metadata is for.  This is passed into the 
     * constructor.  This is called the "catalog" in JDBC metadata lingo.
     */
    private String dbName;
    

    /** 
     * Many JDBC metadata methods allow you to specify the owner of
     * a table to get information about it.  Use null, which tells JDBC that 
     * we don't care about table ownership.  This is called a "schema" in
     * JDBC metadata lingo.
     */
    final private String owner = null;

    /**
     * List of tables in the given database.
     */
    private TreeMap tables;


    /** 
     * List of ZDB Object Types occurring in the given database.  These
     * values are pulled from the ZDB_OBJECT_TYPE table.
     */
    private TreeMap /*<String,ZdbObjectType>*/ zdbObjectTypes;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructor.  Builds a Metadata object that can be used to get
     * information about the database the connection is using.
     *
     * @param connection Database connection to get metadata from
     * @param databaseName Name of database to get metadata from
     */

    public Metadata (Connection connection,
		     String databaseName)
	throws SQLException
    {
	jdbcMetadata = connection.getMetaData();
	dbName = databaseName;
	ignoreTables = new HashSet /*<String>*/ ();
	ignoreTables.add("all_map_names");
	ignoreTables.add("all_name_ends");

	createTableList();
	createZdbObjectTypeList();

	// Now that all tables and all ZDB object types are known, modify 
	// table defs for object home tables to include FKs from zdb_active_data
	// and zdb_active_source
	inheritZdbActiveForeignKeys();

    }


    /* --------------------------------------------------------------------
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */


    /**
     * Return list of tables in the database.
     */

    public Map getTables()
    {
	return tables;
    }



    /**
     * Given the name of a table, return the table object with that name
     *
     * @param tableName  Name of table to search for
     *
     * @return table object with that name.  If no such table exists, then
     *         returns null
     */

    public Table getTable(String tableName)
    {
	return (Table) tables.get(tableName);
    }



    /**
     * Return the Zdb Object Type list in HTML format.  Used for debugging
     * purposes.
     *
     * @return ZDB Object Type list in HTML format.
     */

    public String zotsToHtml()
    {
	String zotsHtml = "";
	Collection /*<ZdbObjectType>*/ zots = zdbObjectTypes.values();
	Iterator zotIter = zots.iterator();
	while (zotIter.hasNext()) {
	    ZdbObjectType zot = (ZdbObjectType) zotIter.next();
	    zotsHtml += zot.toHtml();
	}
	return zotsHtml;
    }


    /**
     * Given a ZDB ID, return the ZDB object type of the ID.
     *
     * Note: The code in this method that scans the ZDB ID string looking 
     *       for the object type string is duplicated in the get_obj_type 
     *       C function in the database.
     *
     * @param zdbId   ZDB ID to return the object type for.
     *
     * @return ZDB Object Type of the ID.  If the ID is improperly
     *         formatted, or contains 
     */

    public ZdbObjectType getObjectTypeFromZdbId(String zdbId)
    {
	ZdbObjectType objType = null;

	// Start at leading "ZDB-" and go to next "-"
	if (zdbId.startsWith("ZDB-")) {
	    int objTokenEnd = zdbId.indexOf("-", 4);
	    String objName = zdbId.substring(4, objTokenEnd);
	    objType = (ZdbObjectType) zdbObjectTypes.get(objName);
	}
	return objType;
    }



    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
     * PUBLIC JDBC METADATA ISOLATION METHODS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  
     * 
     * I am not sure what to call this type of method.  These methods exist
     * here so that knowledge of JDBC metadata calls are all isolated in one
     * place.  If that was not a goal, then they could have been dispersed in
     * other object files instead.
     *
     * You can also view these as a callback mechanism.
     * The objects that need the service that the method provides invoke the
     * method, passing themselves as the input parameter.  The method then 
     * gets the desired information and passes it to the calling object 
     * through methods.
     */

    /**
     * Get the definition of the primary key for a table.
     *
     * @param pkDef  Primary key definition to populate.
     */

    public void getPrimaryKeyForTable(PrimaryKeyDefinition pkDef)
	throws SQLException
    {
	Table table = pkDef.getTable();
	ResultSet pkRs = 
	    jdbcMetadata.getPrimaryKeys(dbName, owner, table.getName());
	while (pkRs.next()) {
	    String colName = pkRs.getString("COLUMN_NAME");
	    int colNum = pkRs.getInt("KEY_SEQ") - 1;
	    pkDef.addColumn(colNum, colName);
	}
	return;
    }


    /**
     * Get the list of exported keys for a table.  Exported keys are foreign
     * keys in other tables that point back to this table.
     *
     * @param fkDefs Empty foreign key definition list to put keys into
     */

    public void getExportedKeys(ExportedForeignKeyDefinitionList fkDefs)
	throws SQLException
    {
	Table parentTable = fkDefs.getParentTable();
	ResultSet efkRs = 
	    jdbcMetadata.getExportedKeys(dbName, owner, parentTable.getName());

	// The above call gets all the columns in all the foreign keys that 
	// point back to this table.  Now we have detect when each foreign key 
	// starts and ends in the result set, and create an FK for each one.  
	// Determine this by when the FK name changes 

	String prevFkName = "";
	ForeignKeyDefinition fkDef = null;

	while (efkRs.next()) {
	    String fkName = efkRs.getString("FK_NAME");
	    String fkTableName = efkRs.getString("FKTABLE_NAME");
	    String fkColumnName = efkRs.getString("FKCOLUMN_NAME");
	    String pkColumnName = efkRs.getString("PKCOLUMN_NAME");

	    if (! ignoreTables.contains(fkTableName)) {
		if (! fkName.equals(prevFkName)) {
		    Table childTable = (Table) tables.get(fkTableName);
		    fkDef = fkDefs.addForeignKey(fkName, childTable);
		    prevFkName = fkName;
		}
		fkDef.addColumn(pkColumnName, fkColumnName);
	    }
	}
	return;
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
     * Given a matching record, with no dependent records, find that matching
     * record's dependent records and add them to the matching record.
     *
     * @param parentRecord Record to find dependent records for.  Dependent 
     *                     records are added to this object.
     */

    public void getDependentRecords(MatchedRecord parentRecord)
	throws SQLException
    {
	Connection conn = parentRecord.getDatabase().getConnection();

	// for each foreign key that depends on the matched record
	Table parentTable = parentRecord.getTable();
	KeyValue parentPkValue = parentRecord.getPrimaryKeyValue();
	ExportedForeignKeyDefinitionList fkDefs = 
	    parentTable.getAllExportedForeignKeyDefs();
	Iterator fkDefIter = fkDefs.iterator();

	while (fkDefIter.hasNext()) {

	    // get dependent records for the current FK definition.
	    ForeignKeyDefinition childFkDef = 
		(ForeignKeyDefinition) fkDefIter.next();
	    Table childTable = childFkDef.getChildTable();
	    String childFkCondition = childFkDef.getParameterizedSqlCondition();
	    String childPkCommaList = 
		childTable.getCommaSeparatedPkColumnNames();

	    // :TODO:  This whole select string could be generated within the
	    // foreign key definition.  Think about that.
	    String selectString = 
		"select " + childPkCommaList +
		"  from " + childTable.getName() +
		"  where " + childFkCondition;
	    PreparedStatement select = conn.prepareStatement(selectString);

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


    /**
     * Given a ResultSet and a column name, return the value of that column
     * name in the current row of the result set.
     *
     * @param dbRow      The current row in this ResultSet is searched for the 
     *                   column name.
     * @param columnName Name of column to get the value for.
     *
     * @return The value of the column as an object.  Null if the column does
     *         not exist in the current result set entry.
     */

    public Object resultSetGetColumn(ResultSet dbRow,
				     String columnName)
	throws SQLException
    {
	return dbRow.getObject(columnName);
    }



    /* --------------------------------------------------------------------
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */


    /**
     * Build a complete list of tables that exist in the current database.
     */

    private void createTableList()
	throws SQLException
    {
	tables = new TreeMap /*<Table>*/ ();

	// Get tables and their primary keys into table list.  ignore views
	String tableTypes[] = new String[1];
	tableTypes[0] = "TABLE";
        ResultSet tableRs = 
	    jdbcMetadata.getTables(dbName, owner, null, tableTypes);
	while (tableRs.next()) {
	    String tableName = tableRs.getString("TABLE_NAME");
	    if (! ignoreTables.contains(tableName)) {
		Table table = new Table(this, tableName);
		tables.put(tableName, table);
	    }
	}
	// now that we have all the tables in the list, get the exported keys
	// for each table
	Set /*<String>*/ tableNames = tables.keySet();
	Iterator tableIter = tableNames.iterator();
	while (tableIter.hasNext()) {
	    String tableName = (String) tableIter.next();
	    Table table = (Table) tables.get(tableName);
	    table.setExportedForeignKeyDefs();
	}
    }


    /**
     * Build a complete list of ZDB Object Types that are defined in the
     * database.
     */

    private void createZdbObjectTypeList()
	throws SQLException
    {
	zdbObjectTypes = new TreeMap /*<ZdbObjectType>*/ ();
	
	Statement zotStmt = jdbcMetadata.getConnection().createStatement();
	ResultSet zotRs = 
	    zotStmt.executeQuery("select *" +
				 "  from zdb_object_type");
	while (zotRs.next()) {
	    ZdbObjectType zot = new ZdbObjectType(this, zotRs);
	    zdbObjectTypes.put(zot.getName(), zot);
	}
	zotRs.close();
	zotStmt.close();
	return;
    }



    /** 
     * Given a result set, add the records in it to the dependent records list
     * of the matched record that was also passed in.
     *
     */

    private void addResultSetAsDependentRecords(MatchedRecord parentRecord,
						ResultSet rs,
						Table childTable)
	throws SQLException
    {
	PrimaryKeyDefinition childPkDef = childTable.getPrimaryKeyDefinition();
	while (rs.next()) {
	    // build PK value for child record
	    KeyValue childPkValue = new KeyValue();
	    Iterator childPkColIter = childPkDef.iterator();
	    int colNum = 1;
	    while (childPkColIter.hasNext()) {
		String childPkColName = (String) childPkColIter.next();
		Object value = rs.getObject(colNum);
		childPkValue.addColumnNameValuePair(childPkColName, value);
		colNum++;
	    }
	    // :TODO: At this point we should build FK value that caused the 
	    //        record to match.  However, I am not sure we need it
	    MatchedRecord childRecord = 
		new MatchedRecord(parentRecord, childTable, childPkValue);
	    parentRecord.addDependentRecord(childRecord);
	}
	return;
    }
		


    /**
     * Modify tables defs for object home tables to include FKs from the
     * ZDB_ACTIVE_* tables.  All tables and all ZDB object types must be
     * defined before calling this method.
     */

    public void inheritZdbActiveForeignKeys()
    {
	Collection /*<Table>*/ tableColl = tables.values();
	Iterator tableIter = tableColl.iterator();
	while (tableIter.hasNext()) {
	    Table table = (Table) tableIter.next();
	    table.setAllExportedForeignKeyDefs();
	}
	return;
    }
	    

}
