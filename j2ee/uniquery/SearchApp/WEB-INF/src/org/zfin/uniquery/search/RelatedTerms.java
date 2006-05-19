package org.zfin.uniquery.search;

import org.apache.commons.lang.StringUtils;
import org.zfin.uniquery.ZfinAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import java.sql.*;
import javax.sql.*;
import com.informix.jdbc.*;
import javax.naming.InitialContext;
import javax.naming.Context;

public class RelatedTerms 
{
   private String dbName;
   private long timeToOpenConnection;
   private long timeToGetAnatomyHits;
   private long timeToGetAliasHits;

   /**
    * Constructor. Sets the database name to use.
    */
   public RelatedTerms (String dbName) throws Exception {
      this.dbName = dbName;
   }


   /**
    * Opens a connection to the specified database.
    */
   private Connection openConnection() throws Exception {
      long start = System.currentTimeMillis();
      
      /* get required DataSource information */
      Context initContext = new InitialContext();
      Context envContext = (Context) initContext.lookup("java:comp/env");
      DataSource ds = (DataSource) envContext.lookup("jdbc/zfinDatabase");
      
      /* make a connection using pool */
      Connection conn = ds.getConnection();
      Statement stmt = conn.createStatement();
      stmt.executeUpdate("database " + dbName);
      
      /* release resources to prevent leaks */
      stmt.close();
      envContext.close();
      initContext.close();           
      
      timeToOpenConnection = System.currentTimeMillis() - start;            
      return conn;
   }


   /**
    * Searches anatomy tokens for a match of a given query string.
    * Returns results {token=hits} as a Hashtable, 
    *   where hits is an ArrayList.
    */
   public Hashtable getAllAnatomyHits (String queryString) throws Exception {
      Hashtable results = new Hashtable();
      long start = System.currentTimeMillis();

      ArrayList tokens = getTokens(queryString);
      for (int i = 0; i < tokens.size(); i++) {
         String token = (String)tokens.get(i);
         ArrayList anatomyhits = getAnatomyHits(token);
         if (!anatomyhits.isEmpty()) {
	     token = token.replace("''", "'");
	     results.put(token,anatomyhits);
         }
      }

      timeToGetAnatomyHits = System.currentTimeMillis() - start;

      return results;
   }


   /**
    * Searches anatomy tokens for a match of a given token.
    * Returns zdb_ids as an ArrayList.
    */
   public ArrayList getAnatomyHits (String token) throws Exception {
      Connection db = openConnection();
      
      ArrayList results = new ArrayList();
      Statement stmt = db.createStatement();
      ResultSet rs = stmt.executeQuery("select * from all_anatomy_tokens where anattok_token_lower = '"+token+"'");
      while (rs.next()) {
          String zdb_id = rs.getString("anattok_anatitem_zdb_id");
          if (!results.contains(zdb_id)) {
             results.add(zdb_id);
          }
      }
      results.trimToSize();
      
      /* release resources to prevent leaks */
      rs.close();
      stmt.close();
      db.close();
      
      return results;
   }

    /**
     * Search all_map_names and anatomy_item tables for exact name/symbol match
     * on markers/clones/genes/mutants/anatomy terms. We could only use base 
     * tables, may update when Fish tables consolidated.
     * Return zdb id if match, otherwise empty string.
     */
    public String getBestMatchId (String queryTerm) throws Exception {

      String queryTermEscaped = StringUtils.replace(queryTerm,"'","''");
      String resultId = "";

      Connection db = openConnection();

      Statement stmt = db.createStatement();
      String theSql = "select allmapnm_zdb_id as zdb_id from all_map_names where allmapnm_name_lower = '"+queryTermEscaped.toLowerCase() + "' and allmapnm_precedence in ('Current symbol', 'Current name', 'Fish name/allele', 'Locus abbreviation', 'Locus name') UNION select anatitem_zdb_id as zdb_id from anatomy_item where anatitem_name_lower = '"+queryTermEscaped.toLowerCase() + "'";

      ResultSet rs = stmt.executeQuery(theSql);
      while (rs.next()) {
          resultId = rs.getString("zdb_id");
      }

      /* release resources to prevent leaks */
      rs.close();
      stmt.close();
      db.close();
      
      return resultId;
    }
 
    /**

     */
    public String getReplacedZdbId (String queryTerm) throws Exception {

        String queryTermEscaped = StringUtils.replace(queryTerm,"'","''"); 
	String resultId = "";

	Connection db = openConnection();
	
	Statement stmt = db.createStatement();
	String theSql = "select zrepld_new_zdb_id from zdb_replaced_data where zrepld_old_zdb_id = '" + queryTermEscaped.toUpperCase() + "'";
	
	ResultSet rs = stmt.executeQuery(theSql);
	while (rs.next()) {
	    resultId = rs.getString("zrepld_new_zdb_id");
	}
	
	/* release resources to prevent leaks */
	rs.close();
	stmt.close();
	db.close();
      
	return resultId;
	
    }
    
    /**

    public String getTest (String queryTerm) throws Exception {

      String queryTermEscaped = StringUtils.replace(queryTerm,"'","''");
      String resultId = "";

      Connection db = openConnection();

      Statement stmt = db.createStatement();
      String theSql = "select full_name from person where zdb_id = 'ZDB-PERS-990226-25'";
      ResultSet rs = stmt.executeQuery(theSql);
      while (rs.next()) {
          resultId = rs.getString("full_name");
      }

      // release resources to prevent leaks 
      rs.close();
      stmt.close();
      db.close();
      
      return resultId;
    }
   */     

   /**
    * Searches alias tokens for a match of a given query string.
    * Returns results {token=hits} as a Hashtable, 
    *   where hits is an ArrayList.
    
   public Hashtable getAllAliasHits (String queryString) throws Exception {
      Hashtable results = new Hashtable();
      long start = System.currentTimeMillis();   

      ArrayList tokens = getTokens(queryString);
      for (int i = 0; i < tokens.size(); i++) {
         String token = (String)tokens.get(i);
         ArrayList anatomyhits = getAliasHits(queryString, token);
         if (!anatomyhits.isEmpty()) {
            results.put(token,anatomyhits);
         }
      }
      
      timeToGetAliasHits = System.currentTimeMillis() - start;
      
      return results;
   }  
   * commenting it all out for exact synonymn only testing
   */ 

   /**
    * Searches alias tokens for a match of a given query string.
    * Returns results {token=hits} as a Hashtable, 
    *   where hits is an ArrayList.
    */
   public Hashtable getAllAliasHits (String queryString) throws Exception {
      Hashtable results = new Hashtable();
      long start = System.currentTimeMillis();   

      Connection db = openConnection();
               
      ArrayList abbrevHits = new ArrayList();

      Statement stmt = db.createStatement();
      ResultSet rs = stmt.executeQuery("select dalias_alias_lower, get_obj_abbrev(dalias_data_zdb_id) as alias_abbrev from data_alias where dalias_alias_lower = '"+ StringUtils.replace(queryString,"'","''") +"'");
      while (rs.next()) {
          String match = rs.getString("dalias_alias_lower");
          String abbrev = rs.getString("alias_abbrev");

          /**
           * Added the [abbrev,match] array so we can display the previous name
           * matching text along with the new name abbrevation.
           */
          String [] hit = {abbrev,match};

	  abbrevHits.add(hit);
          
      }      
      abbrevHits.trimToSize();
      
      /* release resources to prevent leaks */
      rs.close();
      stmt.close();
      db.close();
      
      if (!abbrevHits.isEmpty()) {
	  results.put(queryString,abbrevHits);
      }
    
      timeToGetAliasHits = System.currentTimeMillis() - start;
      
      return results;
   }   
     

   /**
    * Searches alias tokens for a match of a given token.
    * Returns a [new term,old term] Vector in an ArrayList.
    */
   public ArrayList getAliasHits (String queryString, String token) throws Exception {
      Connection db = openConnection();
               
      ArrayList abbrev_results = new ArrayList();
      ArrayList results = new ArrayList();
      ArrayList queryTerms = getTokens(queryString);
      Statement stmt = db.createStatement();
      ResultSet rs = stmt.executeQuery("select dalias_alias_lower, get_obj_abbrev(dalias_data_zdb_id) as alias_abbrev from all_alias_tokens, data_alias where aliastok_dalias_zdb_id = dalias_zdb_id and aliastok_token_lower = '"+token+"'");
      while (rs.next()) {
          String match = rs.getString("dalias_alias_lower");
          String abbrev = rs.getString("alias_abbrev");

          /**
           * Added the [abbrev,match] array so we can display the previous name
           * matching text along with the new name abbrevation.
           */
          String [] hit = {abbrev,match};

          /**
           * This extra code is added to prevent duplicate hits when
           * an abbreviation contains more than one word.
           */
          ArrayList abbrevs = getTokens(abbrev);
          int resultAlreadyContains = 0;
          for (int i = 0; i < abbrevs.size(); i++) {
             String term = (String)abbrevs.get(i);
             if (abbrev_results.contains(term) || queryTerms.contains(term)) {
                resultAlreadyContains++;
             }
          }

          if (resultAlreadyContains < abbrevs.size()) {
             abbrev_results.add(abbrev);
             results.add(hit);
          }
      }      
      results.trimToSize();
      
      /* release resources to prevent leaks */
      rs.close();
      stmt.close();
      db.close();

      return results;
   }   


   /**
    * Parses query string and returns tokens as an ArrayList.
    */
   public ArrayList getTokens (String queryString) throws Exception {
      queryString = filterIllegals(queryString);
      ArrayList results = new ArrayList();
      Analyzer analyzer = new ZfinAnalyzer();
      String field = "body";
      if (queryString == null) { return results; }
      TokenStream tokenStream = analyzer.tokenStream(field, new StringReader(queryString));
      Token token = null;
      while ((token = tokenStream.next()) != null) {
         String tokenText = token.termText();
          if (!results.contains(tokenText)) {
             results.add(tokenText);
          }
      }
      results.trimToSize();
      //Collections.sort(results);  /* is sorting query tokens necessary ?  No? */
      return results;
   }


   /**
    * Replaces all occurrances of oldToken with newToken in queryString.
    */   
   public String tokenReplaceAll (String queryString, String oldToken, String newToken) throws Exception {
      String newQueryString = "";
      String result = "";
      ArrayList tokens = getTokens(queryString);
      for (int i = 0; i < tokens.size(); i++) {
         String tokenText = (String) tokens.get(i);
         if (tokenText.equals(oldToken)) {
            tokenText = newToken;
         }
         newQueryString += tokenText.trim() + " ";
      }

      // This should remove any illegal characters and duplicates.
      tokens = getTokens(newQueryString );
      for (int j = 0; j < tokens.size(); j++) {
         result += (String) tokens.get(j) + " ";
      }
      
      return result.trim();
   }

   
   /**
    * Parses test string and removes illegal characters (for database query.)
    */   
   public String filterIllegals (String text) throws Exception {
      text = text.toLowerCase();
      text = text.replaceAll("'","''");
      text = text.replaceAll("<sup>"," ");
      text = text.replaceAll("</sup>"," ");
      return text;
   }


   /**
    * Closes connection.
    */
   public void close() throws Exception {
 
   }
   
   
   /**
    *  Returns the database name.
    *  @return dbName
    */
   public String getDbName() {
      return dbName;
   }
   
   
   /**
    *  Sets the database name.
    */
   public void setDbName(String dbName) {
      this.dbName = dbName;
   }   


   /**
    *  Returns the timeToOpenConnection.
    *  @return timeToOpenConnection
    */
   public long getTimeToOpenConnection () {
      return timeToOpenConnection;
      
   }
   
   public long getTimeToGetAnatomyHits () {
      return timeToGetAnatomyHits;
      
   }
   
   public long getTimeToGetAliasHits () {
      return timeToGetAliasHits;
      
   }   

}

