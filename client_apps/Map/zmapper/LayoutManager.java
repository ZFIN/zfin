package zmapper;

import java.util.*;
import java.awt.Rectangle;

public class LayoutManager {

	/* This class assigns a pixel Y to each Bin,
	 */

	BackBone BB; //parent backbone

	Vector Slots;
	
	public LayoutManager(BackBone BB) {
		this.BB = BB;

	}
	
	public Hashtable doLayout(int HEIGHT, int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH) {
		Hashtable RectTable = new Hashtable(); //maps Bin bounds to rect
		Hashtable YTable = new Hashtable();  //maps Y -> Bin

		
		//calculate bounds for each Bin
		Enumeration E = BB.getBins();
		Rectangle R ;
		Bin B;
		int y = 25;
		int BinH = 0;

		BinGroup BG = null;
		BinGroup lastBG = null;
		
		while(E.hasMoreElements()) {
			B = (Bin)E.nextElement();

			BinH = B.getBinHeight(MAX_WIDTH,BIN_INDENT, DIAG_WIDTH);
			y = BB.getBackBoneY(B,HEIGHT) - (BinH/2); 

//			System.err.println("HEIGHT: " + HEIGHT);
			//System.err.println("BinH: " + BinH);
			//System.err.println("y: " + y);
			
			if (y < BB.TOP_SPACE - 6) 
			{
				//System.err.println(BB + " - " + B + "--- Bin higher than top");
				//System.err.println(BB + " - " + B + "---- y: " + y);
				y = BB.TOP_SPACE - 6;
			}
			if ((y + BinH) > (HEIGHT - BB.BOTTOM_SPACE)) 
			{
				//System.err.println(BB + " - " + B + "--- Bin lower than bottom");
				//System.err.println(BB + " - " + B + "---- y: " + y + " - h: " + HEIGHT + " - BinH: " + BinH);
				y = HEIGHT - BB.BOTTOM_SPACE - BinH + 6;
			}
			
				
			//System.err.println(BB + " - " + B + "---- y: " + y);
			
			BG = new BinGroup(B);

			BG.doLayout(MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
			BG.setOffsetY(new Integer(y));

			if (lastBG != null) {	
				BG.doLayout(MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
				lastBG.doLayout(MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
				if (BG.overlaps(lastBG))  
				{
					lastBG.add_to_bottom(BG);
					BG = lastBG; //this var hangs around in the last iteration and is used below as the end BG
				} else 	{
					lastBG.setNext(BG);
					BG.setPrev(lastBG);
					lastBG=BG;
				}
			} else { lastBG = BG;  }
			
		}

		BinGroup BGtmp = BG;
		int lastYbottom;

		while(BGtmp.getPrev() != null) {
			

			if (BGtmp.getSize() > 1) {
			
				lastYbottom =  BGtmp.getPrev().bounds.y + BGtmp.getPrev().bounds.height;
				BGtmp.doLayout(MAX_WIDTH,BIN_INDENT, DIAG_WIDTH);
				if ( (BGtmp.bounds.y + BGtmp.bounds.height) > (HEIGHT)) // was HEIGHT+40
				{
					//System.err.println("BG GONE TO FAR TROUBLE!");
					BGtmp.setOffsetY(new Integer (HEIGHT - (BGtmp.bounds.height))); // was BGtmp.bounds.height+ 10
				} else if ((BGtmp.getNext() != null) && (BGtmp.overlaps(BGtmp.getNext()))) {
					//System.err.println("BG OVERLAP TROUBLE! " + BGtmp);
					BGtmp.setOffsetY( new Integer( BGtmp.getNext().bounds.y - BGtmp.bounds.height)); //I'm not sure if this can happen..				
				}
				
			

				if (BGtmp.overlaps(BGtmp.getPrev())) { //basically, this bit eliminates BG from the chain..
		   			//System.err.println("NUMBER 2 BG OVERLAP TROUBLE! " + BGtmp);

					BGtmp.getPrev().add_to_bottom(BGtmp);
					BGtmp.getPrev().setNext(BGtmp.getNext());
					if (BGtmp.getNext() != null)
						BGtmp.getNext().setPrev(BGtmp.getPrev());
					BGtmp.getPrev().doLayout(MAX_WIDTH,BIN_INDENT, DIAG_WIDTH); //recalculate the bounds..
				}
				
				
			}
			
			BGtmp = BGtmp.getPrev();
		}
		
		
		while (BG != null) {
			BG.doLayout(MAX_WIDTH, BIN_INDENT, DIAG_WIDTH);
			Enumeration EE = BG.getBins();
			while(EE.hasMoreElements()) {
				B = (Bin)EE.nextElement();
				YTable.put(B, new Integer(B.bounds.y));

			}
			BG = BG.getPrev();

		}
		
		
//		YTable = dumbLayout(HEIGHT,MAX_WIDTH,BIN_INDENT);

		

		
		return YTable;
	}


public Hashtable dumbLayout(int HEIGHT, int MAX_WIDTH, int BIN_INDENT, int DIAG_WIDTH) {
		Hashtable YTable = new Hashtable();  //maps Y -> Bin
		
		Enumeration E = BB.getBins();
		Bin B;
		int y;
		int BinH;
		//dumb and ugly single pass layout
		while (E.hasMoreElements()) {
			B = (Bin) E.nextElement();

			y = BB.getBackBoneY(B, HEIGHT);

			BinH = B.getBinHeight(MAX_WIDTH,BIN_INDENT, DIAG_WIDTH);

			y =  y - (((BinH)/2));  // + (B.LINE_HEIGHT);
			
			YTable.put(B, new Integer(y));
		}
		
		

		return YTable;
	}

	
	
}
