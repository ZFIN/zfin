package zmapper;

/*
  canvas object for drawing genetic maps, can output gif files
  or be used interactively.  

  Contains:
    1 MarkerBuilder
	n BackBones
  
*/

import java.awt.*;
import java.util.*;
import java.lang.*;

public class MapViewer extends Canvas {

	MarkerBuilder MB; //fetches and builds Marker objects
	Hashtable BBTable; //Maps panel_name + OR_lg -> BackBone object

	//String query_string;
	Vector QueryStrings;
	String selected_marker_id;
	Hashtable SM; //maps panel->marker_id
	String data;
	int PADDING = 20;
	boolean watermark_t;
	
	public static final Font F = new Font("SansSerif", Font.PLAIN, 9);
	
	public MapViewer(String data, Hashtable selected_markers) {
		
		watermark_t = true;

		MB = new MarkerBuilder();
		BBTable = new Hashtable();
		this.data = data;
		this.SM = selected_markers;

	}
	

	public MapViewer(String data, String selected_marker_id) 	{

		watermark_t = true;
		
		MB = new MarkerBuilder();
		BBTable = new Hashtable();
		this.data = data;
		this.selected_marker_id = selected_marker_id;


	}
	
	public MapViewer(String query_string, String host_id, String host_port, Hashtable selected_markers) {

		watermark_t = false;

		
		MB = new MarkerBuilder(host_id, host_port);
		BBTable = new Hashtable();


	
		Vector V = new Vector();
		V.addElement(query_string);
		this.QueryStrings = V;
			
		this.SM = selected_markers;
		
		watermark_t = false;
	}
	
	public MapViewer(String query_string, String host_id, String host_port, String selected_marker_id) {

		watermark_t = false;

		
		MB = new MarkerBuilder(host_id, host_port);
		BBTable = new Hashtable();

	
		Vector V = new Vector();
		V.addElement(query_string);
		this.QueryStrings = V;
			
		this.selected_marker_id = selected_marker_id;
	   

	}

	public MapViewer(Vector QueryStrings, String host_id, String host_port, String selected_marker_id) {

		watermark_t = false;

		MB = new MarkerBuilder(host_id, host_port);
		BBTable = new Hashtable();


		this.QueryStrings = QueryStrings;
		this.selected_marker_id = selected_marker_id;
		

	}
	
		
	public Enumeration go() {

		Vector results = new Vector();
		
		if (data !=null) {
			results = MB.buildMarkers(data);
//			watermark_t = true;
		} else {
			Enumeration E = QueryStrings.elements();
			Vector V;
			while(E.hasMoreElements() )	{
				V = MB.getMarkers((String)E.nextElement());
				results = merge(results,V);
			}
//			watermark_t = false;
		}
		
		buildBackBones(results.elements());

			  
		return null;
	}
	
	public void buildBackBones(Enumeration E) {

		Marker M;
		BackBone BB = null;


		while (E.hasMoreElements()) {
			M = (Marker) E.nextElement();



			
			if (selected_marker_id != null) //single selected, being phased out as of 8/29/00
			{
				if (M.getZdb_id().equals(selected_marker_id))
					M.setSelected(true);
			}

			if (SM != null) //panel->abbrev hashtable, new as of 8/29/00
			{
				if ((SM.get(M.getTarget_abbrev()) != null) &&( ((String)SM.get(M.getTarget_abbrev())).equals(M.getZdb_id())))
                                                     //if the marker id is in the hashtable, the hashtable returns
				{					                 //the panel on which it's selected, if that matches the marker also:
					System.err.println("YO! " + M.getZdb_id() + ", " + M.getTarget_abbrev());

					M.setSelected(true);             //we set it as selected.
				}
				
			}
			
			if (BBTable.get(M.getTarget_abbrev()+M.getOR_lg()) == null) { //new BackBone
				BB = new BackBone(M.getTarget_abbrev(), M.getOR_lg(), M.getMetric(), watermark_t);
				BBTable.put(M.getTarget_abbrev() + M.getOR_lg(), BB);
				BB.addMarker(M);
			} else {
				BB = (BackBone) BBTable.get(M.getTarget_abbrev()+M.getOR_lg());
				BB.addMarker(M);
			}
		}

		
	}	
	
		 
	public Image getImage(int w, int h)	{
		
		Frame f = new Frame(); 
		//make sure the frame has a peer 
		f.addNotify(); 
		//use the frame to create your image 
		Image img = f.createImage(w,h);
			//free memory allocated by the frame 
		f.removeNotify(); 

		Graphics g = img.getGraphics();

		g.setColor(Color.white);
		g.fillRect(0,0,w,h);

		g.setColor(Color.red);

//		drawKey(g,10,0);

		go();

		BackBone BB = null;

		int p = 0; //PADDING;
		int bbw = 0;

		if (BBTable.size() != 0)
			bbw = (w/BBTable.size()) - (BBTable.size()*p);  //width for each backbone, minus padding

		int i = 0;

		Enumeration E = this.elements(); //no attempt at ordering

		BackBone lastBB = null;
		while (E.hasMoreElements()) {
			BB = (BackBone)E.nextElement();
			BB.draw(g,i*bbw + (i+1)*p, bbw,h);

			if (lastBB != null) 
				BB.drawFramework(g, i*bbw + (i+1)*p, bbw, h, lastBB,i);
			
			lastBB = BB;
			i++;
			System.err.println(BB);
		}

		
/*		BackBone HS = (BackBone)BBTable.get("HS");
		HS.draw(g,20,160);
		BackBone MGH = (BackBone)BBTable.get("MGH");
		MGH.draw(g, 220,160);*/
		g = null;
		return img;

		
	}

	public void drawKey(Graphics g, int X, int Y) {
		g.setFont(F);
		FontMetrics FM =  Toolkit.getDefaultToolkit().getFontMetrics(F);

		int i = 0;

		g.setColor(Color.black);
		g.drawString("KEY:  ", X, Y+12);
		i = i + FM.stringWidth("KEY:  ");

		
		g.setColor(Marker.SSLP_c);
		g.drawString("SSLP ", X+i, Y+12);	
		i = i + FM.stringWidth("SSLP ");

		g.setColor(Marker.SSR_c);
		g.drawString("SSR ", X+i, Y+12);	
		i = i + FM.stringWidth("SSR ");	

		g.setColor(Marker.STS_c);
		g.drawString("STS ", X+i, Y+12);	
		i = i + FM.stringWidth("STS ");
		
		g.setColor(Marker.RAPD_c);
		g.drawString("RAPD ", X+i, Y+12);	
		i = i + FM.stringWidth("RAPD ");

		i = FM.stringWidth("KEY:  ");

		g.setColor(Marker.FISH_c);
		g.drawString("MUTANT ", X+i, Y+24);
		i = i + FM.stringWidth("MUTANT ");

		g.setColor(Marker.GENE_c);
		g.drawString("GENE ", X+i, Y+24);
		i = i + FM.stringWidth("GENE ");

		g.setColor(Marker.EST_c);
		g.drawString("EST ", X+i, Y+24);
		i = i + FM.stringWidth("EST ");


	}
	

	public String panelClick(int x, int y) {
		//which backbone?

		BackBone BB;
		Enumeration E = this.elements();
		String clickedPanel = null;
		while (E.hasMoreElements()) {
			BB = (BackBone)E.nextElement();
			if (BB.contains(x,y)) {
				clickedPanel = BB.panelClick(x,y);
				continue;
			}
		}

		return clickedPanel;
	}
	
	
	public Marker click (int x, int y) 	{
		//which backbone?

		BackBone BB;
		Enumeration E = this.elements();
		Marker clickedMarker = null;
		
		while (E.hasMoreElements()) {
			BB = (BackBone)E.nextElement();
			if (BB.contains(x,y)) {
				clickedMarker = BB.click(x,y);
				continue;
			}
		}
			
		return clickedMarker;
	}

	//tacks the elements in V2 on to the end of V1
	public Vector merge (Vector V1, Vector V2) 	{
		Enumeration E = V2.elements();
		while (E.hasMoreElements()) {
			V1.addElement(E.nextElement());
		}
		return V1;
	}
	

	public Enumeration elements() 	{
		Vector Results = new Vector();
		Vector M = new Vector();
		Vector M_s = new Vector();
		Vector Rh = new Vector();
		Vector Rh_s = new Vector();
		BackBone BB;

		BackBone ZMAP = null;
		
		Enumeration E = BBTable.elements();

		

		while( E.hasMoreElements()) {  //find the panels without markers selected
			BB = ((BackBone)E.nextElement());

			if (BB.getSelected() == null) 
			{
				if (BB.getPanel_name().equals(BackBone.MERGEDPANEL))
					ZMAP = BB;
				else if (BB.getMetric().equals("cM"))
					M.addElement(BB);
				else
					Rh.addElement(BB);
			}
			
		}

		E = BBTable.elements();
		while( E.hasMoreElements()) {  //now find the panels with markers selected
			BB = ((BackBone)E.nextElement());

			if (BB.getSelected() != null) 
			{
				if (BB.getPanel_name().equals(BackBone.MERGEDPANEL))
					ZMAP = BB;
				else if (BB.getMetric().equals("cM"))
					M_s.addElement(BB);
				else
					Rh_s.addElement(BB);
			}
			
		}

		if (ZMAP != null)
			Results.addElement(ZMAP);
		int i;

		if ((Rh.size() > 1) && (BBTable.size() > 2))
			Rh = BBSort(Rh);
//		E = Rh.elements();
//		while(E.hasMoreElements())
//			Results.addElement(E.nextElement());
		
		for (i = 0 ; i < Rh.size() ; i++) {
			Results.addElement(Rh.elementAt(i));
		}

		if ((Rh_s.size() > 1) && (BBTable.size() > 2))
			Rh_s = BBSort(Rh_s);	
//		E = Rh_s.elements();
//		while(E.hasMoreElements())
//			Results.addElement(E.nextElement());

		for (i = 0 ; i < Rh_s.size() ; i++) {
			Results.addElement(Rh_s.elementAt(i));
		}

		if ((M_s.size() > 1) && (BBTable.size() > 2))
			M_s = BBSort(M_s);
//		E = M_s.elements();
//		while (E.hasMoreElements())
//			Results.addElement(E.nextElement());

		for (i = M_s.size()-1  ; i >= 0 ; i--) {
			Results.addElement(M_s.elementAt(i));
		}
		
		
		if ((M.size() > 1) && (BBTable.size() > 2))
			M = BBSort(M);
//		E = M.elements();
//		while (E.hasMoreElements())
//			Results.addElement(E.nextElement());

		for (i = M.size()-1  ; i >= 0 ; i--) {
			Results.addElement(M.elementAt(i));
		}
			
/*		E = Results.elements();
		while(E.hasMoreElements())
		System.err.println("Ordering: " + E.nextElement());*/

		
		
		return Results.elements();

	}
	
	public Vector BBSort(Vector V) {
		//sorts BackBones by marker count, smallest to largest
		return bubblesort(V);

	}

/*	public Vector RBBSort(Vector V) {
		//sorts BackBones by marker count, largest to smallest
		
		}*/
	
	public Vector bubblesort(Vector V) {
		BackBone temp;
		int count; // for checking whether interchanges are done or not
		int n; // for counting number of passes
		n = 0;
		count=1;

		while (n < V.size() || count != 0) {
			count = 0;
			for(int j=0;j < V.size()-1;j++) {

				if ( ((BackBone)(V.elementAt(j))).getMarker_count() > ((BackBone)(V.elementAt(j+1))).getMarker_count() )   {
					temp = (BackBone)V.elementAt(j);
					V.setElementAt(V.elementAt(j+1), j);
					V.setElementAt(temp,j+1);
					count++;
				}
			}
			n++;
		}
		return V;
	}

	
} 
