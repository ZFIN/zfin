import java.awt.*;
import java.applet.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class SystemsPanel extends Panel {
  Browser pappy; //parent applet
  
  public Label nameLabel; 
  public Label stageLabel1, stageLabel2;
  
  public TextField nameField;
  
  public Choice textChoice;
  public Choice firstStage;
  public Choice lastStage;
  public Button applyConstraints;


  public Checkbox and, or;
  public CheckboxGroup cbg;
  public String andString, orString;
  public  Label separatorLabel;

	public Button submit;
	
  public SystemsPanel(Browser app) {
    pappy = app;

     nameLabel     = new Label("Name"); // Name label
    stageLabel1   = new Label("and between");
    stageLabel2   = new Label("and");
 
    nameField     = new TextField(11); // Textfield for name
    textChoice    = new Choice();      // choice of how to text search
    firstStage    = new Choice();
    lastStage     = new Choice();

    separatorLabel = new Label("Find mutants with"); 
		if (pappy.getParameter("andString") != null)
			andString = pappy.getParameter("andString");
		else
    	andString = new String("ALL listed defects        ");

		if (pappy.getParameter("orString") != null)
			orString = pappy.getParameter("orString");
		else
	    orString = new String("ANY listed defects        ");
		
			
    cbg = new CheckboxGroup();
	and = new Checkbox(andString, cbg, true);
	or = new Checkbox(orString, cbg, false);
	
	if (pappy.getParameter("selected_separator") != null) {
		if (pappy.getParameter("selected_separator").equals("and")) {
			and.setState(true);
			or.setState(false);
		} else if (pappy.getParameter("selected_separator").equals("or")) {
			or.setState(true);
			and.setState(false);
		}
	}
	
	
	submit = new Button("Submit");
	
    applyConstraints = new Button("Search"); 

  }

  public void init() {


    textChoice.addItem("starts with");
    textChoice.addItem("contains");

    int i;
    for (i = 1 ; i <= 24 ; i++) {
      firstStage.addItem((new Integer(i)).toString());
    }

    for (i = 24; i >= 1 ; i--) {
      lastStage.addItem((new Integer(i)).toString());
    }

    //textChoice.setBackground(Color.white);
    //nameField.setForeground(((Browser)pappy).defaultbgColor);
    //nameLabel.setForeground(((Browser)pappy).defaultbgColor);
    //applyConstraints.setBackground(((Browser)pappy).defaultbgColor);


    this.setLayout(new GridLayout(1,0,4,0));
    add(separatorLabel);
    add(and);
    add(or);

	if (pappy.getParameter("submit") != null)
		add(submit);
    /*    GridBagLayout gbl = new GridBagLayout();
    this.setLayout(gbl);

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.ipadx = 1;
    gbc.ipady = 1;
    gbc.insets = new Insets(1,1,1,1);
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
 
    

   
       add(nameLabel,  gbl, gbc, 0, 0, 1, 1, this);
    add(textChoice, gbl, gbc, 1, 0, 1, 1, this);
 
    gbc.anchor = GridBagConstraints.WEST;
    add(nameField, gbl, gbc, 2, 0, 1, 1, this);
    add(stageLabel1, gbl, gbc, 3, 0, 1, 1, this);
    add(firstStage, gbl, gbc, 4, 0, 1, 1, this);
    add(stageLabel2, gbl, gbc, 5, 0, 1, 1, this);
    add(lastStage, gbl, gbc, 6, 0, 1, 1, this);

    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;
    add(applyConstraints, gbl, gbc, 7, 0, 1, 1, this); */
  

    lastStage.select(0);
    nameField.requestFocus();

  }
 
  public void apply() {
    System.out.println("starting constraint application");
/*
    String query = "SELECT seq_num FROM anatomical_parts WHERE ";
    String textClause = "";
    String stageClause = ""; //what, no santaClause?

    if (((textChoice.getSelectedItem()).equals("starts with")) 
	&& (firstStage.getSelectedItem().equals("1"))
	&& (lastStage.getSelectedItem().equals("24"))) {   //not database
      
      ((Browser)getParent()).treePanel.treeCanvas.startsWith(nameField.getText());

    } else {

      if (!((nameField.getText()).equals(""))) { //constraining text
	if((textChoice.getSelectedItem()).equals("starts with"))
	  textClause = "name LIKE '" + nameField.getText() + "%' ";
	else
	  textClause = "name LIKE '%" + nameField.getText() + "%' ";
      } 

      if ( !( (firstStage.getSelectedItem().equals("1"))
	   && (lastStage.getSelectedItem().equals("24")))) {

	if ( !(textClause.equals("")) ) 
	  stageClause = "AND ";
	stageClause = stageClause + "stage >= '" + firstStage.getSelectedItem() 
	  + "' and stage <= '" + lastStage.getSelectedItem() + "' ";
      }
      query = query + textClause + stageClause + ";";
      System.out.println("Query: " + query);
      Vector results = ((Browser)getParent()).Data.constraintQuery(query);
      ((Browser)getParent()).treePanel.treeCanvas.applyConstraints(results);
  }


    System.out.println("finished constraint application");

    System.out.println(((Browser)getParent()).treePanel.treeCanvas.visTable.size() + " parts left after constraint");
	*/
  }

  public boolean action(Event e, Object o) {

    if (e.target == applyConstraints) 
      apply();
    if (e.target == nameField) 
      apply();
	if (e.target == submit)
		pappy.submit();
	
    return false;
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
