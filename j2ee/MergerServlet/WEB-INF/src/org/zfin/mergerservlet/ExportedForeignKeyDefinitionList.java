package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;

/**
 * Defines a list of exported foreign key definitions.  Each foreign key 
 * definition lists the columns that exist in that foreign key.
 * 
 * The existence of the zdb_active_data and zdb_active_source tables in 
 * the ZFIN database sullies the purity of some lists.  Sometimes, the 
 * foreign keys in a list all point to the same parent table.  However,
 * sometimes they point to two tables, where one of the tables is 
 * zdb_active_data or zdb_active_source.
 *
 * Thought about changing the name of this class to be 
 * ForeignKeyDefinitionList.  It could then also be used to represent 
 * imported FK lists, should that need ever arise.  However, the constructor 
 * and addAll methods assume the list contains exported foreign keys.
 * If we ever need to support both exported and imported FK lists, then we
 * can use a common abstract class.
 */

class ExportedForeignKeyDefinitionList
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Parent table for the foreign key relationships.  This will never
     * point to zdb_active_data or zdb_active_source.  However, if this 
     * table (the parent table) has a PK that is an FK that points to 
     * zdb_active_data or zdb_active_source, then some of the entries in 
     * the list may point at zdb_active_data or zdb_active_source.
     */
    Table parentTable;

    /**
     * List of foreign key definitions exported from parentTable (and/or
     * zdb_active_data or zdb_active_source).
     */
    private ArrayList /*<ForeignKeyDefinition>*/ exportedForeignKeys;


    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /** 
     * Constructor.  Build a list of EXPORTED foreign keys for the given
     * table.  This method cannot be called until after the 
     * metadata table list contains all tables in the database.
     *
     * @param parentTbl Build a list of exported foreign keys for this table.
     *                  That is, create a list of foreign key relationships
     *                  that have this table as the parent table. 
     */

    public ExportedForeignKeyDefinitionList (Table parentTbl)
	throws SQLException
    {
	parentTable = parentTbl;
	exportedForeignKeys = new ArrayList /*<ForeignKeyDefinition>*/ ();
        parentTable.getMetadata().getExportedKeys(this);
	return;
    }


    /**
     * Copy constructor.  Does a shallow copy of the passed in list.  That is,
     * the references and lists of references are copied, but the objects
     * they reference are not.
     *
     * @param orig Exported foreign key definition list to make a shallow copy
     *             of.
     */

    public ExportedForeignKeyDefinitionList (ExportedForeignKeyDefinitionList orig)
    {
	this.parentTable = orig.parentTable;
	this.exportedForeignKeys = (ArrayList) orig.exportedForeignKeys.clone();
	return;
    }



    /* --------------------------------------------------------------------
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Return the parent table the foreign keys in the list are associated with.
     *
     * @return Table the foreign keys in the list are associated with.
     */

    public Table getParentTable()
    {
	return parentTable;
    }


    /**
     * Return string representation of foreign key definition list.  Useful
     * for debugging
     *
     * @return String representation of foriegn key definition list.
     */

    public String toString()
    {
	Iterator iter = exportedForeignKeys.iterator();
	String outString = "[ ";

	while (iter.hasNext()) {
	    ForeignKeyDefinition fkDef = (ForeignKeyDefinition) iter.next();
	    outString += fkDef.toString();
	}
	outString += " ]";

	return size() + ": " + outString;
    }



    /**
     * Given a table, remove all definitions from the list that 
     * have that table as the child table.
     *
     * @param childTable Table to to removed FK definitions for.
     */

    public void removeKeysForChildTable(Table childTable)
    {
	// iterate through all entries in the list looking for
	// child table.  Yeah, this is O(n), but n is always less 
	// than 100
	Iterator /*<ForeignKeyDefinition>*/ fkIter = this.iterator();
	while (fkIter.hasNext()) {
	    ForeignKeyDefinition fkDef = (ForeignKeyDefinition) fkIter.next();
	    if (fkDef.getChildTable() == childTable) {
		fkIter.remove();
	    }
	}
	return;
    }

	

    /**
     * Allocate and initialize a foreign key definition in the list.  The 
     * list of columns will be defined but empty.
     *
     * @param fkName     Name of the foreign key to allocate an entry for
     * @param childTable Table that contains the foreign key.
     *
     * @return The newly allocated and intialized foreign key definition.
     *         The column list will be empty.
     */
    public ForeignKeyDefinition addForeignKey(String fkName,
					      Table childTable)
    {
	ForeignKeyDefinition fkDef = 
	    new ForeignKeyDefinition(parentTable, childTable, fkName);
	exportedForeignKeys.add(fkDef);
	return fkDef;
    }



    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
     * COLLECTION METHODS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

    /**
     * Return number of entries in list
     *
     * @return number of foreign key definitions in list
     */

    public int size()
    {
	return exportedForeignKeys.size();
    }

    /**
     * Get an iterator for the list
     *
     * @return Iterator over list.
     */
    
    public Iterator iterator()
    {
	return exportedForeignKeys.iterator();
    }


    /**
     * Add all of the foreign key definitions in the list passed in
     * to this object.
     *
     * @param source List to copy foreign key definitions from
     */

    public void addAll(ExportedForeignKeyDefinitionList source)
    {
	exportedForeignKeys.addAll(source.exportedForeignKeys);
	return;
    }

	

}
