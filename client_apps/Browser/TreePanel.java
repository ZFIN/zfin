import java.awt.*;
import java.applet.*;
import java.net.*;
import java.util.*;

public class TreePanel extends Panel {
  OrganTree treeCanvas;
  Scrollbar treeScroller;

  public Color bgColor, lightColor, darkColor;



  Browser pappy; //parent applet
  
  public TreePanel(Applet app) {
    pappy = (Browser)app;

    treeCanvas = new OrganTree(pappy);
    treeScroller = new Scrollbar(Scrollbar.VERTICAL);
    bgColor = new Color (0,102,102);
    lightColor = new Color (0,153,153);
    darkColor = new Color (0,51,51);

    setBackground(bgColor);
  }

  public void init() {

    setBackground(Color.white);
    GridBagLayout gbl = new GridBagLayout();
    this.setLayout(gbl);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.ipadx = 2;
    gbc.ipady = 2;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;

    // Tree scroller
    treeScroller.setValues(0,10,0,1600); // Arbitrary values for testing
    // gbc.insets = new Insets(15,15,15,0);
    add(treeCanvas, gbl, gbc, 0, 0, 1, 1, this);
    gbc.weightx = 0;
    gbc.weighty = 0;
    //gbc.insets = new Insets(15,0,15,15);   
    add(treeScroller, gbl, gbc, 1, 0, 1, 1, this);

  }



  /*  public Dimension minimumSize() {
    System.out.println("treepanel width: " + (getParent().size().width -50)/2);
    return new Dimension((getParent().size().width)/2, this.size().height);
    }*/

  public Dimension getMinimumSize() {
    return minimumSize();
  }

  public Dimension minimumSize( ) {
    System.out.println("treepanel minimumsize called ------");
    return new Dimension(175,75);
  }


  public void moveSelected() {
    treeCanvas.moveSelected();
  }


  public boolean handleEvent(Event e)  {
    //System.out.println("Treecanvas height: " + treeCanvas.size().height);
    if (e.id == Event.SCROLL_ABSOLUTE)    {
      //System.out.println(treeScroller);
      //treeCanvas.translate(treeScroller.getValue());
      treeCanvas.dy = ((Integer)e.arg).intValue();
      treeCanvas.update(treeCanvas.getGraphics());//drawImage();
    }
    if (e.id == Event.SCROLL_LINE_DOWN) {
      //the default line incriment is 1, that's the starting point to be
      //modified for the correct value.. 
      //System.out.println(treeScroller);
      treeCanvas.dy = ((Integer)e.arg).intValue() + 10;
      treeScroller.setValue(((Integer)e.arg).intValue() + 10); 
      treeCanvas.update(treeCanvas.getGraphics());
    }
     
    if (e.id == Event.SCROLL_LINE_UP) {
      //System.out.println(treeScroller);
      treeCanvas.dy = ((Integer)e.arg).intValue() - 10;
      treeScroller.setValue(((Integer)e.arg).intValue() - 10); 
      treeCanvas.update(treeCanvas.getGraphics());//drawImage();
    }
    
    if (e.id == Event.SCROLL_PAGE_UP) {
      //System.out.println(treeScroller);


      treeCanvas.dy = ((Integer)e.arg).intValue() - treeCanvas.size().height; 
      treeScroller.setValue(((Integer)e.arg).intValue() - treeCanvas.size().height);

      treeCanvas.update(treeCanvas.getGraphics());//drawImage(); 
    }

   if ( e.id == Event.SCROLL_PAGE_DOWN) {
      //the default page amount is 10, so we need to twiddle with that to get
      //the value we want.
     //System.out.println(treeScroller);
      treeCanvas.dy = ((Integer)e.arg).intValue() + treeCanvas.size().height;
      treeScroller.setValue(((Integer)e.arg).intValue() + treeCanvas.size().height);
      treeCanvas.update(treeCanvas.getGraphics());//drawImage();
    }
    return super.handleEvent(e);
  }
  
  void updateScrollers()  {
    System.out.println("updating scrollers");
     
    if ( (treeCanvas.size().width <= 0)||(treeCanvas.size().height <=0)||(treeScroller == null))
      {
	System.out.println("Canvas has no height or width");
	return;
      }
    
    int max;

    if (treeCanvas.totalHeight > treeCanvas.size().height)
      max = treeCanvas.totalHeight + treeCanvas.size().height;  
    else
      max =  treeCanvas.size().height;

    if (treeCanvas.totalHeight > treeCanvas.OffDimension.height)
      max = treeCanvas.OffDimension.height;


    treeScroller.setValues(treeCanvas.dy, 
			   (int)(treeCanvas.size().height * 0.9), 0,max-treeCanvas.size().height); 
			   
			   // treeCanvas.totalHeight+treeCanvas.nHeight - treeCanvas.size().height);

    //treeScroller.setPageIncrement((int)(treeCanvas.size().height * 0.9));
    //treeScroller.setLineIncrement(20);
    
    //treeScroller.setValues(treeScroller.getValue(),treeCanvas.size().height,0,treeCanvas.OffDimension.height - treeCanvas.size().height);
    
    //scrollVert.reshape(size().width-15,-1,16,size().height-13);
    //scrollHoriz.reshape(-1,size().height - 15,size().width-13,16);
  }
   
  public void paint(Graphics g) {
    updateScrollers();

  }
  
  private void add(Component c, GridBagLayout gbl, GridBagConstraints gbc,
                   int x, int y, int w, int h, Container obj) {
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.gridwidth = w;
    gbc.gridheight = h;
    gbl.setConstraints(c, gbc);
    obj.add(c);
  }

  public void fillTree() {

    if (pappy.Data.organs == null) {
      SQLQuery Q = new SQLQuery(pappy.getParameter("host"),pappy.getParameter("port"));
      
      String table;
      if (pappy.getParameter("table") == null)
	table = "anatomical_parts";
      else
	table = pappy.getParameter("table");
      
      
      Vector organs = Q.selectAll(4, "SELECT name, stage, level, seq_num FROM " + table + " ORDER BY seq_num;");

      
      treeCanvas.fillTree(organs,null); //the null is a systems vector,
    } else
      treeCanvas.fillTree(pappy.Data.organs,null);
																			//I don't know why it was there,
																			//but I'll leave it just incase...
  }


/*  public void fillTree(Vector organs, Vector systems) { 
    Vector Organs, Systems;
    Organs = organs;
    Systems = systems;
    treeCanvas.fillTree(Organs, Systems); 
  } */





}
