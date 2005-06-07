package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;


/**
 * A primary key definition, which is distinct from a primary key value.
 * This specifies what columns are in the primary key and in what order.
 */

class PrimaryKeyDefinition
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Table this is the primary key for. 
     */
    private Table table;

    /** 
     * List of columns in the primary key.  Using TreeMap because the columns
     * can be added out of order.  If we don't care about the order then 
     * we could use an array list instead.
     */
    private TreeMap /*<Integer,String>*/ columns;


    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /** 
     * Construct a primary key given the table object.  Returns a fully 
     * populatedprimary key object.
     * 
     * @param tbl  Table to get the primary key for.
     */

    public PrimaryKeyDefinition (Table tbl)
	throws SQLException
    {
	table = tbl;
	columns = new TreeMap /*<Integer,String>*/ ();
	table.getMetadata().getPrimaryKeyForTable(this);
    }



    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Return a string representation of the primary key definition.
     *
     * @return String representation of the primary key definition.
     */
    public String toString()
    {
	return columns.toString();
    }



    /**
     * Return an HTML representation of the primary key definition.
     *
     * @return HTML representation of the primary key definition.
     */
    public String toHtml()
    {
	String header = 
	    "<p>Primary key:" +
	    "<div style=\"margin-left: 2em\">" +
	    "<br>Table: " + table.getName();


	String colString = "";
	for (int colNum = 0; colNum < columns.size(); colNum++) {
	    Integer columnNum = new Integer(colNum);
	    colString += "<br> Column: " + (String) columns.get(columnNum);
	}
	String footer = "</div>";

	return header + colString + footer;
    }


    /** 
     * Return the table the primary key is for.
     *
     * @return Table the primary key is for.
     */

    public Table getTable()
    {
	return table;
    }



    /**
     * Add a column to the primary key definition
     *
     * @param columnNum  Column number of the column within the primary key.
     *                   This is 0 relative, meaning the first column is 
     *                   column 0.
     * @param columnName Name of the column in the primary key
     */

    public void addColumn(int columnNum,
			  String columnName)
    {
	Integer colNum = new Integer(columnNum);
	columns.put(colNum, columnName);
	return;
    }



    /** 
     * Generate a list of columns names in the primary key, separated by
     * commas.  This list could be used in a SQL select statement
     *
     * @return A string containing the column names in the primary key, in 
     *         the order they occur in the primary key, separated by commas
     */

    public String getCommaSeparatedColumnNames()
    {
	// There has to a be a more efficient way to do this
	String commaSeparated = null;

	for (int colNum = 0; colNum < columns.size(); colNum++) {
	    Integer columnNum = new Integer(colNum);
	    if (null == commaSeparated) {
		commaSeparated = (String) columns.get(columnNum);
	    }
	    else {
		commaSeparated += ", " + (String) columns.get(columnNum);
	    }
	}
	return commaSeparated;
    }


    public Iterator iterator()
    {
	return columns.values().iterator();
    }

}
