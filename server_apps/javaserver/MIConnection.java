class MIConnection extends Object /* implements java.sql.Connection (in part) */
{
    protected Object connection;
    protected Statement currentStatement;
    
    static
    {
	System.loadLibrary ("MISQL");
    }
    
    public MIConnection () throws SQLException
    {
	Object connection = openConnection ("template1");
	if (connection == null)
	    throw new SQLException ("Connection failed");
    }

    public MIConnection (String database) throws SQLException
    {
	Object connection = openConnection (database);
	if (connection == null)
	    throw new SQLException ("Connection failed");
    }

    public void finalize () throws SQLException 
    {
	currentStatement.finalize ();
	close ();
    }

    public Statement createStatement () throws SQLException
    {
	if (currentStatement != null)
	    throw new SQLException ("Only one active statement permitted at a time");

	currentStatement = new Statement (this);
	return currentStatement;
    }

    protected native Object openConnection (String database);

    public Object getConnection ()
    {
	return connection;
    }

    public native void close () throws SQLException;

    public void nullCurrentStatement ()
    {
	currentStatement = null;
    }
    
}

