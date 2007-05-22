import java.io.*;
import java.awt.*;
import java.util.*;

import java.sql.*;

import java.awt.image.*;


import zmapper.*;


public class mapimage {

	public static cgi_lib cgi;
	
	public static void main(String[] args)  {

		mapimage app = new mapimage();

		app.init();
	}

	public mapimage() {


	}

	public String getMarkers(String query_string, String host, String port) {
        String data = new String();

        SQLQuery SQL = new SQLQuery(host, port);
		Vector V = SQL.selectAll(8, query_string);
		Vector results = new Vector();

		
		Enumeration E = V.elements();
		while(E.hasMoreElements()) {
			data = data + (String)E.nextElement() + "|"
				        + (String)E.nextElement() + "|"
						+ (String)E.nextElement() + "|"
						+ (String)E.nextElement() + "|"
						+ (String)E.nextElement() + "|"
						+ (String)E.nextElement() + "|"
						+ (String)E.nextElement() + "|"
				        + (String)E.nextElement() + "|\n";
		}

/**		if (query_string.indexOf("desc;") >= 0) { //they're ordered descending and have to be reversed..
			Vector VV = new Vector();
			int i = results.size() - 1; //fencepost...
			while(i >= 0) 	{
				VV.addElement(results.elementAt(i));
				i--;

			}
			results = VV;
  		}
**/		
	  return data;
    }
	

	
	public void init() {

		MapViewer MV;

//	MV = new MapViewer(getParameter("data"), getParameter("selected_marker"));
		
		printImageHeader();

		Hashtable form = cgi.ReadParse(System.in);

        String data;		
		String zmap_panels;
		String selected_marker;
		int w = 100;
		int h = 100;
		if (form.get("data") == null)
			System.err.println("DATA IS NULL");

		data = (String)form.get("data");
		data = data.substring(0, data.length()-2);

		zmap_panels = (String)form.get("from_panels");
		
		String table = "";
		if (data.indexOf("ZMAP") > -1)
			table = "zmap_pub_pan_mark";
		else
			table = "paneled_markers";

		//System.err.println("data: " + data);
		Hashtable SM = new Hashtable();

		StringTokenizer sTok = new StringTokenizer(data,"|");

		String panel;
		String lg;
		String selected;
		String low;
		String high;

		int visible_types;
		String gene = " mtype = 'GENE' ";
		int G = 1;
		String fish = " mtype = 'MUTANT' ";
		int M = 8;
		String est = " mtype = 'EST' or mtype = 'BAC_END' or mtype = 'PAC_END' ";
		int E = 2;
		String anon = " mtype = 'SSLP' or mtype = 'STS' or mtype = 'RAPD' or mtype = 'RFLP' or mtype = 'SNP' ";
		int A = 4;
		String type_string = "";

		String query_string = "";
		if (table.equals("paneled_markers")) {
		    query_string = "select distinct zdb_id, abbrev, mtype, target_abbrev, lg_location::numeric(6,2), OR_lg, mghframework, metric from " + table + " where ("; 
		} else if (table.equals("zmap_pub_pan_mark")) {
		   query_string = "select distinct zdb_id, abbrevp, mtype, target_abbrev, lg_location::numeric(6,2), OR_lg, mghframework, metric from " + table + " where ("; 
		}

		String zpan_where = "";
	   
		if (zmap_panels != null) {
		        //System.err.println("panels? --- " + zmap_panels + " ---");
			zmap_panels = zmap_panels.substring(0, zmap_panels.length()-1);
			StringTokenizer zTok = new StringTokenizer(zmap_panels,"|");
			while (zTok.hasMoreTokens()) {
				zpan_where = zpan_where + " (abbrevp like '%_" + zTok.nextToken() + "') or ";
			}
			zpan_where = zpan_where.substring(0,zpan_where.length()-3); //chop off the trailing 'AND'
			zpan_where = zpan_where + ") and (";
			query_string = query_string + zpan_where;
		}

		
		boolean first = true;
		String or = "";
		while(sTok.hasMoreTokens()) {
			panel = sTok.nextToken();
			if (panel.startsWith("\n")) {
			        //System.err.println("starts with \\n");
				panel = panel.substring(1, panel.length()); //starts with a return..
			}
			
			  lg = sTok.nextToken();
			selected = sTok.nextToken();
			if (selected == null)
				selected = new String("NULL");
			low = sTok.nextToken();
			high = sTok.nextToken();
			if (sTok.hasMoreTokens()) {
				visible_types = (new Integer(sTok.nextToken())).intValue();
			} else {
				visible_types = 0; //all types
			}
				
				
			type_string = "";
			
			if (visible_types == 0) {
				type_string = gene + "or" + fish + "or" + est + "or" + anon;
			} else 	{
				if ((visible_types & G) == G) {
					if (type_string == "")
						type_string = type_string + gene;
					else
						type_string = type_string + "or" + gene;
				}
				
				if ((visible_types & M) == M) {
					if (type_string == "")
						type_string = type_string + fish;
					else
						type_string = type_string + "or" + fish;
				}
				
				if ((visible_types & E) == E) {
					if (type_string == "")
						type_string = type_string + est;
					else
						type_string = type_string + "or" + est;
				}
				
				if ((visible_types & A) == A) {
					if (type_string == "")
						type_string = type_string + anon;
					else
						type_string = type_string + "or" + anon;
				}
				
			}
			
			//System.err.println("Visible Types: " + type_string);

			
			SM.put(panel,selected);
			if (first == true) 
				or = "";
			else
				or = " or ";
			panel = panel.trim();
			//System.err.println("target_abbrev: " + panel);	
			query_string = query_string + or + " (target_abbrev = '" + panel + "' and OR_lg = '" + lg + "' and lg_location >= '" + low + "' and lg_location <= '" + high + "' and (" + type_string + ") )";
			first = false;
		}

		query_string = query_string + " ) order by 4, 5, 2 asc;";

		int p = -1;
		while (query_string.indexOf('\n') > -1) {
			p = query_string.indexOf('\n');
			query_string = query_string.substring(0,p-1) + query_string.substring(p+1, query_string.length());
		}
		
        //System.err.println(query_string);

        //if (form.get("selected_marker") != null)
		//	System.err.println("NO SELECTED MARKER");
		
		//selected_marker = (String)form.get("selected_marker");


		String panel_order = (String)form.get("panel_order");

		//System.err.println("SQL: " + query_string);

		//System.err.println("HOST: " + (String)form.get("host"));
		
		String marker_data = getMarkers(query_string, (String)form.get("host"), (String)form.get("port"));

		
		//MV = new MapViewer(query_string, (String)form.get("host"), (String)form.get("port"), SM, panel_order);
		MV = new MapViewer(marker_data, SM, panel_order);

		if (form.get("height") != null)
			h = (new Integer((String)form.get("height"))).intValue();
		else 
			System.err.println("HEIGHT IS NULL");
		   
		if (form.get("width") != null)
			w = (new Integer((String)form.get("width"))).intValue();
		else
			System.err.println("WIDTH IS NULL");
		

		//System.err.println("getImage");
		java.awt.Image img =  MV.getImage(w,h);


/*		System.err.println("pdf start");
		Document document = new Document();
		document.open();

		try {
			PdfWriter writer = PdfWriter.getInstance(document, System.out);
            document.open();
			document.add(new Paragraph("Hello World"));
			PdfContentByte cb = writer.getDirectContent();
            com.lowagie.text.Image pdfimg = com.lowagie.text.Image.getInstance(img, null);

			document.add(pdfimg);
		}
		catch(com.lowagie.text.DocumentException de) { System.err.println(de.getMessage()); }
		catch(IOException ioe) {System.err.println(ioe.getMessage()); }
        document.close();
					 
		System.err.println("pdf end"); */

/*		try {
			System.err.println("start jimi");
			Jimi.putImage(img, "/dev/null");
			System.err.println("end jimi");
			} catch (JimiException JE) { System.err.println(JE); }*/
		
				
		try	{
		        //System.err.println("make encoder");
			Acme.JPM.Encoders.GifEncoder ie = new Acme.JPM.Encoders.GifEncoder(img , System.out);
			//System.err.println("start encoding");
			ie.encode();	
			//System.err.println("done encoding");
			
		}
        catch ( Exception e )
		{
			System.err.println("e: " + e);   
			} 

/*		ImageOutputStream ios = ImageIO.createImageOutputStream(System.out);
		Iterator writers = ImageIO.getImageWritersByFormatName("png");
		ImageWriter writer = (ImageWriter) writers.next();
		writer.setOutput(ios);
		writer.write(img);*/

//		System.err.println("Headless: " + System.setProperty("java.awt.headless","true"));

/*		
		try {
			System.err.println("IO.write..start");
			javax.imageio.ImageIO.write((BufferedImage)img,"jpg",new BufferedOutputStream(System.out));
			System.err.println("IO.write..end");
			} catch (Exception e) {
			System.err.println(e);
			}
*/		
	 
		//System.err.println("cgi applet finished");
		System.exit(0);
	}	


	public void printImageHeader()	{
		
		System.out.println("Content-type: image/gif");
		System.out.println("");

	}	
 
	
	public void printBody(String[] queryData) {
		
			
	}

	
	
}
