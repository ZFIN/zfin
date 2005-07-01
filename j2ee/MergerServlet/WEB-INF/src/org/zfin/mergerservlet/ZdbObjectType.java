package org.zfin.mergerservlet;

import java.sql.*;
import java.util.*;

/**
 * Defines a ZDB Object Type.  ZDB Object types are defined in the
 * zdb_object_type table in the ZFIN database, and this class more or
 * less mirrors that table.
 *
 * ZDB object types are the second component of ZDB IDs:
 *
 *   ZDB-ojbect-YYMMDD-#
 * 
 * You can look them up in the zdb_object_type table (or in a list 
 * of ZdbObjectType instances) and get information about that object type.
 */

public class ZdbObjectType implements Comparable
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Name of the ZDB Object Type.  This is case sensitive and in almost
     * every case is in ALL CAPS.  
     */
    private String name;

    /**
     * App page to use to display objects of this type.  This includes the
     * leading "aa-".  If an object type does not have its own app page,
     * then this value is null.
     */
    private String appPage;

    /** 
     * Table where records of this object type are defined.
     */
    private Table homeTable;


    /**
     * Name of column containing the ZDB ID in the home table
     */
    private String zdbIdColumnName;


    /**
     * A boolean field indicating if the object type is considered to be 
     * data or not.
     *  True:  This object type is considered to be a data item in ZFIN, 
     *         and ZDB IDs for this object type are stored in ZDB_ACTIVE_DATA.
     *  False: This object type is not considered to be a data item in ZFIN. 
     *         Therefore, ZDB IDs for this object type are not stored in 
     *         ZDB_ACTIVE_DATA.
     * This field has a third possible value in the database, NULL, which 
     * means the object type is considered to be a data item in ZFIN, 
     * but ZDB IDs for this object type are not yet stored in ZDB_ACTIVE_DATA.
     * The distinction between False and NULL is not valuable in this program
     * so NULLs from the database get promoted to FALSE.
     */
    private boolean isData;

    /**
     * A boolean field indicating if the object type is considered to be 
     * a source or not.
     *  True:  This object type is considered to be a source item in ZFIN, 
     *         and ZDB IDs for this object type are stored in ZDB_ACTIVE_SOURCE.
     *  False: This object type is not considered to be a source item in ZFIN. 
     *         Therefore, ZDB IDs for this object type are not stored in 
     *         ZDB_ACTIVE_SOURCE.
     * This field has a third possible value in the database, NULL, which 
     * means the object type is considered to be a source item in ZFIN, 
     * but ZDB IDs for this object type are not yet stored in ZDB_ACTIVE_SOURCE.
     * The distinction between False and NULL is not valuable in this program
     * so NULLs from the database get promoted to FALSE.
     */
    private boolean isSource;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */


    /** 
     * Constructor, given the metadata object for the database the object type
     * is in, and the results of a JDBC call to get the record that defines
     * this object type.  The constructor then makes callbacks to the metadata
     * to extract info from the JDBC result.
     *
     * This also adds the object type the object type's home table's list of
     * object types.
     *
     * @param meta   Metadata object for the database that the object type 
     *               exists in.  The object type is added to the home table
     *               the object belongs to.
     * @param dbRow  JDBC row containing the definition of this object type
     *               from the database.
     */

    public ZdbObjectType (Metadata meta,
			  ResultSet dbRow)
	throws SQLException
    {
        name = (String) meta.resultSetGetColumn(dbRow, "zobjtype_name");
        appPage = (String) meta.resultSetGetColumn(dbRow, "zobjtype_app_page");

	// Get home table for this object type; add this object type to the
	// home table.
	String tableName = 
	    (String) meta.resultSetGetColumn(dbRow, "zobjtype_home_table");
	homeTable = meta.getTable(tableName);
	homeTable.addZdbObjectType(this);

	zdbIdColumnName =
	    (String) meta.resultSetGetColumn(dbRow, 
					     "zobjtype_home_zdb_id_column");

	// Last fields can be null; deal with that.
	Boolean isDataBool = 
	    (Boolean) meta.resultSetGetColumn(dbRow, "zobjtype_is_data");
	if (null == isDataBool) {
	    isData = false;
	}
	else {
	    isData = isDataBool.booleanValue();
	}
	Boolean isSourceBool = 
	    (Boolean) meta.resultSetGetColumn(dbRow, "zobjtype_is_source");
	if (null == isSourceBool) {
	    isSource = false;
	}
	else {
	    isSource = isSourceBool.booleanValue();
	}
	return;
    }



    /* --------------------------------------------------------------------
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    public int compareTo (Object otherZdbObjectType)
    {
	ZdbObjectType other = (ZdbObjectType) otherZdbObjectType;
	return name.compareTo(other.getName());
    }

    public String toString()
    {
	return name;
    }
	
    public String toHtml()
    {
	return "<br>ZOT: " + name + ", Table: " + homeTable.getName() + "\n";
    }
	
    public String getName()
    {
	return name;
    }


    /**
     * Return home table for the object type.  This is the table where
     * records of this type are defined.
     */

    public Table getHomeTable()
    {
	return homeTable;
    }


    /**
     * Return the app page for this ZDB ID.  Not all object types have
     * their own app pages, in which case this will be null.
     */

    public String getAppPage()
    {
	return appPage;
    }

    /**
     * Return the name of the column in the home table where the ZDB ID
     * resides.
     */

    public String getZdbIdColumnName()
    {
	return zdbIdColumnName;
    }


    /**
     * Return true if ZDB IDs with this object type occur in ZDB_ACTIVE_DATA,
     * false otherwise
     */

    public boolean isData()
    {
	return isData;
    }


    
    /**
     * Return true if ZDB IDs with this object type occur in ZDB_ACTIVE_SOURCE,
     * false otherwise.
     */

    public boolean isSource()
    {
	return isSource;
    }


    /* --------------------------------------------------------------------
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

}
