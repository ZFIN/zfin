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
     * Metadata object that for the database this object type is in.  
     */
    final private Metadata metadata;

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
     * @param meta   Metadata object for the database that the 
     *               object type exists in.
     * @param dbRow  JDBC row containing the definition of this object type
     *               fromthe database.
     */
    public ZdbObjectType (Metadata meta,
			  ResultSet dbRow)
	throws SQLException
    {
	metadata = meta;
        name = 
	    (String) metadata.resultSetGetColumn(dbRow, "zobjtype_name");
        appPage = 
	    (String) metadata.resultSetGetColumn(dbRow, "zobjtype_app_page");

	// Get home table for this object type; add this object type to the
	// home table.
	String tableName = 
	    (String) metadata.resultSetGetColumn(dbRow, "zobjtype_home_table");
	homeTable = metadata.getTable(tableName);
	homeTable.addZdbObjectType(this);

	zdbIdColumnName = 
	    (String) metadata.resultSetGetColumn(dbRow, 
						 "zobjtype_home_zdb_id_column");
	// Last fields can be null; deal with that.
	Boolean isDataBool = 
	    (Boolean) metadata.resultSetGetColumn(dbRow, "zobjtype_is_data");
	if (null == isDataBool) {
	    isData = false;
	}
	else {
	    isData = isDataBool.booleanValue();
	}
	Boolean isSourceBool = 
	    (Boolean) metadata.resultSetGetColumn(dbRow, "zobjtype_is_source");
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

    public Metadata getMetadata()
    {
	return metadata;
    }


    public Table getHomeTable()
    {
	return homeTable;
    }


    public String getZdbIdColumnName()
    {
	return zdbIdColumnName;
    }


    public boolean isData()
    {
	return isData;
    }

    public boolean isSource()
    {
	return isSource;
    }


    /* --------------------------------------------------------------------
     * PRIVATE METHODS
     * -------------------------------------------------------------------- */

}
