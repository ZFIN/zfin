package zmapper;

import java.util.*;
import java.awt.*;

public class BackBone {

	private  String panel_name;
	private Integer OR_lg;
	private String metric;
	
	private Float low_loc, high_loc;
	
	private LayoutManager LM;
	private Hashtable BinTable;
	private Vector Bins; //the ordered list
	private Hashtable FWTable; //Maps Marker.abbrev to Bin for framework markers

	private Marker selected_marker; //the selected marker for this backbone;
	
	public int X; //this is the offset in the X axis, updated within the draw method
	public int MAX_WIDTH; //this is the width of the backbone object, also updated by draw
	
	public static final int TOP_SPACE = 65; //was 40 and 20 - k, 8/28/00
	public final int BOTTOM_SPACE = 20;
	
	public final int LOC_WIDTH = 40; //space to draw distance labels
	public final int DIAG_WIDTH = 35;//space to draw diag connecting lines
	public final int BB_WIDTH = 7;
	
	public final int BACKBONE_INDENT = LOC_WIDTH + DIAG_WIDTH; //space to draw location label
	public final int BIN_INDENT = (2 * (LOC_WIDTH + DIAG_WIDTH)) + BB_WIDTH;

	public static final Font F = new Font("SansSerif", Font.PLAIN, 10);
	public static final FontMetrics FM = Toolkit.getDefaultToolkit().getFontMetrics(F);
	public static final Font BoldF = new Font("SansSerif", Font.BOLD, 13);
	public static final FontMetrics BoldFM = Toolkit.getDefaultToolkit().getFontMetrics(BoldF);
	public static Font BBFont = new Font("SansSerif", Font.BOLD, 20);
	public static FontMetrics BBFM =  Toolkit.getDefaultToolkit().getFontMetrics(BBFont);
	
	private int marker_count;

	public boolean watermark_t;
	
	public BackBone (String panel_name, Integer OR_lg, String metric, boolean watermark_t)	{
		this.watermark_t = watermark_t;
		this.panel_name = panel_name;	
		this.OR_lg = OR_lg;
		this.metric = metric;
		this.marker_count = 0;
		low_loc = new Float(99999);
		high_loc = new Float(-99999);
		
		LM = new LayoutManager(this);
		BinTable = new Hashtable();
		Bins = new Vector();
		FWTable = new Hashtable();

		//System.err.println("FONT HEIGHT: " + FM.getHeight());
		
	}

	public void addMarker(Marker M)	{
		//places markers in Bins based on lg_location
		//System.out.println("adding (" + M.getLg_location() + ", " + M.getAbbrev() + ") to " + panel_name);

		marker_count++;
		
		if (M.getSelected())
			this.setSelected(M);
			
		
		low_loc = new Float(Math.min(M.getLg_location().floatValue(), low_loc.floatValue()));
		high_loc = new Float(Math.max(M.getLg_location().floatValue(), high_loc.floatValue()));

		if (BinTable.get(M.getLg_location()) == null) {
			Bin B = new Bin(M.getLg_location());
			BinTable.put(M.getLg_location(), B);
			Bins.addElement(B);
			B.addMarker(M);
			if (M.getFramework_t().booleanValue() == true)
				FWTable.put(M.getAbbrev(), B);
		} else {	
			Bin B = (Bin) BinTable.get(M.getLg_location());
			B.addMarker(M);
			if (M.getFramework_t().booleanValue() == true)
				FWTable.put(M.getAbbrev(), B);
		}
		
	}

	//simple accessors
	public Enumeration getBins() { return Bins.elements();	}
	public int getBinCount() { return Bins.size(); }
	public String getPanel_name() { return panel_name; }
	public String getMetric() { return metric; 	}
	public Float getHigh_loc() { return high_loc; }
	public Float getLow_loc()  { return low_loc; }
	public Marker getSelected() 	{return selected_marker;}
	public void setSelected(Marker M) { selected_marker = M;}
	public int getMarker_count() { return marker_count; }
	
			
	   
			
			

	
/* Obviouslly it's the markers that have framework status, not the bins,
   but we're drawing the lines between bins, rather than exactly from marker
   to marker.

*/
   public Hashtable getFrameworkBins( ) {
	   return FWTable;
   }
	
	
	
	public void draw (Graphics g, int X, int MAX_WIDTH, int HEIGHT) {
		Enumeration E = Bins.elements();
		this.X = X;
		this.MAX_WIDTH = MAX_WIDTH;

		Bin B;

		//draw the backbone label
		g.setColor(Color.black);
		g.setFont(BoldF);
		g.drawString(panel_name + " panel, LG: " + OR_lg + ", units: " + getMetric(), X + 5, TOP_SPACE - 15);
		if (watermark_t == true)
			g.fillRect(X + 5, TOP_SPACE - 13, BoldFM.stringWidth(panel_name), 2);

		//draw watermarks

		if (watermark_t == true) {
			
			g.setFont(BBFont);
			g.setColor(new Color(200,200,200));

			int i; 
			for (i = HEIGHT/300 ; i > 0 ; i--) {
				if ( (HEIGHT - (i*300)) > 100)
					g.drawString(getPanel_name(), X+BACKBONE_INDENT - (BBFM.stringWidth(getPanel_name())/2) - 5  , TOP_SPACE + (i*300));
			
			}
		}
		
		   

		//draw the backbone itself
		g.setColor(Color.black);
		g.fillRect(X+BACKBONE_INDENT-1, TOP_SPACE, 2, HEIGHT - (TOP_SPACE + BOTTOM_SPACE));


		//lay out the bins

		Hashtable YTable = LM.doLayout(HEIGHT, MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
		
		
		//draw the bin labels, bin location labels & diagonal connecting lines

		int y; //= TOP_SPACE;

		int bby = 0;
		int h;

		float last_loc = -1;
		int last_y = -1;
		int last_h = -1;
		Float last;
		int last_tmp;
		
		while (E.hasMoreElements()) {
			B = (Bin)E.nextElement();
			y = ((Integer)YTable.get(B)).intValue(); //find height to draw bin label
			h = B.draw(g,X,y-3,MAX_WIDTH,BIN_INDENT,DIAG_WIDTH); //bin draws it's own label, returns it's height


			
			g.setColor(Color.black);
			bby = getBackBoneY(B, HEIGHT); //translate location -> pixel
			g.drawLine(X + BACKBONE_INDENT-4, bby, X + BACKBONE_INDENT+3, bby);	//draw bin position on backbone
			g.setFont(F);
			g.drawString(B.getLg_location().toString(), X, y + (h/2) + 4); //draw bin location label to the left

			if (last_loc != -1) {
				last = new Float(B.getLg_location().floatValue() - last_loc);
				last_tmp = (int) ( last.floatValue() * 100);
				last = new Float(((float)last_tmp)/100);
				//System.out.println(B.getLg_location().toString() + ", " + last_loc);
				g.setFont(F);
				g.drawString(last.toString(), X + BIN_INDENT - LOC_WIDTH + 2, 4 + ((last_y + last_h/2) + (y + h/2))/2);
			}
				
			last_loc = B.getLg_location().floatValue();
			last_y = y;
			last_h = h;
			
			//the diagonals:
		    g.drawLine(X + BACKBONE_INDENT-4, bby, X + LOC_WIDTH , y + (h/2) ); //connecting left to location label
			g.drawLine(X + LOC_WIDTH, y + (h/2), X + LOC_WIDTH-8, y + (h/2)); //little horiz line from diag to loc label
			g.drawLine(X + BACKBONE_INDENT+3, bby, X + BIN_INDENT - LOC_WIDTH -3 , y + h/2); //connecting right to Bin label
			g.drawLine(X + BIN_INDENT - LOC_WIDTH - 3, y + h/2, X + BIN_INDENT - 3, y + h/2); //horiz line to Bin label

			
		}
		
	}

	public void drawFramework(Graphics g, int X, int MAX_WIDTH, int HEIGHT, BackBone BB, int bb_index) {
		//System.err.println("Connect " + this.getPanel_name() + " to " + BB.getPanel_name());

		Hashtable LeftTable = BB.getFrameworkBins();

		Color BBColor = new Color(120,120,120);
		
		Enumeration E = FWTable.keys();
		String S;
		Bin LeftBin, RightBin;

		int leftX, rightX;
		int leftY, rightY;
		
		while(E.hasMoreElements()) {
			S = (String)E.nextElement();
			if (LeftTable.get(S) != null) {
				LeftBin = (Bin) LeftTable.get(S);
				RightBin = (Bin) FWTable.get(S);

				leftX = (X - MAX_WIDTH ) + BIN_INDENT +  LeftBin.bounds.width + 1;
				leftY = LeftBin.bounds.y + (LeftBin.bounds.height/2);

				//System.err.println(S + ": " + LeftBin.bounds.width);
				
				rightX = X;
				rightY = RightBin.bounds.y + (RightBin.bounds.height/2);
				
				if (LeftBin.containsSelected())
					g.setColor(Color.red);
				else
					g.setColor(BBColor);

				if (LeftBin.bounds.height > LeftBin.LINE_HEIGHT) //more than one line, draw the vertical bar
					g.drawLine(leftX, LeftBin.bounds.y + 5,
							   leftX, LeftBin.bounds.y + LeftBin.bounds.height - 6);

				g.drawLine(leftX, leftY, X - DIAG_WIDTH, leftY);

				if (LeftBin.containsSelected() && RightBin.containsSelected())
					g.setColor(Color.red);
				else
					g.setColor(BBColor);
				
				g.drawLine(rightX -5, rightY, X - DIAG_WIDTH, leftY);

				g.drawLine(rightX, rightY, rightX - 5, rightY);

				//System.err.println(LeftBin + " -- " + X + ", " + leftY + ", " + rightX + ", " + rightY);
				//System.err.println("Connect " + RightBin + " to " + LeftBin); 
				
			}
		}
		
	}
	
	
	public int getBackBoneY(Bin B, int HEIGHT) {
		int y = 0;

		float hl = high_loc.floatValue(); 
		float ll = low_loc.floatValue();

		float totaldist = hl - ll;
		float yl = (B.getLg_location().floatValue()) - ll;

		int hp = HEIGHT - (BOTTOM_SPACE);
		int lp = TOP_SPACE;
		int totalpixels = hp - lp;
		
		//y = (int) (totalpixels * ((yl/totaldist) - ll));

		y = (int) ((yl/totaldist) * (float)totalpixels);
			
		
		y = y + TOP_SPACE;

		return y;
	}
	
	public boolean contains(int x, int y) {
		if ((x > X) && (x < (X + MAX_WIDTH))) 
			return true;
		else
			return false;
	}
	
	public Marker click(int x, int y)  {
		Marker clickedMarker = null; 
		Bin B;
		Enumeration E = Bins.elements();
		while (E.hasMoreElements()) {
			B = (Bin)E.nextElement();
			if ( (y > B.bounds.y) && (y < (B.bounds.y + B.bounds.height))) {
				clickedMarker = B.click(x, y);
				continue;
			}

		}
		return clickedMarker;
	}

	public String panelClick(int x, int y) 	{
		if ((x >= (X + 5)) && (x <= (X + 5 + BoldFM.stringWidth(getPanel_name()))))
			return getPanel_name();
		else
			return null;
	}
	
	

	
   	public String toString() {
		return  panel_name + " panel: " + Bins.size() + " bins (" + low_loc + ", " + high_loc + ")";
	}
	
}
