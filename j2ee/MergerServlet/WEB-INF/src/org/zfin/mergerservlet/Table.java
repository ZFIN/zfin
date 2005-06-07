package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;

/**
 * Defines a database table.  Visible only within package.
 */

public class Table implements Comparable
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Metadata object that for the database this table is in.  
     */
    final private Metadata metadata;

    /**
     * Name of the table.  Don't save the owner name. 
     */
    private String name;

    /** 
     * Primary key of the table.  A list of columns that uniquely identify
     * records in the table.
     */
    private PrimaryKeyDefinition primaryKeyDef;

    /** 
     * List of foreign keys in other tables that point at this table. 
     * If this is null, that indicates we haven't gathered the foreign
     * key definitions for the table yet.  If this exists, but is 
     * empty that means the table has no foreign keys.
     */
    private ExportedForeignKeyDefinitionList exportedForeignKeyDefs; 

    /**
     * List of foreign keys in other tables that point back to this table
     * (i.e., the contents of exportedForeignKeyDefs),
     * 
     *  PLUS
     *
     * if this table is the home table for any ZDB object types that 
     * reside in zdb_active_data or zdb_active_source, then this list
     * also contains foreign keys that are exported by those tables as
     * well.
     *
     * This list exists, in addition to exportedForeignKeyDefs, because
     * when we match a record in, say, the marker table, we want to find
     * all records that depend on that marker record, PLUS, all records
     * that depend on the corresponding zdb_active_data record as well.
     *
     * If this is null, that indicates we haven't yet combined this
     * table's FKs with the FKs of the zdb_active_* tables.  If this exists, 
     * but is empty that means the table has no foreign keys from either 
     * source.
     */
    private ExportedForeignKeyDefinitionList allExportedForeignKeyDefs; 

    /**
     * List of ZDB Object Types that call this table home.  Each object type
     * has one and only one home table.
     */
    private TreeMap /*<String,ZdbObjectType>*/ homeForObjectTypes;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /** 
     * Constructor, given the metadata object for the database the table is in,
     * and the table name.  Table object only describes the parts of the table
     * definition that we care about.
     *
     * @param meta      Metadata object for the database the table exists in.
     * @param tableName Name of the table to create a Table object for.
     */
    public Table (Metadata meta,
		  String tableName)
	throws SQLException
    {
	name = tableName;
	metadata = meta;
	primaryKeyDef = new PrimaryKeyDefinition(this);
	exportedForeignKeyDefs = null;
	allExportedForeignKeyDefs = null;
	homeForObjectTypes = new TreeMap /*<String,ZdbObjectType>*/ ();

	return;
    }



    /* --------------------------------------------------------------------
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    public int compareTo (Object otherTable)
    {
	Table other = (Table) otherTable;
	return name.compareTo(other.getName());
    }

    public String toString()
    {
	return name + " " + primaryKeyDef.toString() + "\n  FK: " +
	    exportedForeignKeyDefs.toString() +
	    "\n  All FK: " + allExportedForeignKeyDefs.toString() + "\n";
    }
	
    public String getName()
    {
	return name;
    }

    public Metadata getMetadata()
    {
	return metadata;
    }

    public ExportedForeignKeyDefinitionList getExportedForeignKeyDefs()
    {
	return exportedForeignKeyDefs;
    }

    public ExportedForeignKeyDefinitionList getAllExportedForeignKeyDefs()
    {
	return allExportedForeignKeyDefs;
    }


    /** 
     * Gathers all of the foreign key definitions in the database that 
     * point back to this table.  This method can not be called until 
     * after all the tables are defined in this table's metadata object.
     */

    public void setExportedForeignKeyDefs()
	throws SQLException
    {
	exportedForeignKeyDefs = new ExportedForeignKeyDefinitionList(this);
	return;
    }


    /**
     * gathers the foreign key definition of keys exported from this table,
     * PLUS, if this table is the home table for any ZDB object types that 
     * reside in zdb_active_data or zdb_active_source, then this list
     * also contains foreign keys that are exported by those tables as
     * well.
     *
     * This must be called after setExportedForeignKeyDefs() has been called
     * and after the homeForObjectTypes list has been fully populated.
     */
    public void setAllExportedForeignKeyDefs()
    {
	// start with FKs exported by this table
	allExportedForeignKeyDefs = 
	    new ExportedForeignKeyDefinitionList(exportedForeignKeyDefs);

	// walk through each object that calls this table home.
	// For any object where the ZDB IDs also occur in a ZDB_ACTIVE_*
	// table, then add the FKs' for that table.
	boolean isData = false;
	boolean isSource = false;
	Collection /*<ZdbObjectType>*/ zots = homeForObjectTypes.values();
	Iterator zotIter = zots.iterator();
	while (zotIter.hasNext() && !isData && !isSource) {
	    ZdbObjectType zot = (ZdbObjectType) zotIter.next();
	    if (!isData && zot.isData()) {
		isData = true;
		Table zdbActiveData = metadata.getTable("zdb_active_data");
		allExportedForeignKeyDefs.addAll(zdbActiveData.exportedForeignKeyDefs);
		// remove FK that points to this table.
		allExportedForeignKeyDefs.removeKeysForChildTable(this);
	    }
	    if (!isSource && zot.isSource()) {
		isSource = true;
		Table zdbActiveSource = metadata.getTable("zdb_active_source");
		allExportedForeignKeyDefs.addAll(zdbActiveSource.exportedForeignKeyDefs);
		// remove FK that points to this table.
		allExportedForeignKeyDefs.removeKeysForChildTable(this);
	    }
	}

	return;
    }


    public String getCommaSeparatedPkColumnNames()
    {
	return primaryKeyDef.getCommaSeparatedColumnNames();
    }


    public PrimaryKeyDefinition getPrimaryKeyDefinition()
    {
	return primaryKeyDef;
    }

    /** 
     * Adds a ZDB Object Type to this list of object types that call this
     * table their home table.
     */
    public void addZdbObjectType(ZdbObjectType zot)
    {
	homeForObjectTypes.put(zot.getName(), zot);
	return;
    }



    /* --------------------------------------------------------------------
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

}
