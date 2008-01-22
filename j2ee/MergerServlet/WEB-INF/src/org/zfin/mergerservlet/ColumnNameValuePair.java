package org.zfin.mergerservlet;

import javax.sql.*;
import java.sql.*;
import java.util.*;

/**
 * Defines a column name / value pair.  That is, what is the name of the 
 * column and what is its value.  If the value is a ZDB ID, then we also
 * store additional information about the value.
 */

class ColumnNameValuePair
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Column name
     */
    private String columnName;

    /**
     * Value of column, as an Object.  Using Object class rather than 
     * specific datatype helps us avoid a raft of complicated code
     */
    private Object columnValue;

    /**
     * If value of column is a ZDB ID then also save the ZDB object type of 
     * the ZDB ID.  This is null if value is not a ZDB ID.
     */
    private ZdbObjectType objectType;



    /**
     * This is the result of calling get_obj_name(columnValue), if columnValue
     * is a ZDB ID.  If the ZDB ID exists in the database then this will 
     * always have a value, although it may just be the ZDB ID itself.
     *
     * For ZDB IDs this string is used to display a meaningful name with the
     * ZDB ID.
     *
     * :TODO: Not sure if the output of get_obj_name() is what we want here.
     *        Historically, get_obj_name() returned names for things that
     *        clearly had names, like markers, and returned ZDB IDs for things
     *        that lacked well defined names, such as marker_relationships.
     */
    private String valueName;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructor.  Given a column name and its value, construct a column name
     * / value pair.  If the value is a ZDB ID then save additional information
     * about the value.
     *
     * @param db      Database that column name / value pair exists in.
     * @param colName Name of column
     * @param colVal  Value of column
     */

    public ColumnNameValuePair (MergerDatabase db,
				String colName,
				Object colVal)
	throws SQLException
    {
	columnName = colName;
	columnValue = colVal;
	objectType = null;
	String colValString = colVal.toString();
	valueName = colValString;

	// if column value is a ZDB ID then look up and save the object
	// type and name of the object it identifies.
	if (columnValue.getClass().getName().equals("java.lang.String")) {
	    objectType = db.getMetadata().getObjectTypeFromZdbId(colValString);
	    if (null != objectType) {
		// We have a ZDB ID, get object name
		valueName = db.getNameFromZdbId(colValString);
	    }
	}
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


    /**
     * Show the column name value pair as HTML, possibly with links.
     */
    public String show()
    {
	String outString;

	if (null == objectType || null == objectType.getAppPage()) {
	    outString = valueName;
	}
	else {
	    outString = 
		"<a href=\"/<!--|WEBDRIVER_PATH_FROM_ROOT|-->/webdriver?MIval=" + 
		objectType.getAppPage() +
		"&OID=" + columnValue.toString() + "\">" +
		valueName + " (" + columnValue.toString() +")</a>";
	}
	return outString;
    }

}
