import java.awt.*;
import java.applet.*;
import java.util.*;
import zmapper.*;
import netscape.javascript.*;


public class mapplet extends Applet  {

	Image img;
	MapViewer MV;
	public JSObject JS;
	Vector Buttons;
	
	Marker clickedMarker; //defined globally to be used by both mousedown and mouse up
	String clickedPanel; //ditto

	public mapplet() {

	}

	public void init() {


		if (getParameter("data") != null) {
			if (getParameter("selected_marker") != null)
				MV = new MapViewer(getParameter("data"), getParameter("selected_marker"));
			else {
				Hashtable SM = new Hashtable();
				//Lookout!  hardcoded panel names.. now I'm gonna go to hell for sure..
				//this mess makes a zdb_id -> target_abbrev hashtable to send along.
				if (getParameter("MGH_m") != null)
					SM.put("MGH",getParameter("MGH"));
				if (getParameter("GAT_m") != null)
					SM.put("GAT",getParameter("GAT"));
				if (getParameter("HS_m") != null)
					SM.put("HS", getParameter("HS_m"));
				if (getParameter("MOP_m") != null)
					SM.put("MOP",getParameter("MOP_m"));
				if (getParameter("T51_m") != null)
					SM.put("T51",getParameter("T51_m"));
				if (getParameter("LN54_m") != null)
					SM.put("LN54",getParameter("LN54_m"));
				if (getParameter("JPAD_m") != null)
					SM.put("JPAD",getParameter("JPAD_m"));	
				MV = new MapViewer(getParameter("data"), SM);
			}
			
		} else if (getParameter("where") != null) 	{
			String Q = "select zdb_id, abbrev, mtype, target_abbrev, lg_location::numeric(6,2), OR_lg, mghframework, metric from paneled_markers where " + getParameter("where") + " and private='f' order by 4,5 asc;";
			MV = new MapViewer(Q,getParameter("host"), getParameter("port"), getParameter("selected_marker"));
		} else if (getParameter("query") != null) 	{
			 MV = new MapViewer(getParameter("query"),getParameter("host"), getParameter("port"), getParameter("selected_marker"));
		} else if (getParameter("magic_marker") != null) { //this means we're taking a marker name and a list of panels
			Vector V = new Vector();
			int i=1;
			String panel_name;
			String zdb_id = getParameter("magic_marker");

			String host = getParameter("host");
			String port = getParameter("port");
			
			while(getParameter("panel"+i) != null) {
				panel_name=getParameter("panel"+i);

				V.addElement("select first 10  zdb_id, abbrev, mtype, target_abbrev, lg_location::numeric(6,2), OR_lg, mghframework, metric  from paneled_markers where OR_lg="
							 + " (select OR_lg from paneled_markers where zdb_id='" + zdb_id
							 + "' and target_abbrev='" + panel_name + "') and target_abbrev = '" + panel_name
							 + "' and lg_location::numeric(6,2) < (select lg_location::numeric(6,2) from paneled_markers where zdb_id = '"
							 + zdb_id + "' and target_abbrev='" + panel_name + "') and private='f' order by 5 desc;");

				V.addElement("select first 10  zdb_id, abbrev, mtype, target_abbrev, lg_location::numeric(6,2), OR_lg, mghframework, metric  from paneled_markers where OR_lg="
							 + " (select OR_lg from paneled_markers where zdb_id='" + zdb_id
							 + "' and target_abbrev='" + panel_name + "') and target_abbrev = '" + panel_name
							 + "' and lg_location::numeric(6,2) >= (select lg_location::numeric(6,2) from paneled_markers where zdb_id = '"
							 + zdb_id + "' and target_abbrev='" + panel_name + "') and private='f' order by 5 asc;");
				
				i++;	
			}
			MV = new MapViewer(V, host, port, getParameter("magic_marker"));
		} else {
			MV = new MapViewer("select zdb_id, abbrev, mtype, target_abbrev, lg_location::numeric(6,2), OR_lg, framework_t, metric from paneled_markers " 
							   + " where OR_lg = '7'"
							   + " and ((target_abbrev = 'LN54' and lg_location > -1 and lg_location < 100) "
							   + " or (target_abbrev = 'GAT' and lg_location > -1 and lg_location < 100) "
							   + " or (target_abbrev = 'JPAD and lg_location > -1 and lg_location < 100) "
							   + " or (target_abbrev = 'MOP' and lg_location > -1 and lg_location < 100) "
							   + " or (target_abbrev = 'MGH' and lg_location > 10 and lg_location < 70)) "
							   + " and private = 'f' order by 4, 5;", "zfin.org","7365", "ZDB123-1231312"); 
		} 


		img = MV.getImage(this.bounds().width,this.bounds().height);

		Buttons = new Vector();
		Button zI, zO;
		Enumeration E = MV.elements();
		int panel_count = 0;

		while(E.hasMoreElements()) 
		{
			BackBone BB = (BackBone)E.nextElement();
			zO = new Button("Zoom In");
			zI = new Button("Zoom Out");
			Buttons.addElement(zI);
			Buttons.addElement(zO);
			panel_count++;
		}
		int w = this.bounds().width/panel_count;
		Enumeration BE = Buttons.elements();
		E = MV.elements();
		int i = 5; //indent

		Graphics g = img.getGraphics();
		Font PF = new Font("SansSerif", Font.PLAIN, 14);
		FontMetrics PFM =  Toolkit.getDefaultToolkit().getFontMetrics(PF); 
		g.setFont(PF);
		g.setColor(new Color(200,200,200));
		g.setColor(Color.black);
		
		while (BE.hasMoreElements()) 
		{
			zO = (Button)BE.nextElement();
			zI = (Button)BE.nextElement();

			this.setLayout(null);
			
			this.add(zO);
			this.add(zI);

			zO.reshape(i,0,70,22);


			BackBone BB = (BackBone)E.nextElement();
			int percent_width = 0;
			String PS = new String("");
			if (getParameter(BB.getPanel_name() + "_ztotal") != null) {
				int ztotal = (new Integer(getParameter(BB.getPanel_name() + "_ztotal"))).intValue();
				//bbgm = new Float(BB.getMarker_count());
				//Float zt = new Float(ztotal);
				int percent = (int)((BB.getMarker_count()*100/ztotal) + .05);
				if (percent < 1)
					percent = 1;
				if (percent > 100)
					percent = 100;
				//System.err.println("BB.getMc: " + BB.getMarker_count() + ", ztotal: " + ztotal);
				PS = percent + "%";
				g.drawString(PS, i+zO.bounds().width+5, 16);
				percent_width = PFM.stringWidth(PS);
			}
			
			zI.reshape(i+zO.bounds().width+10 + percent_width  ,0,70,22);

				
			i = i + w;		
		}
		
		repaint();

		if (getParameter("onload") != null) {
			getJS();
			try {
				JS.eval(getParameter("onload")); 
			} catch (Exception e) {
				System.out.println("ONLOAD couldn't be executed");
			}
		}
		

	}

	public void start()	{

		if (MV == null)
			this.init();

//		if (getParameter("onload") != null) {
//			getJS();
//			try {
//				JS.eval(getParameter("onload")); 
//			} catch (Exception e) {
//				System.out.println("ONLOAD couldn't be executed");
//			}

//			}
	   super.start();
		
	   }
	
	public void stop() {
		MV = null;
		img = null;
		System.gc();
			
		super.stop();	
	}
	
	public void paint(Graphics g) {    	
		g.drawImage(img, 0,0,this.bounds().width,this.bounds().height, this);

		if (clickedMarker != null) {
			g.setColor(Color.blue);
			g.drawRect(clickedMarker.bounds.x, clickedMarker.bounds.y, clickedMarker.bounds.width, clickedMarker.bounds.height);
			//g.drawString("Viewing Marker in ZFIN Window...", clickedMarker.bounds.x + clickedMarker.bounds.width, clickedMarker.bounds.y -2);
		}
		
	}


	public boolean mouseDown(Event evt, int x, int y)  {

		if ((y < BackBone.TOP_SPACE -10) && (y > BackBone.TOP_SPACE-30)) { //clicking on a panel
			clickedPanel = null;
			clickedPanel = MV.panelClick(x,y);

		} else 	{ //clicking on a marker
			clickedMarker = null;
			clickedMarker = MV.click(x, y);

			if (clickedMarker != null) 	{
				repaint();
			}
		}
		
		return true;
		
		
	}

	public boolean mouseUp(Event evt, int x, int y) {
		if (clickedMarker != null) {
			viewMarker(clickedMarker.getZdb_id());
			clickedMarker = null;
			repaint();
		}

		if (clickedPanel != null) {
			viewPanel(clickedPanel);
			clickedPanel = null;
		}
		
		return true;
	}

	public boolean action (Event evt, Object obj) {
		Enumeration E = MV.elements();
		Vector V = new Vector();
		while(E.hasMoreElements())
			V.addElement(E.nextElement());
		int w = this.bounds().width/V.size();
		
		int p = 0;
		BackBone BB;
		if ("Zoom Out".equals((String)obj)) {
			p = evt.x/w;
			BB = (BackBone)V.elementAt(p);
			//System.err.println("p: " + p + " - " + V.elementAt(p));
			zoom(BB.getPanel_name(), (String)obj);
		} else if ("Zoom In".equals((String)obj)) {
			p = evt.x/w;
			BB = (BackBone)V.elementAt(p);
			//System.err.println("p: " + p + " - " + V.elementAt(p));
			zoom(BB.getPanel_name(), (String)obj);
		}
		
		
				
		   


		return true;
	}

	public void zoom(String panel_name, String button)  {

		getJS();

		
		
		String command = "document.optform.edit_panel.value=\"" + panel_name + "\"; ";
		command = command + " document.optform." + panel_name + "_zoom.value = ";

		int oldz;
		int z = (new Integer((String)JS.eval("document.optform." + panel_name + "_zoom.value"))).intValue();
		int ztotal = (new Integer(getParameter(panel_name + "_ztotal"))).intValue();
		oldz = z;
		if (button.equals("Zoom Out")) 
		{
			z = z + 20;
			z = z - (2*z); //go negative!
			command = command + z + ";";
		} else if (button.equals("Zoom In")) {
			if (z > 25)
				z = z - 20;
			else 
				z = z - 10;
			z = z - (2*z);

			command = command + z + ";";
		}

		if (( z > 0 ) && (  button.equals("Zoom In")  )) {
			JS.eval("alert(\"Can't zoom in any further.\");");
		} else if ((oldz == ztotal) && (z <= 0) && ( button.equals("Zoom Out"))) {
			JS.eval("alert(\"Can't zoom out any further.\");");
		} else {
			if (getParameter("zoom_url") != null)
				command = command + " document.optform.refresh_map.value=1; document.optform.target=\"pbrowser\"; document.optform.action=\"" + getParameter("zoom_url") + "\"; document.optform.submit();";
			else
				command = command + " document.optform.refresh_map.value=1; document.optform.target=\"pbrowser\"; document.optform.action=\"/cgi-bin_B/view_mapplet.cgi\"; document.optform.submit();";
			System.err.println(command);
			JS.eval(command);
		}
		
		
		
	}

	public void viewPanel(String Abbrev) {

		if ((getParameter("panel_url") != null) && (getParameter("port") != null) && (getParameter("host") != null))
		{
			String panel_url = getParameter("panel_url");
			String port = getParameter("port");
			String host = getParameter("host");
			SQLQuery Q = new SQLQuery(host,port);
			String query = "select zdb_id from panels where abbrev = '" + Abbrev + "';";
			Vector V = Q.select(1, query);

			if (V.size() == 1) {
				String ZDB_ID = (String)V.elementAt(0);
				getJS();
				String target_frame;
				if (getParameter("target_frame") != null)
					target_frame = getParameter("target_frame");
				else
					target_frame = "content";
					
				if ((JS != null) && (ZDB_ID != null)) {
					JS.eval("open(\"" + panel_url + "&OID=" + ZDB_ID + "\",\"" + target_frame + "\");");	
				}	
			}
		}
	}
	
	

	
	public void viewMarker(String ZDB_ID) {

		String marker_url;
		String target_frame;

		if (ZDB_ID.indexOf("FISH") > -1) {
			if (getParameter("fish_url") != null)
				marker_url = getParameter("fish_url");
			else	
				marker_url = "/cgi-bin_B/webdriver?MIval=aa-fishview.apg&OID=";
		} else if ( ZDB_ID.indexOf("LOCUS") > -1) {
			if (getParameter("locus_url") != null)
				marker_url = getParameter("locus_url");
			else	
				marker_url = "/cgi-bin_B/webdriver?MIval=aa-locusview.apg&OID=";
		} else {
			if (getParameter("marker_url") != null)
				marker_url = getParameter("marker_url");
			else	
				marker_url = "/cgi-bin_B/webdriver?MIval=aa-markerview.apg&OID=";
		}
		

		if (getParameter("target_frame") != null)
			target_frame = getParameter("target_frame");
		else
			target_frame = "content";

		
		getJS();

		if ((JS != null) && (ZDB_ID != null)) {
		    JS.eval("open(\"" + marker_url + ZDB_ID + "\",\"" + target_frame + "\");");	
		}
	}
	

	private void getJS ()
	{
		if (JS != null)
			return;
		try 
		{
			//System.err.println ("About to call getWindow ()");
			JS = JSObject.getWindow (this);
			//System.err.println ("Survived getWindow ()");
		}
		/*
		  If we caught this error, it is because the runtime environment
		  (browser or appletviewer) does not support class JSObject.
		*/
		catch (LinkageError error)
		{
			System.err.println ("In LinkageError clause");
			JS = null;

		}
		/*
		  If we caught an exception it could be because:
		  1.  The browser in question doesn't support calling JavaScript from Java
		      using JSObject.
	      2.  MAYSCRIPT wasn't set in the <APPLET ... > tag.
		  3.  (unlikely) We took an error on the String conversion.
		*/
		catch (Exception exception)
		{
			System.err.println ("In Exception clause");
			JS = null;
		}
	}
	

	
   
		
}
/*
	  + " and (target_abbrev='MGH' or target_abbrev='LN54' or target_abbrev='MOP' or target_abbrev='GAT') "
       + " and lg_location > -1 and lg_location < 120 */
