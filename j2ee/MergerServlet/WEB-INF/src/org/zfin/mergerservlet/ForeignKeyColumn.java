package org.zfin.mergerservlet;

/**
 * Defines a foreign key column.  Why is this worth a class of its own?
 * Mainly because we also store the corresponding primary key column name
 * along with the foreign key column name.  They come back from JDBC 
 * togehter and storing them together should make it easy to generate 
 * SQL that compares foreign keys with primary keys.
 *
 * However, this could be an inner class of ForeignKeyDefinition.
 * 
 * The names of the tables that contains these columns are stored in objects
 * that contain these objects.
 */

class ForeignKeyColumn
{
    /* --------------------------------------------------------------------
     * ATTRIBUTES
     * -------------------------------------------------------------------- */

    /**
     * Primary key column name
     */
    private String primaryKeyColumnName;

    /**
     * Foreign key column name
     */
    private String foreignKeyColumnName;



    /* -------------------------------------------------------------------- 
     * CONSTRUCTORS
     * -------------------------------------------------------------------- */

    /**
     * Constructor.  Given a primary key - foreign key column name pair,
     * create a ForeignKeyColumn object that associates the two.
     *
     * @param pkColumnName  Name of column in parent table that corresponds 
     *                      to the foreign key column in the child table
     * @param fkColumnName  Name of column in child table that corresponds 
     *                      to the primary key column in the parent table
     */

    public ForeignKeyColumn (String pkColumnName,
			     String fkColumnName)
    {
	primaryKeyColumnName = pkColumnName;
	foreignKeyColumnName = fkColumnName;

	return;
    }



    /* -------------------------------------------------------------------- 
     * PUBLIC METHODS
     * -------------------------------------------------------------------- */

    /**
     * Return the primary key colum name that corresponds to the foreign key
     * column.
     *
     * @return Primary key column name.
     */

    public String getPrimaryKeyColumnName()
    {
	return primaryKeyColumnName;
    }



    /**
     * Return the foreign key colum name.
     *
     * @return Foreign key column name.
     */

    public String getForeignKeyColumnName()
    {
	return foreignKeyColumnName;
    }


    /**
     * Return a string representation of the foreign key column.
     *
     * @return A string representation of the foriegn key column.
     */
    public String toString()
    {
	return primaryKeyColumnName + " = " + foreignKeyColumnName;
    }
    

}
