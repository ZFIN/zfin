//----------------------------------------------------------------------
// Filename : OrganTree.java
// Author   : Matt Sottile (matt@cs.uoregon.edu)
// Version  : Pre-1.0
//
// Purpose  : Store the data representing anatomical data in a tree and
//            allow browsing of the tree structure
//----------------------------------------------------------------------
import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.util.*;
import java.lang.*;



// CLASS   : OrganTree
// PURPOSE : Allow the user to browse the tree structure representing the
//           anatomy.
class OrganTree extends Canvas {
  TreeNode  root = new TreeNode("All Organs",null,new Integer(-1));  // Root the tree - change this.
  TreeNode  selectedNode;

  public    Font myFont;
  public    Font myFontB;

  public    FontMetrics myFontMetrics;

  long       currentClick, clickInterval, firstClick;
  

  int       nHeight;      // Node height (pixels)
  int       indentSize;   // Distance indented (x axis) to paint
  int       boxSize = 9;  
  int       myDescent;    // Font descent
  int       organCount;   //number of organs
  Hashtable coordTable;   // Use this for fast lookups on visible tree parts
  Hashtable visTable;     // stores the set of visible nodes,
                          // the object itself is the key, a boolean 
                          //value of true is the data
  Hashtable visChTable;   //this table will tell us if a parent has visible children
  Hashtable seqTable;
  Hashtable nameTable;

  Applet    iApplet;      // Applet that owns the tree
  public Dimension OnDimension;
  public Dimension OffDimension;
  public int totalHeight;
  
  SelectedPanel selectedPanel;

  //double buffering stuff
  protected Image OffImage = null;
  protected Graphics OffGraphics;
  public int dy; //difference scrolled
 
  // Constructor
  public OrganTree(Applet applet) {
    iApplet = applet;
    selectedPanel = ((Browser)applet).getSelectedPanel();
    root.isClosed = false;
    visTable = new Hashtable();
    visChTable = new Hashtable();
    seqTable = new Hashtable();
    nameTable = new Hashtable();
  }



  public void fillTree(Vector organs, Vector systems) {

    seqTable.clear();
    nameTable.clear();
    int i = buildTree(organs, null, -1, 0);


    System.out.println("Built " + new Integer(i/3) + " organs");
    if (iApplet.getParameter("open_all") != null)
      viewAll(root,true);
    else
      viewAll(root, false);
  }

  int buildTree(Vector organs, TreeNode parent, int level, int i) {

    TreeNode tn;

    if (level != -1) { //don't need to add root

      
      tn = parent.addChild((String)organs.elementAt(i), 
			   (Integer)organs.elementAt(i+3));
      seqTable.put((Integer)organs.elementAt(i+3), tn);
      nameTable.put((String)organs.elementAt(i), tn);

      i = i + 4;
      //    System.out.println("Adding " + organs.elementAt(i) 
      //	       + ", " + organs.elementAt(i + 1)
      //	       + ", " + organs.elementAt(i + 2));    
    }
    else
      tn = root;

    while((i < organs.size())
	  && 
   (((Integer)organs.elementAt(i+2)).intValue() > level)) {  //the next one is a child of this one
      i = buildTree(organs, tn, level+1, i);
	
    }

    //it's either a leaf node, or we're done with all the children
    return i;
  }
  
  // TraverseTree : Traverses the tree in a recursive fashion.  Returns the
  // current height so the traversal stops when it runs out of visible space.
  public int traverseTree(Graphics g, TreeNode node, int level, int y) {

    TreeNode child;
    int      curHeight = y;
      
    if ((visTable.get(node) != null) && ( visTable.get(node).equals(Boolean.TRUE))) {


      Polygon  aPoly = new Polygon();
      // These polygons are simply the arrows pointing right if the node is
      // closed and down if it's open.  level*indentSize is used to compute
      // the appropriate x offset from the left side of the canvas.

      if ((visChTable.get(node) != null) 
	  && ( visChTable.get(node).equals(Boolean.TRUE))) {

	if (node.isClosed && node.children.size() > 0) {
	  aPoly.addPoint(level*indentSize,y);
	  aPoly.addPoint(level*indentSize+boxSize,y+3);
	  aPoly.addPoint(level*indentSize,y+6);
	  aPoly.addPoint(level*indentSize,y);
	  
	} else if (!node.isClosed && node.children.size() > 0) {
	  aPoly.addPoint(level*indentSize,y);
	  aPoly.addPoint(level*indentSize+9,y);
	  aPoly.addPoint(level*indentSize+4,y+6);
	  aPoly.addPoint(level*indentSize,y);
	  
	}

	g.setColor(Color.darkGray);
	//	g.fillPolygon(aPoly);
	g.drawPolygon(aPoly);

      } 
      
      // Put this node in the hashtable of coordinates for quick lookup in mouse
      // routines.
      coordTable.put(""+curHeight,node);
      node.set_height(curHeight);


      g.setColor(Color.black);

      if (node == selectedNode) {
      OffGraphics.setColor(Color.black);
      OffGraphics.fillRect(boxSize+2+(node.getLevel()*indentSize),
			   node.get_height(),
			   myFontMetrics.stringWidth(node.get_data()), 
			   nHeight);
      OffGraphics.setColor(Color.white);
      }
      
      if (node.getLevel() < 2) 
	g.setColor(Color.blue);

      g.drawString(node.get_data(),boxSize+2+(level*indentSize),
		   y+nHeight-myDescent);
    } else 
      if (curHeight - nHeight > 0) 
	curHeight = curHeight - nHeight; 
      else 
	curHeight = 0;  //don't want it to go negative
      
    
    // Now traverse the current node's children if needed.
    for (int i = 0; i < node.children.size(); i++) {
      child = (TreeNode)node.children.elementAt(i);
      if (!node.isClosed) {
	curHeight += nHeight;
	curHeight = traverseTree(g,child,level+1,curHeight);
      }
    }

    return curHeight; // Return the current height (pixels) of the tree
                      // which is, non-intuitively enough, nHeight
                      // more than it should be.. 
  }
  
  public void drawImage() {
    System.out.println("drawImage");
    indentSize = 20;
    
    if ((OffImage == null) ||
        (this.size().height != OffDimension.height)) {
      OffDimension = new Dimension(this.size().width,6000);
      OffImage = createImage(this.size().width, OffDimension.height);
      if (OffImage == null)
        return; //sometimes it doesn't get an image
      OffGraphics = OffImage.getGraphics();
    }

    OffGraphics.setColor(new Color(255,255,255));
    OffGraphics.fillRect(0,0,this.size().width,OffDimension.height);
    
    myFont = new Font("Helvetica", Font.PLAIN, 10);
    myFontB =  new Font("Helvetica", Font.BOLD, 10);

    OffGraphics.setFont(myFont);
    myFontMetrics = OffGraphics.getFontMetrics(myFont);
    nHeight = myFontMetrics.getMaxAscent();
    myDescent = myFontMetrics.getDescent();

    
    coordTable = new Hashtable();
    totalHeight = traverseTree(OffGraphics,root,0,0) + nHeight;
    System.out.println("Total Height       : " + totalHeight);
    System.out.println("OffDimension height: " + OffDimension.height);
    System.out.println("treeCanvas height  : " + this.size().height);
    System.out.println("OffImage height    : " + OffImage.getHeight(this));
    if (totalHeight <= 0)
      totalHeight = 1;
    
    ((TreePanel)getParent()).updateScrollers();
    //repaint();
    update(this.getGraphics());
  }


  public void drawChildren(TreeNode TN) {
    TreeNode tn = TN;

    //this chunk of code may seem sort of odd, I feel the need to explain myself...
    //the reason we're getting the subtreeHeight is so we know how much to move
    //things around when we copy and paste.. we just have to cheat a little, because
    // if tn is closed, the subtree value will just be nHeight (11 right now)

    boolean closedValue;
    closedValue = tn.isClosed;
    tn.isClosed = false;
    int subtreeHeight = updateHeight(tn,tn.getLevel(),tn.get_height()) - tn.get_height() + nHeight;
    tn.isClosed = closedValue;

    System.out.println("subtree height: " + subtreeHeight);

    if (TN.isClosed == false) {
      
      //move the bottom portion down,
      OffGraphics.copyArea(0,(tn.get_height())+nHeight,     //x,y
			   this.size().width, 
			   (totalHeight - (tn.get_height()+nHeight)), //size of box
			   0,subtreeHeight - nHeight); //offset
      //draw the middle
      OffGraphics.setColor(new Color(255,255,255));
      
      OffGraphics.fillRect(0,(tn.get_height()),this.size().width,
			    subtreeHeight);
      
      //viewAll(tn, false);
      traverseTree(OffGraphics, tn, tn.getLevel(), tn.get_height());
    } else { // it's closed

     //first move the bottom up
      
      OffGraphics.copyArea(0,subtreeHeight + tn.get_height(),//x,y
			   this.size().width, 
			   (totalHeight 
			    - (subtreeHeight)), //size of box
			   0,- (subtreeHeight - nHeight)); //offset

      OffGraphics.setColor(new Color(255,255,255));
      
      OffGraphics.fillRect(0,(tn.get_height()),
			   this.size().width,nHeight); 
      //viewAll(tn,false);
      traverseTree(OffGraphics, tn, tn.getLevel(), tn.get_height());
      
    }
    

    coordTable.clear();
    totalHeight=updateHeight(root,0,0)+nHeight;

    OffGraphics.setColor(new Color(255,255,255));
    OffGraphics.fillRect(0,totalHeight,this.size().width,8000);

    repaint();

    ((TreePanel)getParent()).updateScrollers();

    System.out.println("Total Height       : " + totalHeight);
    System.out.println("OffDimension height: " + OffDimension.height);
    System.out.println("treeCanvas height  : " + this.size().height);
    System.out.println("OffImage height    : " + OffImage.getHeight(this));
    
  }

  public void drawSelected(TreeNode node) {
    if (node == selectedNode) {
      OffGraphics.setColor(Color.black);
      OffGraphics.fillRect(boxSize+2+(node.getLevel()*indentSize),
			   node.get_height(),
			   myFontMetrics.stringWidth(node.get_data()), 
			   nHeight);
      OffGraphics.setColor(Color.white);
      OffGraphics.drawString(node.get_data(),boxSize+2+(node.getLevel()*indentSize),node.get_height()+nHeight-myDescent);
    } else {
      OffGraphics.setColor(Color.white);
      OffGraphics.fillRect(boxSize+2+(node.getLevel()*indentSize) ,
			   node.get_height(),
			   myFontMetrics.stringWidth(node.get_data()), 
			   nHeight);

      if (node.getLevel() < 2) 
	OffGraphics.setColor(Color.blue);
      else 
	OffGraphics.setColor(Color.black);
      OffGraphics.drawString(node.get_data(),boxSize+2+(node.getLevel()*indentSize),node.get_height()+nHeight-myDescent);
    }
    repaint();
  }




  public void startsWith(String substr) {

    if (substr.equals(""))
      viewAll(root, false);
    else
      starts_with(root, substr);
    drawImage();
  }

  private void starts_with(TreeNode node, String substr) {
    if (node == root) {
      visTable.clear();
      visChTable.clear();
    }

    if (node.get_data().startsWith(substr)) {
      visTable.put((Object)node, (Object) new Boolean(true));

      TreeNode parent = node.get_parent();

      //      if (parent != null)
      //visChTable.put((Object)parent, (Object) new Boolean(true));	

      while(parent != null) {
	 visTable.put((Object)parent, (Object) new Boolean(true));
	 parent.isClosed = false;
	 visChTable.put((Object)parent, (Object) new Boolean(true));
	 parent = parent.get_parent();
      }
    }

    for (int i = 0; i < node.children.size(); i++) {
      starts_with((TreeNode)node.children.elementAt(i), substr);
    }

  }

  public void applyConstraints(Vector visables) {
    visTable.clear();
    TreeNode tn, parent;
    int i;
    

    for (i = 0 ; i < visables.size() ; i++) {
      tn = (TreeNode)seqTable.get(visables.elementAt(i));
      visTable.put((Object)tn, (Object) new Boolean(true));

      parent = tn.get_parent();

      while(parent != null) {
	 visTable.put((Object)parent, (Object) new Boolean(true));
	 parent.isClosed = false;
	 visChTable.put((Object)parent, (Object) new Boolean(true));
	 parent = parent.get_parent();
      }

    }
    drawImage();
  }


  public void hideNode(TreeNode node) {

    //keep everything coherent and correct
    visTable.remove(node);
    if (selectedNode == node)
      selectedNode = null;

    //move everything below up
    OffGraphics.copyArea(0, node.get_height()+nHeight,  //x,y
			 this.size().width, totalHeight, //box size
			 0, -nHeight); //offset

    updateHeight(root, 0, 0);

    //fill in the carbage underneath with whitespace
    OffGraphics.setColor(new Color(255,255,255)); 
    OffGraphics.fillRect(0,totalHeight, this.size().width, nHeight);

    repaint();
    
    //drawImage();
  }

  public void hideNodes(Vector nodes) {
    int i = 0;
    nodes.trimToSize();
    for (i = 0 ; i < nodes.size() ; i++) 
      hideNode((TreeNode)nodes.elementAt(i));
  }

  public void showNode(TreeNode node) {
    visTable.put((Object)node, (Object) new Boolean(true));

    //OffGraphics.copyArea(0,

    //traverseree(OffGraphics, node, node.getLevel(), node.get_height());

    drawImage();
  } 



  public void showNodes(Vector nodes) {
    int i = 0;
    nodes.trimToSize();
    for (i = 0 ; i < nodes.size() ; i++) 
     showNode((TreeNode)nodes.elementAt(i));
  }


  public void moveSelected() {
    System.out.println("moving " + selectedNode.get_data());
    selectedPanel.addItem(selectedNode);
  }

  public void selectNodes(Vector Nodes) {
    Enumeration E = Nodes.elements();
    while(E.hasMoreElements()) 
      selectedPanel.addItem((TreeNode)E.nextElement());
  }


  public Dimension getMinimumSize() {
    return minimumSize();
  }

  public Dimension minimumSize( ) {
    System.out.println("organtree minimumsize called ------");
    return new Dimension(175,75);
  }

  public int updateHeight(TreeNode node, int level, int y) {

    TreeNode child;
    int      curHeight = y;
    
    if (node == root)
      coordTable.clear();

    if ((Boolean)visTable.get(node) != null) {
      
      coordTable.put(""+y,node);
      node.set_height(y);
      
      //System.out.println("updating: " + node.get_data() + ", " + node.height);
      
      for (int i = 0; i < node.children.size(); i++) {
	child = (TreeNode)node.children.elementAt(i);
	if (!node.isClosed) {
	  curHeight += nHeight;
	  curHeight = updateHeight(child,level+1,curHeight);
	}
      }
    } else 
      { if (curHeight - nHeight > 0) 
	return curHeight - nHeight;
      else return 0; 
      }
    return curHeight;
    
}


  public void viewAll(TreeNode node, boolean openAll) {
    TreeNode child;

    if (node == root) {
      visTable.clear();
    }
    
    visTable.put((Object)node, (Object) new Boolean(true));

    if (node.get_parent() != null) {
       visChTable.put((Object)node.get_parent(), (Object) new Boolean(true));
    }

    if (openAll == true) 
      node.isClosed = false;

    for (int i = 0; i < node.children.size(); i++) {
      child = (TreeNode)node.children.elementAt(i);
      viewAll(child,openAll);
    }
  }

  public void openAll(TreeNode node) {
   TreeNode child;

   node.isClosed = false;

   for (int i = 0; i < node.children.size(); i++) {
      child = (TreeNode)node.children.elementAt(i);
      openAll(child);
    }

  }

 // Graphical routine - required by class Canvas.
  public void paint(Graphics g) {

    
    if (-dy > 0)
      dy = 0;

    if ((dy > totalHeight - this.size().height) && totalHeight > this.size().height)
      dy =  totalHeight - this.size().height; 

    g.translate(0,-dy); //scrollystuff
    if (OffImage == null) 
      drawImage();
    else
      g.drawImage(OffImage,0,0,this);

  }

  public void update(Graphics g) {

    if (-dy > 0)
      dy = 0;
    

    if ((dy > totalHeight - this.size().height) && totalHeight > this.size().height)
      dy =  totalHeight - this.size().height;

    g.translate(0,-dy); //scrollystuff
    if (OffImage == null) 
      drawImage();
    else
      g.drawImage(OffImage,0,0,this); 
  }

  public void doSelectedAction(TreeNode N) {
    TreeNode tmp =null;
    String result = "";
    tmp = N;

    while (tmp != null) {
      result = tmp.get_data() + "::" + result;
      tmp = tmp.get_parent();
      if (tmp == root)
	tmp = null;
    }

    ((Browser)iApplet).doSelectedAction(N.get_data(),result);
  }


  // MouseDown event handler - very straight-forward.
  public boolean mouseDown(Event e, int x, int y) {
    int yd;
    TreeNode clicked;
    int clickCount;

    // double clicks count, anything else is out
    currentClick  = System.currentTimeMillis();
    clickInterval = currentClick - firstClick;
    firstClick    = currentClick;
 
    if (clickInterval > 200)
      clickCount = 1;
    else
      clickCount = 2;

    y = y + dy; //dy scrolled distance
    yd = (int)(y/nHeight) * nHeight;
    clicked = (TreeNode)coordTable.get(""+yd);

    System.out.println(clicked.get_data() + " at " + clicked.get_height() + " : " + e); 
    
    if ((clicked != null) && (clickCount == 1)
	&& (x < boxSize+2+(clicked.getLevel()*indentSize))
	&& (clicked.children.size() > 0)) {
      System.out.println("Open " + clicked.get_data());

      

      if (clicked.isClosed) 
	clicked.isClosed = false;
      else 
	clicked.isClosed = true;

      if (e.shiftDown() && !clicked.isClosed)
	openAll(clicked);

      drawChildren(clicked);

    } else if ((clicked != null)&&(clickCount == 1)
	  && (x >= boxSize+2+(clicked.getLevel()*indentSize) )) {
	TreeNode previouslySelected = selectedNode;
	if (clicked == selectedNode)
	  selectedNode = null;
	else {
	  selectedNode =  clicked;

	  doSelectedAction(clicked);

	  if (previouslySelected != null 
	      && ((Boolean)visTable.get(previouslySelected) != null))
	    drawSelected(previouslySelected);
	}
	drawSelected(clicked);
      } 
    
    if ((clicked != null) && (clickCount == 2)) {
      TreeNode previouslySelected = selectedNode;  

      if (previouslySelected != null 
	  && ((Boolean)visTable.get(clicked) != null))
	drawSelected(previouslySelected);
      selectedNode = clicked;
      drawSelected(clicked);
      
      moveSelected();

    } 


    return true;
  }
}
