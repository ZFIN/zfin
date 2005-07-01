package org.zfin.mergerservlet;

import java.sql.*;
import java.util.*;

/**
 * Defines a primary or foreign key value.  A key value is a list of 
 * column name / value pairs.  The pairs combine to form a key.
 */

class KeyValue
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /** 
     * List of column name/value pairs 
     */
    private ArrayList /* <ColumnNameValuePair> */ columnValuePairs;


    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructs an empty KeyValue.
     */

    public KeyValue ()
    {
	columnValuePairs = new ArrayList /*<ColumnNameValuePair>*/ ();
	return;
    }




    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /** 
     * Add a column name value pair to the key value.
     *
     * @param db         Database that column name value pair exists in.
     * @param columnName Name of the column 
     * @param value      Value of the column
     */

    public void addColumnNameValuePair(MergerDatabase db,
				       String columnName, 
				       Object value)
	throws SQLException
    {
	ColumnNameValuePair cnvp = 
	    new ColumnNameValuePair(db, columnName, value);
	columnValuePairs.add(cnvp);
	return;
    }


    /**
     * Show the primary key in HTML format, with links, if appropriate.
     */
    
    public String show()
    {
	String kvString = "";
	Iterator colIter = columnValuePairs.iterator();
	while (colIter.hasNext()) {
	    if (! kvString.equals("")) {
		kvString += ", ";
	    }
	    ColumnNameValuePair nameValuePair = 
		(ColumnNameValuePair) colIter.next();
	    kvString = kvString + nameValuePair.show();
	}
	return kvString;
    }



    /**
     * Return a string representation of the key value.
     *
     * @return String representation of the key value.
     */

    public String toString()
    {
	String kvString = "KeyValue[ ";
	Iterator colIter = columnValuePairs.iterator();
	while (colIter.hasNext()) {
	    ColumnNameValuePair nameValuePair = 
		(ColumnNameValuePair) colIter.next();
	    kvString = kvString + nameValuePair.toString() + " ";
	}
	kvString = kvString + "]";
	return kvString;
    }



    /**
     * Return an HTML representation of the key value.
     *
     * @return HTML representation of the key value.
     */

    public String toHtml()
    {
	String header = 
	    "<div style=\"border-width: 1px\">\n" +
	    "<br><b>Key Value</b>:\n" +
	    "<table style=\"border-style: solid; border-width: 1px\">\n";

	String colString = "";
	Iterator colIter = columnValuePairs.iterator();
	while (colIter.hasNext()) {
	    ColumnNameValuePair nameValuePair = 
		(ColumnNameValuePair) colIter.next();
	    colString += 
		"<tr>\n" +
		"  <td style=\"text-align: right\">\n" + 
		"    " + nameValuePair.getName() + "\n" +
		"  </td>\n" +
		"  <td>=</td>\n" + 
		"  <td>\n" +
		"    " + nameValuePair.getValue() +
		"  </td>\n" +
		"</tr>\n";
	}
	String footer = "</table>\n</div>\n";

	return header + colString + footer;
    }



   /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
     * COLLECTION METHODS
     * - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -  */


    /**
     * Return iterator for column name/value pair list
     */

    public Iterator /*<ColumnNameValuePair>*/ iterator()
    {
	return columnValuePairs.iterator();
    }


    /* -------------------------------------------------------------------- 
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */



}
