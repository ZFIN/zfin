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
	Object connection = openConnection (null, null, null);
	if (connection == null)
	    throw new SQLException ("Connection failed");
    }

    public MIConnection (String database, String user, String password) throws SQLException
    {
	Object connection = openConnection (database, user, password);
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

    protected native Object openConnection (String database, String user, String password);

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

