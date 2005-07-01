package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;
import org.zfin.mergerservlet.*;

/**
 * Metadata contains information about the target database.  This includes 
 * the tables, primary keys, and foreign keys that exist in the database,
 * as well as information needed to open a connection to the database.
 *
 * The encapsulates all knowledge of JDBC metadata routines.  It also 
 * provides connections to the database.  However, it does not encapsulate
 * JDBC/SQL access to the database when searching for data.  See MergerDatabase
 * for that.
 *
 * :TODO: This whole class could be (maybe?) converted to a static class, as 
 *        there is only a need for one metadata object at a time.  Once it
 *        is constructred, it never changes.
 */


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
     * DataSource for getting connections to the database.  The connection to
     * get the metadata info, and then subsequent connections to extract data,
     * all come from this DataSource.
     */
    private DataSource dataSource;


    /**
     * JDBC metadata object.  JDBC has a metadata API and all calls to that
     * API go through this object.  This is valid only during construction.
     * It is null after that.
     */
    private DatabaseMetaData jdbcMetadata;


    /** 
     * Name of database this metadata is for.  This is passed into the 
     * constructor.  This is called the "catalog" in JDBC metadata lingo.
     */
    private String databaseName;
    


    /**
     * Database connection used to get metadata information.
     * This connection is valid only during construction.
     * It is null after that.
     */
    private Connection dbConnection;



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
    private TreeMap /*<String,Table>*/ tables;



    /** 
     * List of ZDB Object Types occurring in the given database.  These
     * values are pulled from the ZDB_OBJECT_TYPE table.
     */
    private TreeMap /*<String,ZdbObjectType>*/ zdbObjectTypes;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructor.  Builds a Metadata object that is used to get
     * information about a database
     *
     * @param ds     JDBC/JNDI DataSource.  Connections to the database are
     *               obtained with this DataSource.
     * @param dbName Name of database that all connections will be to.  Metadata
     *               in this object will be about this database.
     */

    public Metadata (DataSource ds,
		     String dbName)
	throws SQLException
    {
	dataSource = ds;
	databaseName = dbName;
	dbConnection = openConnection();
	jdbcMetadata = dbConnection.getMetaData();

	ignoreTables = new HashSet /*<String>*/ ();
	ignoreTables.add("all_map_names");
	ignoreTables.add("all_name_ends");

	createTableList();
	createZdbObjectTypeList();

	// Now that all tables and all ZDB object types are known, modify 
	// table defs for object home tables to include FKs from zdb_active_data
	// and zdb_active_source
	inheritZdbActiveForeignKeys();

	// Meatadata now set in stone.  Release stuff
	jdbcMetadata = null;
	dbConnection.close();
	dbConnection = null;

	return;
    }


    /* --------------------------------------------------------------------
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */


    /**
     * Return list of tables in the database.
     */

    public Map /*<String,Table>*/ getTables()
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
     * These methods exist here so that knowledge of JDBC metadata calls are 
     * all isolated in one class.  
     *
     * You can view these as a callback mechanism. The objects that need the 
     * service that the method provides invoke the method, passing themselves 
     * as the input parameter.  The method then gets the desired information 
     * and passes it to the calling object through methods.
     */

    /**
     * Get the definition of the primary key for a table.
     *
     * @param pkDef  Primary key definition to populate.  This specifies
     *               which table the PK is for.
     */

    public void getPrimaryKeyForTable(PrimaryKeyDefinition pkDef)
	throws SQLException
    {
	Table table = pkDef.getTable();
	ResultSet pkRs = 
	    jdbcMetadata.getPrimaryKeys(databaseName, owner, table.getName());
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
     * @param fkDefs Empty foreign key definition list to put keys into.
     *               List specifies what table to get exported keys for.
     */

    public void getExportedKeys(ExportedForeignKeyDefinitionList fkDefs)
	throws SQLException
    {
	Table parentTable = fkDefs.getParentTable();
	ResultSet efkRs = 
	    jdbcMetadata.getExportedKeys(databaseName, owner, 
					 parentTable.getName());

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
     * isolated in one place.  These are standard JDBC calls that are 
     * independent of the JDBC metadata interface.
     */

    /** 
     * Open a database connection to the database.  Also set several 
     * standard session parameters that ZFIN uses.
     *
     * @return JDBC connection to the database
     */

    public Connection openConnection()
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
     * At the end of this method we know about each table in the database,
     * what the table's primary key definition is, and what the directly
     * exported foreign keys are for each table.
     */

    private void createTableList()
	throws SQLException
    {
	tables = new TreeMap /*<Table>*/ ();

	// Get tables and their primary keys into table list.  ignore views
	String tableTypes[] = new String[1];
	tableTypes[0] = "TABLE";
        ResultSet tableRs = 
	    jdbcMetadata.getTables(databaseName, owner, null, tableTypes);
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
	
	Statement zotStmt = dbConnection.createStatement();
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
     * Modify table defs for object home tables to include FKs from the
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
