package org.zfin.mergerservlet;

import java.util.*;


/**
 * A foreign key definition, which is distinct from a foreign key value.
 * This specifies the 2 tables involved in the foreign key relationship, and
 * the columns in both tables that are participating in the foreign key.
 */

class ForeignKeyDefinition implements Comparable
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /** 
     * Parent table.  This is the table containing the primary key that is used
     * by the foriegn key. 
     */
    private Table parentTable;

    /** 
     * Child table.  Table containing the actual foreign key. 
     */
    private Table childTable;


    /** 
     * Name of the foriegn key relationship.  All such relationships are 
     * named in Informix, and they are usually explicitly named by ZFIN 
     */
    private String name;

    /** 
     * List of columns in the foreign key.  These columns occur in the child 
     * table.  They occur in the list in the order they occur in the foreign 
     * key. 
     */
    private ArrayList /*<ForeignKeyColumn>*/ foreignKeyColumns;


    /**
     * Parameterized SQL select statement to get PK with a paramterized
     * FK condition.  Storing this is an optimization.  It could be 
     * generated on the fly every time.
     */
    private String sqlSelectPkWhereParameterizedFk;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructor, given the parent and child tables and a name of
     * the foreign key.  This intialized the foreign key column list, but
     * leaves it empty.
     *
     * @param parentTbl   Parent/exporting table in the foreign key relationship
     * @param childTbl    Child/importing table in the foreign key relationship.
     *                    This table has the foreign key in it.
     * @param fkName      Name of the foreign key relationship.
     *                    key in the child table.  
     *
     */

    public ForeignKeyDefinition (Table parentTbl,
				 Table childTbl,
				 String fkName)
    {
	parentTable = parentTbl;
	childTable = childTbl;
	name = fkName;
	foreignKeyColumns = new ArrayList /*<ForeignKeyColumn>*/ ();
	sqlSelectPkWhereParameterizedFk = null; /* instantiate when needed */
    }


    /* --------------------------------------------------------------------
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Return a string representation of the foreign key definition.
     *
     * @return String representation of the foreign key definition.
     */

    public String toString()
    {
	return "FK: " + 
	    childTable.getName() + " (child) to " +
	    parentTable.getName() + " (parent) using " +
	    foreignKeyColumns.toString() + " columns.";
    }


    /**
     * Implements compareTo method needed to support Comparable interface.
     *
     * @param otherFk Another foreign key definition.
     *
     * @return Returns a negative number if the name of this FK is less than
     *         the name of the other FK; returns 0 if they have the same name;
     *         returns a positive number if this name of this FK is greater
     *         than the name of the other FK.
     */

    public int compareTo (Object otherFk)
    {
	ForeignKeyDefinition other = (ForeignKeyDefinition) otherFk;
	return name.compareTo(other.getName());
    }

    /**
     * Return the name of the foreign key.
     *
     * @return Name of the foreign key.
     */

    public String getName()
    {
	return name;
    }



    /**
     * Return the child table in the foreign key definition.
     *
     * @return Child table in the foreign key definition.
     */

    public Table getChildTable()
    {
	return childTable;
    }


    /**
     * Add a foreign key column to this foriegn key defintion 
     * 
     * @param pkColumnName Name of column in parent table.
     * @param fkColumnNmae Name of matching column in child table.
     */
    
    public void addColumn(String pkColumnName,
			  String fkColumnName)
    {
	ForeignKeyColumn fkCol = 
	    new ForeignKeyColumn(pkColumnName, fkColumnName);
	foreignKeyColumns.add(fkCol);
	return;
    }

    /**
     * Generate and SQL query to select the primary key columns of the 
     * child table, using a parameterized where clause on the foreign
     * key columns.
     *
     * For example, if the child table's primary key definition has 2
     * columns in it, col1, and col2, and the child table's 
     * foreign key definition has 2 columns in it, col2 and 
     * col3, then this will return a string of the form:
     *
     *   "select col1, col2 from child_table where col2 = ? and col3 = ?"
     * 
     * @return parameterized SQL query of the form: 
     *         select pkcol1, pkcol2, ... 
     *           from child_table
     *           where fkcol1 = ? and fkcol2 = ? and ...
     */

    public String getSqlSelectPkWhereParameterizedFk()
    {
	// instantiate string if routine has not been called before
	if (null == sqlSelectPkWhereParameterizedFk) {
	    sqlSelectPkWhereParameterizedFk = 
		"select " + childTable.getCommaSeparatedPkColumnNames() +
		"  from " + childTable.getName() +
		"  where " + getParameterizedSqlCondition();
	}
	return sqlSelectPkWhereParameterizedFk;
    }



    /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
     * COLLECTION METHODS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */

    /**
     * Return iterator on columns in foreign key definition.
     */

    public Iterator /*<ForeignKeyColumn>*/ iterator()
    {
	return foreignKeyColumns.iterator();
    }



    /* --------------------------------------------------------------------
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

    /** 
     * Get a parameterized SQL condition string that can be used with specific 
     * column values to retrieve matching rows in the table with this foreign 
     * key.
     *
     * For example, if a foreign key definition has 2 columns in it, fkcol1 and 
     * fkcol2, then this will return a string of the form:
     *
     *   "fkcol1 = ? and fkcol2 = ?"
     * 
     * @return parameterized SQL condition string of the form: 
     *         "fkcol1 = ? and fkcol2 = ? and ...".  This string can be dropped
     *         into an SQL where clause.
     */

    private String getParameterizedSqlCondition()
    {
	String parameterizedSqlCondition = null;
	Iterator /*<ForeignKeyColumn>*/ colIter = foreignKeyColumns.iterator();
	while (colIter.hasNext()) {
	    ForeignKeyColumn column = (ForeignKeyColumn) colIter.next();
	    String columnName = column.getForeignKeyColumnName();
	    if (null == parameterizedSqlCondition) {
		parameterizedSqlCondition = columnName + " = ?";
	    }
	    else {
		parameterizedSqlCondition += " and " + columnName + " = ?";
	    }
	}
	return parameterizedSqlCondition;
    }


}
