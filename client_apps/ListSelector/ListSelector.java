import java.awt.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class ListSelector extends Applet {

   public SelectedPanel selected;
   public SelectablePanel selectable;
	 public SystemsPanel sysPanel;

   public Button toSelected;
   public Button toSelectable;

   public Color defaultbgColor, bgColor, lightColor, darkColor;

	 public String selectedLabel,selectableLabel;

  public boolean liveupdate;
  public String LUtable;
  public String LUcol1;
  public String LUcol2;
  public String LUcol2val;

   public ListSelector() {
   
   
   }
   
   public void init() {
   	selected = new SelectedPanel(this);
		selectable = new SelectablePanel(this);   	
		sysPanel = new SystemsPanel(this);
   	sysPanel.init();
    toSelected = new Button(">>>");
    toSelectable = new Button("<<<");
   

    selectedLabel = getParameter("selectedLabel");
    selectableLabel = getParameter("selectableLabel");

    if (getParameter("preselected") != null) {
      Vector V = new Vector();
      StringTokenizer stok = new StringTokenizer(getParameter("preselected"),
						 getParameter("preselected_delim"));
      while(stok.hasMoreTokens()) {
	V.addElement(stok.nextToken());
      }
      selectable.selectItems(V, getSelectedPanel());
    }

    if (getParameter("pub_id") != null) {
      SQLQuery Q = new SQLQuery(this);

      String query = "select full_name, zdb_id from person, int_person_pub where zdb_id = source_id and target_id = '" + getParameter("pub_id") + "';";

      Vector V = Q.selectAll(2, query);
      Enumeration E = V.elements();
      String D = new String();
      String L = new String();
      ListItem LI = null;

      while(E.hasMoreElements()) {
	L = (String)E.nextElement();
	D = (String)E.nextElement();
	System.out.println("label & data: " + L + " , " + D); 
	LI = new ListItem(D,L);

	//	selectables.addItem(LI.get_label());
	//selectHash.put(LI.get_label(), LI); 

	selected.addItem(LI);
	

      }
    }
      

    /*    if (getParameter("authorlist") != null) {
      System.out.println(authorTrim(getParameter("authorlist")));
      }*/
    
    if (getParameter("liveupdate") != null) {
      liveupdate = true;
      StringTokenizer LUstok = new StringTokenizer(getParameter("liveupdate"),":");
      LUtable = LUstok.nextToken();
      LUcol1 = LUstok.nextToken();
      LUcol2 = LUstok.nextToken();
      LUcol2val = LUstok.nextToken();
    }




   	defaultbgColor = Color.lightGray;
    bgColor = new Color(136,166,166);
    lightColor = new Color (0,153,153);
    darkColor = new Color (0,51,51);

    setBackground(bgColor);
    toSelected.setBackground(defaultbgColor);
    toSelectable.setBackground(defaultbgColor);
	
		selected.setBackground(Color.yellow);


		    //applet gui layout
    GridBagLayout gbl = new GridBagLayout();
    this.setLayout(gbl);

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.ipadx = 2;
    gbc.ipady = 2;
  
    if ((selectedLabel==null) && (selectableLabel==null))
      gbc.insets = new Insets(5,5,5,5);
    else
      gbc.insets = new Insets(15,15,5,15);



    gbc.weightx = 100;
    gbc.weighty = 100;

    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    add(selectable, gbl, gbc, 0, 0, 4, 6, this);

    gbc.anchor = GridBagConstraints.NORTHEAST;
    add(selected, gbl, gbc, 6, 0, 4, 5, this);

    gbc.fill = GridBagConstraints.NONE;

    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.CENTER;
    add(toSelected, gbl, gbc, 5, 4, 1, 1, this);    
    gbc.anchor = GridBagConstraints.SOUTH;
    add(toSelectable, gbl, gbc, 5, 4, 1, 1, this); 

    gbc.fill=GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.SOUTH;
    gbc.weighty = 0;
    gbc.insets = new Insets(5,5,5,5);
    if (getParameter("separator") == null)
      add(sysPanel, gbl, gbc, 0, 7, 11, 2, this);
   }
   
   
  public SelectedPanel getSelectedPanel() {
    return selected;
  }
   
  public void clear() {
   selected.clear();
   System.out.println("clear() method called");
  }
  
  public String getList() {
    Vector V = selected.SelectedVector;
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

		return selected.getList(format_pre,format_post,separator,"");
  }

  
  public boolean action(Event e, Object o) {

    if (e.target == toSelected) {
      selectable.moveSelected(selected);
    }

    if (e.target == toSelectable) {
      selected.moveSelected();

    }
        
    return true;
  }
 
   
 
   
   
   
   
  public void paint(Graphics g) {
		if (selectableLabel != null) {
    	NeXTRect(g,selectableLabel, 7, 7, 
	   	  selectable.size().width + 15, 
	   	  selectable.size().height + 15 ); 
	  }
		if (selectedLabel != null) {
	    NeXTRect(g,selectedLabel, 15 + selectable.size().width+toSelected.size().width+50, 7,
		     selected.size().width + 15, 
		     selected.size().height + 15); 
 		}
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


  public Vector authorTrim(String authors) {

	  if ((authors.indexOf(", and") == -1) && (authors.indexOf(" and ") > -1)) { //has exactly two authors
		  int i = authors.indexOf(" and");
		  authors = authors.substring(0,i) + ", and" + authors.substring(i+4);
		  System.out.println(authors);
	  }
	  
	  StringTokenizer stok = new StringTokenizer(authors,",");
	  String nT = null;

	  Vector results = new Vector();
	  
	  while (stok.hasMoreTokens()) {
		  nT = stok.nextToken();
		  //System.out.println(nT + " , " + nT.indexOf("."));
		  if (  nT.indexOf(".") == -1  ) { //contains no periods, ie, it's a last name
	
			  if (nT.startsWith(" and "))
				  nT = nT.substring(4);

			  if (nT.startsWith(" "))
				  nT = nT.substring(1); //trim leading space

			  //nT = nT.toLowerCase();
	
			  results.addElement(nT);
			  System.out.println(nT);
			  //	if (results == "")
			  //  results = nT;
			  //else	
			  //  results = results + " , " + nT;
	
		  }
	  }
		
	  

	
		return results;
	}


  /* this method was written by Scott Herz (herz@cs.uoregon.edu), whatta guy */

  public void NeXTRect(Graphics g,String label,int TopX, int TopY, int width, int height)  {       
      Font textFont = new Font("Helvetica", Font.PLAIN, 12);
      FontMetrics metrics = getFontMetrics(textFont); 
      int textWidth  = metrics.stringWidth(label);
      int textHeight  = metrics.getHeight();
      
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
   
