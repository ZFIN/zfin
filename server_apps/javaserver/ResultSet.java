class ResultSet extends Object
{
    Object connection;
    boolean nomore;
    Object row;
    Object rowDesc;
    boolean wasnull;
    boolean wascomposite;
    int colCount;
    
    public ResultSet (Object conn, boolean nom)
    {
	connection = conn;
	nomore = nom;
	row = null;
	rowDesc = null;
	wasnull = false;
	wascomposite = false;
	colCount = 0;
    }

    protected void finalize ()
    {
	try
	{
	    if (connection != null)
	    {
		close ();
		connection = null;
	    }
	}
	catch (SQLException e)
	{
	    e.printStackTrace ();
	}
    }
    
    
    public boolean next() throws SQLException 
    {
	int result = getNext ();
	if (nomore)
	    return false;
	if (result != 0)
	{
	    throw new SQLException ("getNext returned " + Integer.toString (result));
	}
	return true;
    }

    protected native int getNext ();

    public String getString(int columnIndex) throws SQLException
    {
	String result = nativeGetString (columnIndex);
	if (result != null || wasnull)
	    return result;
	throw new SQLException ("getString failed, wascomposite = " +
				new Boolean (wascomposite).toString ());
    }
    
    protected native String nativeGetString (int columnIndex);
    
    public native void close() throws SQLException;

    public  boolean wasNull() throws SQLException
    {
	return wasnull;
    }

    public int getColumnCount ()
    {
	return colCount;
    }

/*
    public native int getInt(int columnIndex) throws SQLException; */
}

