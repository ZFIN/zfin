package zmapper;

/*
  Retrieves marker data and builds Marker objects

*/

import java.util.*;


public class MarkerBuilder {

	String host_id;
	String host_port;

	SQLQuery SQL;
	
	public MarkerBuilder (String host_id, String host_port)  {
		this.host_id = host_id;
		this.host_port = host_port;

		SQL = new SQLQuery(host_id, host_port);
	}	

	public MarkerBuilder() {
		
	}
	
	public Vector buildMarkers(String data) {
		StringTokenizer sTok = new StringTokenizer(data, "|");
		Vector V = new Vector();
		while(sTok.hasMoreTokens()) {
			try 
			{
				V.addElement(new Marker((String)sTok.nextToken() ,
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken(),
										(String)sTok.nextToken()));
			}
			catch (java.util.NoSuchElementException e) 
			{
				System.err.println("MarkerBuilder.buildMarkers - " + e);
			}
			
		}	
		return V;
	}
	
	
	public Vector getMarkers(String query_string) 	{
		Vector V = SQL.select(8, query_string);
		Vector results = new Vector();
		Marker M;
		
		Enumeration E = V.elements();
		while(E.hasMoreElements()) {
			M = new Marker((String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement(),
						   (String)E.nextElement());
			results.addElement(M);

		}

		if (query_string.indexOf("desc;") >= 0) { //they're ordered descending and have to be reversed..
			Vector VV = new Vector();
			int i = results.size() - 1; //fencepost...
			while(i >= 0) 	{
				VV.addElement(results.elementAt(i));
				i--;

			}
			

			results = VV;
			
		}
		
		return results;
	}
 

}
