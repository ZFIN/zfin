package zmapper;

import java.util.*;
import java.lang.*;
import java.awt.*;

public class Bin {

	Vector Markers;
	Float lg_location;

	int X, Y; //(0,0 of this box)

	int width;
	
	//static vars
	public static final Font F = new Font("SansSerif", Font.PLAIN, 10);
	public static final FontMetrics FM = Toolkit.getDefaultToolkit().getFontMetrics(F);
	public static final Font BoldF = F;//= new Font("SansSerif", Font.BOLD, 11);
	public static final FontMetrics BoldFM = FM;//Toolkit.getDefaultToolkit().getFontMetrics(BoldF);

	public int LINE_HEIGHT = FM.getHeight();
	public int INDENT = 80; //distance from start of BackBone 'area' to Bin label

	public Rectangle bounds;
	
	public Bin (Float lg_location)	{
		this.lg_location = lg_location;
		Markers = new Vector();
		width = 0;
		bounds = new Rectangle();

	}
	
	public void addMarker(Marker M) {
		
		Markers.addElement(M);
	}	

	public Float getLg_location() 	{
		return lg_location;
	}

	public boolean containsSelected() {
		Enumeration E = Markers.elements();
		boolean result = false;
		Marker M;
		while(E.hasMoreElements()) {
			M = (Marker)E.nextElement();
			if (M.getSelected() == true)
				result = true;
		}
		return result;
	}	
	
	
	public int getBinHeight(int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH) {
		//returns the height in pixels of the bin label
		Enumeration E = Markers.elements();
		String S = new String("");
		Marker M;
		String result = "";
		int ln = 1; //line number

		FontMetrics fm;
		
		while(E.hasMoreElements()) 	{
			M = (Marker)E.nextElement();
			S = M.getAbbrev();

			if (M.getFramework_t().booleanValue() == true) 	
				fm = BoldFM;
			else
				fm = FM;
			
			if (fm.stringWidth(S + "  " + result)  < MAX_WIDTH - BIN_INDENT - DIAG_WIDTH) { //fits on this line
				if (result != "")
					result = result + " ";
				result = result + S;

			} else {  //make a new line
				if (FM.stringWidth(result) > width)
					width = FM.stringWidth(result);
				result = S;
				ln++;
			}
		}			

		if (FM.stringWidth(result) > width)
			width = FM.stringWidth(result); //..for bounding rect

		
		return ln * LINE_HEIGHT;
	}

	public void calculateBounds(int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH)	{
		bounds.height = getBinHeight(MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
		bounds.width = width;
		
	}
	

	public int NoFakeBin_draw(Graphics g, int X, int Y, int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH) {

		Enumeration E = Markers.elements(); //the abbrevs
		String S;
		Marker M = null;
		Marker lastM = null;
		int ln = 1; //line number

		Font f;
		FontMetrics fm;

		g.setColor(Marker.SSLP_c);
		int w = 0;
		
		while(E.hasMoreElements()) 	{
			lastM = M;
			M = (Marker)E.nextElement();

//			if (M.getAbbrev().equals("gof9"))
//				System.err.println("gof9: " + M.getFramework_t());
			
			if (M.getFramework_t().booleanValue() == true) 	{
				f = BoldF;
				fm = BoldFM;
			} else {
				f = F;
				fm = FM;
			}
			
			g.setFont(f);
			
			S = M.getAbbrev();
			if ((lastM != null) && (M.isRelatedTo(lastM))) {
				
				if (w + fm.stringWidth(" " + S)  < MAX_WIDTH - BIN_INDENT - DIAG_WIDTH) { //fits on this line
					if (w != 0) { S = " " + S;	}
					g.setColor(M.getColor());
					g.drawString(S, X+BIN_INDENT+w, Y + (LINE_HEIGHT*ln));
					M.bounds = new Rectangle(X + BIN_INDENT + w +1 , Y + (LINE_HEIGHT*(ln-1)) +2,
											 fm.stringWidth(S) + 1, LINE_HEIGHT + 2);
					if (M.getSelected()) {
						g.setColor(Color.red);
						g.drawRect(M.bounds.x-1, M.bounds.y, M.bounds.width+1, M.bounds.height);
					}
				
					
					w = w + fm.stringWidth(S);
					if (w > bounds.width)
						bounds.width = w;
				} else {  //make a new line
					w = 0;
					ln++;
					g.setColor(M.getColor());
					g.drawString(S, X+BIN_INDENT+w, Y + (LINE_HEIGHT*ln));
					M.bounds = new Rectangle(X + BIN_INDENT + w -1 , Y + (LINE_HEIGHT*(ln-1)) + 2,
											 fm.stringWidth(S) +1, LINE_HEIGHT +2);
					if (M.getSelected()) {
						g.setColor(Color.red);
						g.drawRect(M.bounds.x-1, M.bounds.y, M.bounds.width+1, M.bounds.height);
					}	
					w = fm.stringWidth(S);
				}
			} else 	{
				w = 0;
				g.setColor(M.getColor());
				g.drawString(S, X+BIN_INDENT+w, Y + (LINE_HEIGHT*ln));
				M.bounds = new Rectangle(X + BIN_INDENT + w -1 , Y + (LINE_HEIGHT*(ln-1)) + 2,
										 fm.stringWidth(S) +1, LINE_HEIGHT +2);
				if (M.getSelected()) {
					g.setColor(Color.red);
					g.drawRect(M.bounds.x-1, M.bounds.y, M.bounds.width+1, M.bounds.height);
				}	
				w = fm.stringWidth(S);
			}
			
			
		}			

		if (ln > 1) { //if there's more than one, draw a vert line to visually 'group' the markers
			g.setColor(Color.black);
			g.drawLine(X+BIN_INDENT-3, Y + 8, X+BIN_INDENT-3, Y+(ln * LINE_HEIGHT)-3); 
		}
		

		
		//	g.setColor(Color.orange);
		//g.drawRect(X + bounds.x, bounds.y, bounds.width, bounds.height);
		
		
		return ln * LINE_HEIGHT;

	}
	
	//groups all markers with wordwrapping
	public int draw(Graphics g, int X, int Y, int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH) {

		Enumeration E = Markers.elements(); //the abbrevs
		String S;
		Marker M;
		int ln = 1; //line number

		Font f;
		FontMetrics fm;

		g.setColor(Marker.SSLP_c);
		int w = 0;
		
		while(E.hasMoreElements()) 	{
			M = (Marker)E.nextElement();

//			if (M.getAbbrev().equals("gof9"))
//				System.err.println("gof9: " + M.getFramework_t());
			
			if (M.getFramework_t().booleanValue() == true) 	{
				f = BoldF;
				fm = BoldFM;
			} else {
				f = F;
				fm = FM;
			}
			
			g.setFont(f);
			
			S = M.getAbbrev();
			if (w + fm.stringWidth(" " + S)  < MAX_WIDTH - BIN_INDENT - DIAG_WIDTH) { //fits on this line
				if (w != 0) { S = " " + S;	}
				g.setColor(M.getColor());
				g.drawString(S, X+BIN_INDENT+w, Y + (LINE_HEIGHT*ln));
				M.bounds = new Rectangle(X + BIN_INDENT + w +1 , Y + (LINE_HEIGHT*(ln-1)) +2,
										 fm.stringWidth(S) + 1, LINE_HEIGHT + 2);
				if (M.getSelected()) {
					g.setColor(Color.red);
					g.drawRect(M.bounds.x-1, M.bounds.y, M.bounds.width+1, M.bounds.height);
				}
				
					
				w = w + fm.stringWidth(S);
				if (w > bounds.width)
					bounds.width = w;
			} else {  //make a new line
				w = 0;
				ln++;
				g.setColor(M.getColor());
				g.drawString(S, X+BIN_INDENT+w, Y + (LINE_HEIGHT*ln));
				M.bounds = new Rectangle(X + BIN_INDENT + w -1 , Y + (LINE_HEIGHT*(ln-1)) + 2,
										 fm.stringWidth(S) +1, LINE_HEIGHT +2);
				if (M.getSelected()) {
					g.setColor(Color.red);
					g.drawRect(M.bounds.x-1, M.bounds.y, M.bounds.width+1, M.bounds.height);
				}	
				w = fm.stringWidth(S);
			}
		}			

		if (ln > 1) { //if there's more than one, draw a vert line to visually 'group' the markers
			g.setColor(Color.black);
			g.drawLine(X+BIN_INDENT-3, Y + 8, X+BIN_INDENT-3, Y+(ln * LINE_HEIGHT)-3); 
		}
		

		
		//	g.setColor(Color.orange);
		//g.drawRect(X + bounds.x, bounds.y, bounds.width, bounds.height);
		
		
		return ln * LINE_HEIGHT;

	}

	public Marker click(int x, int y) 	{
		Marker clickedMarker = null;

		Marker M;
		Enumeration E = Markers.elements();
		while (E.hasMoreElements()) {
			M = (Marker)E.nextElement();
			if (M.bounds.inside(x,y)) {
				clickedMarker = M;
				continue;
			}
			
		}
		
		return clickedMarker;
		
	}
	
		

	public String toString() {

		String S = lg_location + ": ";
		Marker M;
		
		Enumeration E = Markers.elements();
		while (E.hasMoreElements()) {
			M = (Marker) E.nextElement();
			S = S + M.getAbbrev() + " ";
		}
		
		return S;
	}	

}









