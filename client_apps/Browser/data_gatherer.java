//--------------------------------------------------------------
// FileName : data_gatherer.java
// Author   : Matt Sottile (matt@cs.uoregon.edu)
// Version  : 1.0
//
// Purpose  : Provide a class to retrieve data for the other
//            parts of the browser without imposing a specific
//            method of data transfer on the other classes.
//--------------------------------------------------------------
import java.util.*;
import java.net.*;
import java.io.*;
import java.applet.*;

// CLASS : data_gatherer
public class data_gatherer {
  Vector rs, rn, tn, tv, systems, organs;  // Vectors to hold data
  Applet pappy; //parent applet

  SQLQuery Q;

  // Nothing to do currently in constructor
  public data_gatherer(Applet app) {
    pappy = app;
    Q = new SQLQuery();
  }

  // silderRangeNames() : Returns a vector of strings containing the
  // names of the ranges for the Horizontal Slider.
  // *currently hardcoded for zebrafish.*
  public Vector sliderRangeNames() {
    rn = new Vector();
    rn.addElement("Cleavage");
    rn.addElement("Blastula");
    rn.addElement("Gastrula");
    rn.addElement("Segmentation");
    rn.addElement("Pharyngula");
    rn.addElement("Hatching");
    rn.trimToSize();

    return rn;
  }

  // sliderRangeSizes() : sizes of the ranges in "ticks" - each tick is
  // a single increment of time, so 7 ticks is seven consecutive time
  // points where data is available.
  public Vector sliderRangeSizes() {
    rs = new Vector();
    rs.addElement(new Integer(7));
    rs.addElement(new Integer(9));
    rs.addElement(new Integer(6));
    rs.addElement(new Integer(5));
    rs.addElement(new Integer(4));
    rs.addElement(new Integer(3));
    rs.trimToSize();

    return rs;
  }

  // This just returns the vector tn (tick names)
  public Vector sliderTickNames() {
    return tn;
  }

  // This just returns the vector tv (tick values)
  public Vector sliderTickValues() {
    return tv;
  }

  // This returns the systems vector (nervous, skeletal, muscular, etc..)
  public Vector fishSystems() {
    return systems;
  }

  // This returns the organ vector (brain, bone, heart, etc...)
  public Vector fishOrgans() {
    return organs;
  }

  // readStagesFromServer(app) - reads a flatfile from the server
  // which contains the data for the stage names.
  public void readStagesFromServer(Applet app) {
    StringTokenizer sTok;          // Use this to split each line into data
    String          s = null;
    String          s_name = null;
    String          s_num = null;
    String          s_time = null;
    InputStream     in; // Stream of input file
    URL             stageURL = null; // Location of data file (URL)
    DataInputStream dataIn;
    Float           s_time_temp;  // Used in time conversion
    double          s_time_dbl;

    tn = new Vector();  // Init tick name and tick value vector
    tv = new Vector();
    try {
      stageURL = new URL(app.getDocumentBase(), "stages.dat");
    } catch (MalformedURLException mue) { // Need this or compiler complains
    }

    try {
      in = stageURL.openStream();  // Open input stream
      dataIn = new DataInputStream(in); // Open datainput stream
      s = dataIn.readLine(); // Read a line of text
      while (s != null) {    // Go to end of stream (EOF)
		sTok = new StringTokenizer(s,":");  // Cut string on colons

		// String format = STAGE_NUMBER:STAGE_TIME:STAGE_NAME
		// NOTE : Number not used yet
		try {
			s_num = (String)sTok.nextElement();
			s_time = (String)sTok.nextElement();
			s_name = (String)sTok.nextElement();
		} catch (NoSuchElementException nsee) {
		}

		// Convert time from String to Double float
		s_time_temp = Float.valueOf(s_time);
		s_time_dbl = s_time_temp.doubleValue() * 24;

		// Add stuff to vectors
		tn.addElement(s_name);
		tv.addElement(new Double(s_time_dbl));

		// Get next line
		s = dataIn.readLine();
      }
    } catch (IOException io) {
    }

	// Save memory by trimming.
    tv.trimToSize();
    tn.trimToSize(); 
  }

  // IDENTICAL METHOD TO READ ANATOMY PARTS AS USED ABOVE FOR
  // STAGE DATA.  READ COMMENTS THERE AGAIN IF YOU NEED TO.

  public void readAnatomyFromServer(Applet app, String filename, String SEPARATOR) {

    System.out.println("reading data from file");

    StringTokenizer sTok;
    String          line = null;
    String          s_stage = null;
    String          s_indent = null;
    String          s_name = null;
    String          name = "";
    Integer         intTemp;
    int             count = 0;
    int             n_indent;
    int             stage;
    int             i;
    InputStream     in;
    URL             anatURL = null;
    DataInputStream dataIn;
    String          statString;

    organs = new Vector();
    systems = new Vector();

    try {
      anatURL = new URL(filename);
    } catch (MalformedURLException mue) {
    }

    try {
      in = anatURL.openStream();
      dataIn = new DataInputStream(in);
      line = dataIn.readLine();




      while (line != null) {
	//      System.out.println (line);
	sTok = new StringTokenizer(line,SEPARATOR);
	organs.addElement((String)sTok.nextElement());//name 
	organs.addElement(new Integer((String)sTok.nextElement()));//stage
	organs.addElement(new Integer((String)sTok.nextElement()));//level
	organs.addElement(new Integer((String)sTok.nextElement()));//seq_num 
	line = dataIn.readLine ();
      }
      
    } catch (IOException io) {
    }
    organs.trimToSize();
    systems.trimToSize();
  }
  
  public void readAnatomyFromDB() {
    String table;
    if (pappy.getParameter("table") == null)
      table = "anatomical_parts";
    else
      table = pappy.getParameter("table");


    organs = Q.selectAll(4, "SELECT name, stage, level, seq_num FROM " + table + " ORDER BY seq_num;");
  }
	
	public void readAnatomyFromParams() {
		String data = pappy.getParameter("data");

		if (data.endsWith("|"))
			data = data.substring(0,data.length()-1);
	   
		StringTokenizer sTok = new StringTokenizer(data, pappy.getParameter("data_delim"));
		organs = new Vector();
		String name;
		while (sTok.hasMoreTokens()) {
			name = sTok.nextToken();
			if (name.startsWith("\n"))
				name = name.substring(1,name.length()); //strip leading return

			organs.addElement(name);//name
			organs.addElement(new Integer((String)sTok.nextElement()));//stage
			organs.addElement(new Integer((String)sTok.nextElement()));//level
			organs.addElement(new Integer((String)sTok.nextElement()));//seq_num
		}
		
	}
	
	
	
  public Vector constraintQuery(String query) {
    return Q.selectAll(1, query);
  }
  

}

