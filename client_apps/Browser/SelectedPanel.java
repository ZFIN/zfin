import java.awt.*;
import java.applet.*;
import java.net.*;
import java.util.*;

public class SelectedPanel extends Panel {
  public java.awt.List SelectedList;
  public Vector SelectedVector;

  Applet pappy; //parent applet

  public SelectedPanel(Applet app) {
    pappy = app;

    SelectedList = new java.awt.List();
    SelectedVector = new Vector();

    //		this.setLayout(new BorderLayout());
    //		add(SelectedList);
   GridBagLayout gbl = new GridBagLayout();
   this.setLayout(gbl);
   SelectedList.setBackground(Color.white);
   GridBagConstraints gbc = new GridBagConstraints();
   gbc.ipadx = 2;
   gbc.ipady = 2;
   //    gbc.insets = new Insets(5,5,5,5);
   gbc.weightx = 1;
   gbc.weighty = 1;
   gbc.fill = GridBagConstraints.BOTH;
   gbc.anchor = GridBagConstraints.CENTER; 
   add(SelectedList, gbl, gbc, 0, 0, 1, 1, this); 
    

  }


  public void init() {
   /* GridBagLayout gbl = new GridBagLayout();
    this.setLayout(gbl);
    SelectedList.setBackground(Color.white);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.ipadx = 2;
    gbc.ipady = 2;
    //    gbc.insets = new Insets(5,5,5,5);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER; 
    add(SelectedList, gbl, gbc, 0, 0, 1, 1, this); */



		//addItem(new ListItem("one"));

  }
  /*
  public Dimension minimumSize() {
    //System.out.println("selectedPanel width: " + (getParent().size().width -50)/2);
    //return new Dimension((getParent().size().width -50)/2, this.size().height);
    System.out.println("Min size called");
    return new Dimension(150,150);
  } 
  public Dimension getMinimumSize() {
    return minimumSize();
  }
  */

	public String getList(String format_pre, String format_post, String separator, String recursive_select) {
		Vector V = SelectedVector;
		String S = new String();
    
    
		//security check
    
		String test = separator + " " +	format_pre + " " + format_post;
    
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
		// end secutiry check
		if (clear) {
      
			int i = 0;
			String results = "";
			String tmpstr = new String();

			System.out.println("Recrusive Select: " + recursive_select);
      
			for(i=0 ; i < V.size() ; i++) {
				tmpstr = new String();
				if (  (!((Selectable)V.elementAt(i)).get_children().isEmpty()) && (recursive_select != null)) //if it's got children
					tmpstr =  ((Selectable)V.elementAt(i)).expand(tmpstr,format_pre,format_post, recursive_select); 
				else
					tmpstr = format_pre + ((Selectable)V.elementAt(i)).get_data() + format_post;
	
				if (!results.equals("")) 
					results = results + separator;

				if (format_pre.length() > 0)
					results = results + " (" + tmpstr + ") ";	      
				else
					results = results + tmpstr;
			}
      
			return results;

			/*	    if (results.length() > 1)
					results = " ( " + results + " ) ";

					System.out.println(results);
	   
					for (i=0 ; i < V.size() ; i++) {
					if (!S.equals(""))
					S = S + separator;
					S = S + " " + format_pre + ((Selectable)V.elementAt(i)).get_data() 
					+ format_post + " ";
					}
			
					if (results.equals(""))
					return S;
					else
					return S + " " + separator + " " + results; */
		} else {
    	return "Securtity Violation";
    }
	
	}



  

  public boolean contains(String newString) {
    int i;
    SelectedVector.trimToSize();
    for (i = 0 ; i < SelectedVector.size() ; i++)
      if (newString.equals( ((Selectable)(SelectedVector.elementAt(i))).get_label()))
	return true;
    return false;
  }

  public void addItem(Selectable newNode) {
    if (!contains(newNode.get_label())) {
      SelectedVector.addElement(newNode);
      SelectedList.addItem(newNode.get_label());
    }
    SelectedList.select(SelectedVector.indexOf(newNode.get_label()));
  }


  public void moveSelected() {
    int[] selected;
    int i;

    Selectable node = null;

    selected = SelectedList.getSelectedIndexes();
    
    System.out.println(SelectedList.getItem(selected[0]) + " is no longer selected");

    String LUtable = "";
    String LUcol1 = "";
    String LUcol2 = "";
    String LUcol2val = "";

    if (pappy.getParameter("liveupdate") != null) {
      StringTokenizer LUstok = new StringTokenizer(pappy.getParameter("liveupdate"),":");
      LUtable = LUstok.nextToken();
      LUcol1 = LUstok.nextToken();
      LUcol2 = LUstok.nextToken();
      LUcol2val = LUstok.nextToken();
    }

    
    
    SelectedVector.trimToSize();
    for (i = 0 ; i < SelectedVector.size() ; i ++) {
      if (SelectedList.getItem(selected[0]).equals( ((Selectable)(SelectedVector.elementAt(i))).get_label()))
	node = (Selectable)(SelectedVector.elementAt(i));
    }
    if (node == null)
      System.out.println("Removal from SelectedList failed");
    
    SelectedList.delItem(selected[0]);
    SelectedVector.removeElement(node);
    
    if (pappy.getParameter("liveupdate") != null) {
 
      System.out.println("SQL: delete from " + LUtable + " where " + LUcol2 + "='" + LUcol2val + "' and " + LUcol1 + "='" + node.get_data() + "';");
      SQLQuery Q = new SQLQuery(pappy.getParameter("host"), pappy.getParameter("port"));
      String query = "delete from " + LUtable + " where " + LUcol2 + "='" + LUcol2val + "' and " + LUcol1 + "='" + node.get_data() + "';";
      Q.selectAll(1,query);
    }
    
  }
 
  public void clear() {
    SelectedList.clear();
    SelectedVector.removeAllElements();

  }

	

  public boolean action(Event e, Object o) {
    int[] selected;
    int i;

    if (e.target == SelectedList 
	&& SelectedList.countItems() > 0) {

      moveSelected();

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
