class Statement extends Object 
{
    protected MIConnection miC;
    protected Object connection;
    protected int count;
    protected ResultSet lastResult;

    /*
       Following four constants taken from milib.h.  We can't give
       them exactly the same names as their milib.h counterparts
       because then the C preprocessor will have trouble with
       Statement.h.
    */
    protected final int JDBC_NO_MORE_RESULTS = 0;
    protected final int JDBC_ROWS = 1;
    protected final int JDBC_DML = 3;
    protected final int JDBC_DDL = 4;
    
    public Statement (MIConnection c)
    {
	miC = c;
	connection = c.getConnection ();
    }

    protected void finalize ()
    {
	try 
	{
	    close ();
	}
	catch (SQLException e)
	{
	    e.printStackTrace ();
	}
	
    }
    
    public ResultSet executeQuery(String sql) throws SQLException
    {	
	if (lastResult != null)
	    lastResult.close ();

	int result = doQuery (sql);
	if (result != JDBC_ROWS && result != JDBC_NO_MORE_RESULTS)
	    throw new SQLException ("Error " + result + " on query");
	lastResult = new ResultSet (connection, result == JDBC_NO_MORE_RESULTS);
	return lastResult;
    }
    
    protected native int doQuery (String sql);

    public int executeUpdate(String sql) throws SQLException
    {
	if (lastResult != null)
	{
	    lastResult.close ();
	    lastResult = null;
	}
	
	int result = doUpdate (sql);
	if (result == JDBC_DML || result == JDBC_DDL)
	    return count;
	throw new SQLException ("Error " + result + " on update or DDL");
    }

    protected native int doUpdate (String sql);

    public void close () throws SQLException
    {
	if (lastResult != null)
	    lastResult.finalize ();
	lastResult = null;
	miC.nullCurrentStatement ();
    }
}

