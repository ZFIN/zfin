<%@ page import="org.zfin.properties.ZfinProperties,
                 org.zfin.uniquery.SearchCategory" %>
<%@ page import="org.zfin.uniquery.search.SearchResults"%>

<!---
JSP page for the Quick Search ZFIN feature.  Relies on the SearchBean Java Bean.
--->


<!---
The SearchBean Java Bean should be used in session mode, so user cookies must be
enabled.  ZfinSession mode allows the bean to cache search queries and thereby
speed up performance dramatically while browsing categories.  The tradeoff is
that Tomcat will require significant memory allocation to store these Beans
in memory.

The Tomcat startup script should adequately specify enough heap size for the Java
JVM.  As an example, this is how the Tomcat startup script could allocate 256 Megabytes
of memory for the JVM:

CATALINA_OPTS= -XX:NewSize=128m -XX:MaxNewSize=128m -XX:SurvivorRatio=8 -Xms256M -Xmx256M

By default, the JVM is allocated only about 60 Megabytes which is very (too) small.
--->
<jsp:useBean id="searchBean" class="org.zfin.uniquery.search.SearchBean" scope="session" />

<!---
/**
 *  Page initialization:
 *  First get all the browser variables, making sure null are taken care of properly.
 *
 */
 --->
<%

    /*
     * Get the path to search indexes from the Tomcat ServeltContext parameters
     */
//    String indexPath = getServletContext().getInitParameter("path_to_index");
    String indexPath = ZfinProperties.getSearchIndexDirectory();
    String dbName = getServletContext().getInitParameter("db_name");
    
    /*
     * Get relevant browser query parameters
     */
     
    /* queryTerm */
    String queryTerm = request.getParameter("query");
    if (queryTerm == null) {
        queryTerm = "";
    }
    queryTerm = queryTerm.replace("*"," ").replace("+"," ");
    queryTerm = queryTerm.trim();
    
    /* categoryTerm */
    String categoryTerm = "";
    categoryTerm = request.getParameter("category");
    if (categoryTerm == null) {
        categoryTerm = "ALL";
    }
    categoryTerm = categoryTerm.trim();

    /* page view preferences */
    int startIndex;
    int pageSize;
    try {
        startIndex = Integer.parseInt(request.getParameter("startIndex"));
    } catch (NumberFormatException e) {
        startIndex = 0;
    }
   
    try {
        pageSize = Integer.parseInt(request.getParameter("pageSize"));
    } catch (NumberFormatException e) {
        pageSize = 25;
    }

    /*
     *  Verify the query term is not a replaced zdb id, 
     *	perform the search based on the user query and preferences.
     */
	
    String newZdbId = searchBean.getNewZdbId(dbName, queryTerm);

    SearchResults expressionResults = null;

    if (queryTerm.length() > 0 && newZdbId.equals("")) {
      expressionResults = searchBean.doCategorySearch(indexPath, queryTerm, categoryTerm, pageSize, startIndex);
    }
%>


<!--- Display Page --->

    <style type="text/css">
        .category_header       { font-size: 100%; font-weight: bold; font-family:arial, sans-serif; }
        .category_label        { font-size: 100%; font-weight: bold; font-family:arial, sans-serif; }
        .category_box          { padding-top: 10px; }
        .category_table        { font-size: 90%; font-family:arial, sans-serif; border: 1px solid #006666; padding:2px; }
        .category_item         { font-size: 90%; font-family:arial, sans-serif; padding-top:2px; padding-right:5px; padding-bottom:2px; padding-left:5px;}
        .alias_list            {  }
        .alias_list_header     { Color:#cc0000;}
        .ignored_words         { font-size: 90%; font-family:arial, sans-serif; padding-top: 10px; }
        .related_terms         { font-size: 90%; font-family:arial, sans-serif; padding-top: 10px;}
        .related_terms_match   { color:#999999; }
        .specific_search       { font-size: 90%; font-family:arial, sans-serif; padding-top: 10px;}
        .results_header        { font-size: 100%; font-weight: bold; font-family:arial, sans-serif; padding: 0px; margin: 0px;}
	.search_tip	       { font-size: 90%; font-family:arial, sans-serif; padding-top: 10px;}	
	.best_match            { font-size: 100%; font-family:arial, sans-serif; padding: 0px; margin: 0px;}
    </style>

<script>
  document.getElementById("qsearch").value = "<%= queryTerm %>";
  function call_reset() {
    document.uniquery_search.category.selectedIndex = 0;
    document.uniquery_search.query.value = "";  
   }

</script>


<!--- Only display results if there is a query --->
<% if (queryTerm == null || queryTerm.trim().equals("")) { %>
    <span class="results_header">
      Please enter a search term.
   </span>
<% } else if ( !newZdbId.equals("") ) {  %>
    <span class="results_header">
      <%= queryTerm %> has been changed. <p>
      Please check <a href="/SearchApp/category_search.jsp?query=<%= newZdbId %>"><%= newZdbId %></a>.
    </span>		
    
<% } else { %>

<!--- The page title --->
<!-- we want to display "Search results" for all, and "Genes/Markers/Clones search results" for the rest
     the SearchCategory.getDescriptionById() returns "All" for all which is also used else where, thus 
     we add logic here though really do not want to. 
-->

<table width=100% cellspacing=0>
  <tr>
   <td width=85% align=center>
    <%  
	String categoryDesc = SearchCategory.getDescriptionById(categoryTerm);
	String categorySearch = "";
	if ( categoryDesc.equals("All") ) {
		categorySearch = "Search";
	}else {
		categorySearch =  categoryDesc + " search";
	}
    %>
    <span class="results_header">
        <%= categorySearch %> results for '<%=queryTerm%>' (<%= searchBean.getSearchResultsCount(indexPath, queryTerm, categoryTerm)%>). 
    </span>
    <span class="search_tip">
        <a href="/quicksearch/syntax_help.jsp"> Tips </a>
    </span>
   </td>
   <td width=15% align=right>
      <form method=post 
            action="/<!--|WEBDRIVER_PATH_FROM_ROOT|-->" 
            target=comments>
         <input type=hidden name=page_name value="Site search">   
	 <input type=hidden name=MIval value="aa-input_welcome_generic.apg">
	 <input type=submit value="Your Input Welcome">
      </form>
   </td>
  </tr>
</table>


<!--- Display Ignored Words List --->
<%= searchBean.getIgnoredWordsHTML(queryTerm) %>
<!--- END: Display Ignored Words List --->
<table>
<tr> <td>
<!--- Display Related Words List --->
<%= searchBean.getRelatedTermsHTML(dbName, queryTerm) %>
<!--- END: Display Related Words List --->
</td> </tr>
<tr> <td>
<!---  Display suggestion to use ZFIN specific search forms. --->
<%= searchBean.getRelatedSearchPageHTML(dbName, queryTerm, categoryTerm) %>
<!---  suggestion to use ZFIN specific search forms. --->
</td> </tr>
</table>

<!--- Display Category List (as a TABLE) --->
<center>
    <%= searchBean.getCategoryListingHTML(indexPath, queryTerm, categoryTerm, pageSize) %>
</center>
<!--- END: Display Category List (as a TABLE) --->

<!--- Display the best match if any --->
<%= searchBean.getBestMatchHTML(dbName, queryTerm) %>
<!--- END: Display the best match --->

<!--- Display Search Results --->
<%= expressionResults.toString() %>
<!--- END: Display Search Results --->


<!--- Page Navigation --->
<center>
<%= searchBean.getPageNavigationHTML(expressionResults, indexPath, queryTerm, categoryTerm, pageSize) %>
</center>
<!--- END: Page Navigation --->


<!--- END: Only display results if there is a query --->

<% }  %>
