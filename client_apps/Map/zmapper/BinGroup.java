package zmapper;

import java.util.*;
import java.awt.*;

public class BinGroup {

	//object used during drawing, by layoutmanager, to hold an ordered group of Bins
	
	public Rectangle bounds;
	public Integer OffsetY;
	private Vector Bins;

	private BinGroup Prev,Next;
	
	
	public BinGroup(Bin B) { 
		Bins = new Vector();
		OffsetY = new Integer(0);

		Bins.addElement(B);

	}
	
	public void add_to_bottom(BinGroup BG) 	{
		Enumeration E = BG.getBins();
		Bin B;
		while (E.hasMoreElements()) {  //this is dependent on ordering, will that hold?
			B = (Bin)E.nextElement();
			Bins.addElement(B);

		}
		
	}

	public boolean overlaps(BinGroup BG) {
		return this.bounds.intersects(BG.bounds);
	}

	public Enumeration getBins() 	{
		return Bins.elements();
	}
	
	public int getSize() 	{
		return Bins.size();
	}
	
	
	public void setOffsetY(Integer Y) 	{
		OffsetY = Y;
		if (bounds != null) 		{
			bounds.y = Y.intValue();
		} else 		{
			System.err.println("bounds is null, why?");
		}
		
		
		}

	public Integer getOffsetY() {
		return OffsetY;
	}
	
	public String toString() {
		String result = "";
		Enumeration E = Bins.elements();
		while (E.hasMoreElements()) 
		{
			result = result + " " + E.nextElement(); 
		}
		
		
		return result;

	}
	
	public void setPrev(BinGroup BG) { Prev = BG; 	}
	public void setNext(BinGroup BG) { Next = BG; 	}
	public BinGroup getPrev() { return Prev; }
	public BinGroup getNext() { return Next; }
	
	public void doLayout(int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH) {
		Enumeration E = this.getBins();

		Rectangle R;
		Bin B;
		bounds = new Rectangle();
		bounds.x = BIN_INDENT;
		bounds.y = OffsetY.intValue();
		bounds.width = MAX_WIDTH - BIN_INDENT;
		bounds.height = 0;

		int lines = 0;
		int y = OffsetY.intValue();
		
		while(E.hasMoreElements()) {

			B = (Bin)E.nextElement();
			
			R = new Rectangle();
			R.x = BIN_INDENT;
			R.y = y;
			B.calculateBounds(MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
			R.width = B.bounds.width;
			R.height = B.bounds.height;

			y = y + R.height;
			bounds.add(R);
			B.bounds = R;
			
		}
		
		
	}
	
	
}
