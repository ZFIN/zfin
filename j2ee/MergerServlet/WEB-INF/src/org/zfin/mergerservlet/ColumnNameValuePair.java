package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;

/**
 * Defines a column name / value pair.  That is, what is the name of the 
 * column and what is its value.
 */

class ColumnNameValuePair
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /** Column name */
    private String columnName;

    /**
     * Value of column, as an Object.  Using Object class rather than 
     * specific datatype helps us avoid a raft of complicated code
     */
    private Object columnValue;


    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructor.  Given a column name and its value, construct a column name
     * / value pair.
     *
     * @param colName Name of column
     * @param colVal  Value of column
     */

    public ColumnNameValuePair (String colName,
				Object colVal)
    {
	columnName = colName;
	columnValue = colVal;
	return;
    }


    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Get value 
     *
     * @return value object in pair
     */
    public Object getValue()
    {
	return columnValue;
    }



    /**
     * Get name
     *
     * @return name of column
     */
    public String getName()
    {
	return columnName;
    }



    /**
     * Convert object to String.  Very useful for debugging.
     *
     * @return The string representation of this column name value pair.
     */

    public String toString()
    {
	return "(" + columnName + " = " + columnValue + ")";
    }
}
