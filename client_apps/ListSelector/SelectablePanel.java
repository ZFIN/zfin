import java.awt.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SelectablePanel extends Panel {
	
  ListSelector pappy; //parent applet	
  java.awt.List selectables;
  Hashtable selectHash;
  
  public SelectablePanel(ListSelector app) { 
    pappy=app;

    selectHash = new Hashtable();

    selectables = new java.awt.List();
    selectables.setMultipleSelections(false);
    //		setLayout(new BorderLayout());
    //add(selectables);
    
    
    GridBagLayout gbl = new GridBagLayout();
    this.setLayout(gbl);
    selectables.setBackground(Color.white);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.ipadx = 2;
    gbc.ipady = 2;
    //    gbc.insets = new Insets(5,5,5,5);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER; 
    add(selectables, gbl, gbc, 0, 0, 1, 1, this); 
    
    
    
    
    if (pappy.getParameter("query") != null) {
      retrieveList(pappy.getParameter("query"));		
    } else if (pappy.getParameter("authorlist") != null) { //this is a very specific 'mode' for the applet
      String select = "Select full_name, zdb_id from  person where ";
      String clause = "";
      Vector A = pappy.authorTrim(pappy.getParameter("authorlist"));
      Enumeration E = A.elements();
      while(E.hasMoreElements()) {
	if (!clause.equals("")) // not empty
	  clause = clause + " or ";
	clause = clause + "full_name like \"%" + E.nextElement() + "%\"";
      }
      
      
      System.out.println("Q: " + select + clause + ";");
      retrieveList(select + clause + ";");
    } else {
       selectables.addItem("no query param");
    }
  }
  
  public void retrieveList(String query) {
	  SQLQuery Q = new SQLQuery(pappy.getParameter("host"), pappy.getParameter("port"));

    
    if ((pappy.getParameter("use_item_labels") != null) && (pappy.getParameter("use_item_labels").equals("true"))) {

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
	selectables.addItem(LI.get_label());
	selectHash.put(LI.get_label(), LI); 
      }
      

    } else {
      
      Vector V = Q.selectAll(1, query);
      Enumeration E = V.elements();
      String S = new String();
      ListItem LI = null;
      while(E.hasMoreElements()) {
	S = (String)E.nextElement();
	LI = new ListItem(S);
	selectables.addItem(LI.get_label());
	selectHash.put(LI.get_label(),LI); 
      }
    }
  }


  public void selectItems(Vector V, SelectedPanel SP) {
    Enumeration Pre = V.elements();
    while (Pre.hasMoreElements()) {
      String tmpstr = ((String)Pre.nextElement());
      if (selectHash.get(tmpstr) != null) {
	SP.addItem((ListItem)selectHash.get(tmpstr));
      }

    }
  }

  public void moveSelected(SelectedPanel SP) {
    
	  SQLQuery Q = new SQLQuery(pappy.getParameter("host"), pappy.getParameter("port"));
 
    if (pappy.liveupdate == true) {
      String query = "insert into " + pappy.LUtable + " (" + pappy.LUcol1 + "," + pappy.LUcol2 + ") values ('" + ((ListItem)selectHash.get(selectables.getSelectedItem())).get_data() + "','" + pappy.LUcol2val + "');";
      System.out.println(query);
      Q.selectAll(1,query);
      
      
    }

    SP.addItem( (ListItem)selectHash.get(selectables.getSelectedItem()));

    System.out.println("Selected, data: " + 
    ((ListItem)selectHash.get(selectables.getSelectedItem())).get_data() + " label: " + ((ListItem)selectHash.get(selectables.getSelectedItem())).get_label());

    

  }
  
  
  public boolean action(Event e, Object o) {
    int[] selected;
    int i;
    
    if (e.target == selectables 
	&& selectables.countItems() > 0) {
      
      moveSelected(((ListSelector)pappy).getSelectedPanel());
      
    }
    return true;
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
	

}
