package zmapper;

import java.awt.*;

public class Marker {

	private String zdb_id;
	private String abbrev;
	private String mtype;
	private String target_abbrev;
	private Float lg_location;
	private Integer OR_lg;
	private Boolean framework_t;
	private Boolean selected_t;
	private String metric;
	
	public Rectangle bounds;
	
//	public static Color GENE_c = new Color(20 , 60 ,20); //darker green
	public static Color GENE_c = new Color(0,0,204);//blue

	public static Color GENEP_c = GENE_c; //lighter blue?

	public static Color EST_c = new Color(0,153,0); //lighter green

	//public static Color SSLP_c = new Color(0,0,204); //med blue
	public static Color SSLP_c = new Color(102,51,0); //brown
	public static Color RAPD_c = new Color(102,51,0);  //brown
	public static Color STS_c = new Color(102,51,0); //brown
//	public static Color SSR_c = SSLP_c;
	public static Color SNP_c = new Color(240,100,240);  //magenta light
//	public static Color FISH_c = new Color(102,51,0);
	public static Color FISH_c = Color.red;
	public static Color BAC_c = new Color(0,51,0);
	public static Color PAC_c = new Color(0,51,0);
	
	public Marker(String zdb_id, String abbrev,	String mtype,
				  String target_abbrev, String mm_chrom_location, String OR_lg, String framework_t, String metric)  {

		this.zdb_id = zdb_id;

		if (!this.zdb_id.startsWith("Z"))
			this.zdb_id = this.zdb_id.substring(this.zdb_id.indexOf("Z"),this.zdb_id.length());

		this.abbrev = abbrev;
		this.mtype = mtype;
		this.target_abbrev = target_abbrev;
		this.lg_location = new Float(mm_chrom_location);
		try { this.OR_lg = new Integer(OR_lg); }
		catch (NumberFormatException e) { System.err.println("NumberFormatException: " + this.abbrev + " failed on '" + OR_lg + "'"); }
									
		
		this.metric = metric;
		
		if ((framework_t.equals("t")) || (framework_t.equals("1"))) 
			this.framework_t = new Boolean(true);
		else 			
			this.framework_t = new Boolean(false);

		
		selected_t = new Boolean(false);
	}


	public boolean isRelatedTo(Marker M) {
		return false;
			
	}
	
	
	public Color getColor() {
		if (mtype.equals("GENE")) { return GENE_c; }
		else if (mtype.equals("GENEP"))	{ return GENEP_c; }
  		else if (mtype.equals("EST")) { return EST_c; }
		else if (mtype.equals("SSLP")) { return SSLP_c; }
		else if (mtype.equals("RAPD")) { return RAPD_c; }
		else if (mtype.equals("STS")) { return STS_c; }
//		else if (mtype.equals("SSR")) { return SSR_c; }
		else if (mtype.equals("MUTANT")) { return FISH_c;}
		else if (mtype.equals("BAC")) { return BAC_c; }
		else if (mtype.equals("PAC")) { return PAC_c; }
   		else if (mtype.equals("SNP")) { return SNP_c; }
		else return Color.black;
			
	}
	
	
	public String getZdb_id() {
		return zdb_id;
	}
	
	public String getAbbrev() {
		return abbrev;
	}

	public String getMtype() {
		return mtype;
	}

	public String getTarget_abbrev() {
		return target_abbrev;
	}

	public String getMetric() {  return metric;	}
	

	public Float getLg_location() {
		return lg_location;
	}
	
	public Integer getOR_lg() {
		return OR_lg;
	}
	
	public Boolean getFramework_t() { 
		return framework_t;
		//return new Boolean(true);
	}
	  
	public boolean getSelected() {
		return selected_t.booleanValue();
	}
	
	public void  setSelected(boolean b) {
		selected_t = new Boolean(b);
		if (!(framework_t.equals(new Boolean(true)) && selected_t.equals(new Boolean(false)))) //we don't want to make real framework not a framework, but in every other case, we're calling selected markers frameworks - then coloring them in red in the drawframework code
			 framework_t = selected_t;
	}
	

	public String toString() {
		return new String (abbrev + " " + lg_location + " " + OR_lg + " " + framework_t + " " + mtype + " " + target_abbrev + " " + zdb_id);
	}
 
	
}
