import java.awt.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;
import netscape.javascript.*;

public class Browser extends Applet {

  // --- Interface goodies ---
  //HSPanel sliderPanel;
  public TreePanel treePanel;
  public SelectedPanel selectedPanel;
  data_gatherer Data;
  SystemsPanel sysPanel;

  public JSObject win;

  public Button toSelectedPanel;
  public Button toTreePanel;
  public Button Done;

  public Color defaultbgColor, bgColor, lightColor, darkColor;
  public Font textFont; 
  public int textWidth, textHeight;

	public String selected_label,list_label;

  public Browser() {

  }
 
  public void init() {
    System.out.println("custom labels"); //version control, it's nice too know if netscape is reloading

    JSObject win = null;


/*		Panel P = new Panel();
		P.setBackground(Color.red);
		Label L = new Label("Applet Loading...");
		setLayout(new BorderLayout());

		validate(); //forces layout stuff...?
*/				

    //sliderPanel = new HSPanel(this);

		//parameter goodies

    if (getParameter("selected_label") != null)
      selected_label = getParameter("selected_label");
    
    if (getParameter("list_label") != null)
      list_label = getParameter("list_label");
    
    String test = getParameter("separator") + " " +
      getParameter("format_pre") + " " + getParameter("format_post");
    

    

		//make all my little doodads
    	
    selectedPanel = new SelectedPanel(this);
    treePanel = new TreePanel(this);
    Data = new data_gatherer(this);
    sysPanel = new SystemsPanel(this);

    toSelectedPanel = new Button(">>>");
    toTreePanel = new Button("<<<");
    Done = new Button("Done");

    defaultbgColor = Color.lightGray;
    //    bgColor = new Color (0,102,102);
    bgColor = new Color(136,166,166);
    lightColor = new Color (0,153,153);
    darkColor = new Color (0,51,51);

    setBackground(bgColor);




    
    //get the data
    System.out.println("choosing 'tween the database and the file");

    String AF, AF_delim;

    if (getParameter("anatomyfile") != null) {
      if (getParameter("anatomyfile_delim") != null)
	AF_delim = getParameter("anatomyfile_delim");
      else 
	AF_delim = ":";
      
      if (getParameter("anatomyfile").startsWith("http"))
	AF = getParameter("anatomyfile");
      else
	AF = getCodeBase() + getParameter("anatomyfile");
      
      Data.readAnatomyFromServer(this, AF, AF_delim);

      System.out.println("Geting data from: " + AF + " separating with: " + AF_delim);

    }  else { 
      Data.readAnatomyFromDB();
      System.out.println("database");
    }
    //    sliderPanel.init(Data.sliderRangeSizes(), Data.sliderRangeNames(),
    //	     Data.sliderTickNames(), Data.sliderTickValues());
    selectedPanel.init();
    //treePanel.fillTree(Data.fishOrgans(), Data.fishSystems());
    treePanel.fillTree();
    treePanel.init();
    sysPanel.init();


    //If there's data to preselect, preselect it!
    if (getParameter("preselected") != null) {
      Vector V = new Vector();
      StringTokenizer stok = new StringTokenizer(getParameter("preselected"),
						 getParameter("preselected_delim"));
      String tmp;
      TreeNode tn;
      Integer I;
      String S;

      if (getParameter("preselected_format") != null) {
	if (getParameter("preselected_format").equals("int")) {
	  while(stok.hasMoreTokens()) {
	    tmp = stok.nextToken();

	    try {
	      I = new Integer (Integer.parseInt(tmp));
	      tn = (TreeNode)treePanel.treeCanvas.seqTable.get(I);
	      if (tn != null)
		V.addElement(tn);
	    }
	    catch (NumberFormatException e) { 
	      System.out.println("While building preselected list: " + e);
	    }
	    
	   
	  }
	  
	} else if (getParameter("preselected_format").equals("String")) {
	  while(stok.hasMoreTokens()) {
	    tmp = stok.nextToken();
	    S=tmp;
	    
	    tn = (TreeNode)treePanel.treeCanvas.nameTable.get(S);
	    if (tn != null)
	      V.addElement(tn);
	  }
	}
	
      } else {
	while(stok.hasMoreTokens()) {
	  
	  
	  tmp = stok.nextToken();
	  try {
	    I = new Integer (Integer.parseInt(tmp));
	    tn = (TreeNode)treePanel.treeCanvas.seqTable.get(I);
	    if (tn != null)
	      V.addElement(tn);
	  }
	  catch (NumberFormatException e) { 
	    S=tmp;
	    
	    tn = (TreeNode)treePanel.treeCanvas.nameTable.get(S);
	    if (tn != null)
	      V.addElement(tn);
	  }
	  
	  //System.out.println( treePanel.treeCanvas.seqTable.get(I));
	}
      }
      treePanel.treeCanvas.selectNodes(V);
    }
    


    toSelectedPanel.setBackground(defaultbgColor);
    toTreePanel.setBackground(defaultbgColor);
    Done.setBackground(defaultbgColor);

    //applet gui layout
    GridBagLayout gbl = new GridBagLayout();
    this.setLayout(gbl);

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.ipadx = 2;
    gbc.ipady = 2;

    if ((list_label==null) && (selected_label==null))
      gbc.insets = new Insets(5,5,5,5);
    else
      gbc.insets = new Insets(15,15,5,15);




    gbc.weightx = 100;
    gbc.weighty = 100;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTH;

    gbc.weightx=50;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    add(treePanel, gbl, gbc, 0, 0, 5, 5, this);

    if (getParameter("view_only")==null) {

      gbc.weightx=0;
      gbc.anchor = GridBagConstraints.NORTHEAST;
      add(selectedPanel, gbl, gbc, 8, 0, 4, 5, this);
      
      gbc.fill = GridBagConstraints.NONE;
      gbc.weightx = 0;
      gbc.weighty = 0;
      gbc.anchor = GridBagConstraints.CENTER;
      add(toSelectedPanel, gbl, gbc, 5, 4, 1, 1, this);    
      gbc.anchor = GridBagConstraints.SOUTH;
      add(toTreePanel, gbl, gbc, 5, 4, 1, 1, this); 
      
      gbc.anchor = GridBagConstraints.SOUTHEAST;
      //    add(Done, gbl, gbc, 9, 6, 1, 1, this); 
      
      gbc.fill=GridBagConstraints.HORIZONTAL;
      gbc.anchor = GridBagConstraints.SOUTH;
      gbc.weighty = 0;
      gbc.insets = new Insets(5,5,5,5);
      if (getParameter("separator") == null)
	add(sysPanel, gbl, gbc, 0, 6, 11, 1, this);
    }
    
  }


  public SelectedPanel getSelectedPanel() {
		return selectedPanel;
	}


  private void getWin() {
    if (win != null)
      return;
    try 
      {
	win = JSObject.getWindow (this);
      }
    catch (LinkageError error)
      {
	System.out.println(error);
	win = null;
      }
    catch (Exception exception)
      {
	System.out.println(exception);
	win = null;
      }
  }

  public void doSelectedAction(String node, String node_path) {
    if (getParameter("selectedAction") != null) {

      getWin();
      if (win != null) {
	System.out.println("doing selected action");

	String command = getParameter("selectedAction") + "(\"" + node +  "\",\"" + node_path + "\");";
	System.out.println(command);
	win.eval(command);
      } else {System.out.println("Unable to get JSObject");}


    }
      
  }


  public String getList() {
    Vector V = selectedPanel.SelectedVector;
    String S = new String();
    String separator;

    if (getParameter("separator") == null) 
     if (sysPanel.cbg.getCurrent().getLabel().equals(sysPanel.andString))
      separator = "and";
     else
      separator = "or";
    else
      separator = getParameter("separator");
    
    String format_pre = getParameter("format_pre");
    String format_post = getParameter("format_post");

    return selectedPanel.getList(format_pre,format_post,separator,getParameter("recursive_select"));

/*    System.out.println("separator & format string " 
		       + separator + " " + format_pre + format_post);

		//security check

    String test = getParameter("separator") + " " +
    							getParameter("format_pre") + " " + getParameter("format_post");
		
		boolean clear = true;
		StringTokenizer stok = new StringTokenizer(test);
		String temp = new String();
		while(stok.hasMoreTokens()) {
			temp = stok.nextToken();
			if ((temp.toLowerCase().equals("delete"))
 					|| (temp.toLowerCase().equals("update"))
 					|| (temp.toLowerCase().equals("insert"))) {
 				 System.out.println("Bad keywords in params");
				 clear = false;
 			}
			System.out.println("Token: '" + temp + "'");
 		}

		if (clear) {

	    int i = 0;
	    String results = "";

	    for(i=0 ; i < V.size() ; i++) {
	      if ( !((TreeNode)V.elementAt(i)).children.isEmpty())
		results = ((TreeNode)V.elementAt(i)).OrExpand(results, format_pre, format_post);
	    }
	    if (results != "")
	      results = " ( " + results + " ) ";

	    System.out.println(results);
	   
	    for (i=0 ; i < V.size() ; i++) {
	      if (!S.equals(""))
		S = S + separator;
	      S = S + " " + format_pre + ((TreeNode)V.elementAt(i)).get_data() 
		+ format_post + " ";
	    }
			
	    if (results.equals(""))
	      return S;
	    else
	      return S + " " + separator + " " + results; 
    } else {
    	return "Securtity Violation";
    } */
  }

  public void clear() {
   selectedPanel.clear();
   System.out.println("Browser.clear() method called");
  }

  
  public boolean action(Event e, Object o) {

    if (e.target == toSelectedPanel) {
      treePanel.moveSelected();
    }

    if (e.target == toTreePanel) {
      selectedPanel.moveSelected();
    }

    if (e.target == Done) {
      System.out.println("Done");
    }
    return true;
  }


  public void paint(Graphics g) {


		if (list_label != null) {
	    NeXTRect(g,list_label, 7, 7, 
		     treePanel.size().width + 15, 
		     treePanel.size().height + 15 );
		}
		if (selected_label != null) {
	    NeXTRect(g,selected_label, 15 + treePanel.size().width+toSelectedPanel.size().width+50, 7,
		     selectedPanel.size().width + 15, 
		     selectedPanel.size().height + 15);
		} 
    //    NeXTRect(g,"Find organs where", 7, treePanel.size().height + 30,
    //	     sysPanel.size().width + 15, sysPanel.size().height + 15);
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

  /* this method was written by Scott Herz (herz@cs.uoregon.edu), whatta guy */

  public void NeXTRect(Graphics g,String label,int TopX, int TopY, int width, int height)
    {       
      textFont            = new Font("Helvetica", Font.PLAIN, 12);
      FontMetrics metrics = getFontMetrics(textFont); 
      textWidth           = metrics.stringWidth(label);
      textHeight          = metrics.getHeight();
      
      g.setFont(textFont);
      
      g.setColor(darkColor);
      g.drawRect(TopX,TopY,width,height);
      
      g.setColor(lightColor);
      g.drawRect(TopX+1,TopY+1,width,height);
      
      g.setColor(bgColor);
      g.fillRect( (TopX + 15) , (TopY), (10 + textWidth), 2);
      
      g.setColor(Color.white);
      g.drawString(label,TopX + 20 , TopY + 4);
    }

}
