<%@ page import="org.zfin.uniquery.SearchCategory,
                 org.zfin.uniquery.search.SearchBean,
                 org.zfin.uniquery.search.Hit,
                 org.zfin.uniquery.search.SearchResults,
                 org.apache.lucene.search.Hits,
                 org.apache.lucene.document.Document,
                 org.apache.lucene.document.Field,
                 org.apache.commons.lang.StringUtils,
                 java.util.Collections,
                 java.util.Vector,
                 java.util.Hashtable,
                 java.util.ArrayList,
                 java.util.List,
                 java.util.Iterator,
                 java.net.URLEncoder" %>

<!---
    JSP page for the Quick Search ZFIN feature.  Relies on the SearchBean Java Bean.
--->


<!---
    The SearchBean Java Bean should be used in session mode, so user cookies must be
    enabled.  Session mode allows the bean to cache search queries and thereby
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
    String indexPath = getServletContext().getInitParameter("path_to_index");
    // hard coding indexPath for now
    indexPath = "/research/zcentral/www_homes/quark/j2ee/uniquery/indexes";
    String dbName = getServletContext().getInitParameter("db_name");


    /*
     * Get relevant browser query parameters
     */
     
    /* queryTerm */
    String queryTerm = request.getParameter("query");
    if (queryTerm == null) {
        queryTerm = "";
    }
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
     * Now perform the search based on the user query and preferences.
     */
    SearchResults results = null;

    if (queryTerm.length() > 0) {
      results = searchBean.doCategorySearch(indexPath, queryTerm, categoryTerm, pageSize, startIndex);
    }
%>


<!--- Display Page --->

<html>
<head>
    <title>ZFIN Search</title>
    <style type="text/css">
        .category_header       { font-size: 120%; font-weight: bold; font-family:arial, sans-serif; }
        .category_label        { font-size: 100%; font-weight: bold; font-family:arial, sans-serif; }
        .category_box          { padding-top: 10px; }
        .category_table        { font-size: 90%; font-family:arial, sans-serif; border: 1px solid #006666; padding:2px; }
        .category_item         { font-size: 90%; font-family:arial, sans-serif; padding-top:2px; padding-right:5px; padding-bottom:2px; padding-left:5px;}
        .alias_list            {  }
        .alias_list_header     { Color:#cc0000;}
        .ignored_words         { font-size: 80%; font-family:arial, sans-serif; padding-top: 10px; }
        .related_terms         { font-size: 80%; font-family:arial, sans-serif; padding-top: 10px;}
        .related_terms_match   { color:#999999; }
        .specific_search       { font-size: 100%; font-family:arial, sans-serif; padding-top: 10px;}
        .advanced_search       { font-size: 80%; font-family:arial, sans-serif; padding: 0px; margin: 0px;}
        .results_header        { font-size: 100%; font-weight: bold; font-family:arial, sans-serif; padding: 0px; margin: 0px;}
    </style>
</head>
<script language="JavaScript" src="/header.js"></script>

<script>
  function call_reset() {
    document.uniquery_search.category.selectedIndex = 0;
    document.uniquery_search.query.value = "";  
   }
</script>

<!--- Only display results if there is a query --->
<% if (queryTerm != null && !queryTerm.trim().equals("")) { %>


<!--- The Advanced Search link to allow users to modify query, category, or page size. --->
<div align="right" class="advanced_search">
    <a href="#modify">Advanced Search</a>
</div>


<!--- The page title, and suggestion to use ZFIN specific search forms. --->
<div align="center" class="results_header">
    <b><%= SearchCategory.getDescriptionById(categoryTerm) %> search results for '<%=queryTerm%>'</b> (<%= searchBean.getSearchResultsCount(indexPath, queryTerm, categoryTerm)%>).
    <br>
    <%= searchBean.getRelatedSearchPageHTML(dbName, queryTerm, categoryTerm) %>
</div>


<!--- Display Ignored Words List --->
<%= searchBean.getIgnoredWordsHTML(queryTerm) %>
<!--- END: Display Ignored Words List --->


<!--- Display Related Words List --->
<%= searchBean.getRelatedTermsHTML(dbName, queryTerm) %>
<!--- END: Display Related Words List --->


<!--- Display Category List (as a TABLE) --->
<center>
    <%= searchBean.getCategoryListingHTML(indexPath, queryTerm, categoryTerm, pageSize) %>
</center>
<!--- END: Display Category List (as a TABLE) --->


<!--- Display Search Results --->
<%= results.toString() %>
<!--- END: Display Search Results --->


<!--- Page Navigation --->
<center>
<%= searchBean.getPageNavigationHTML(results, indexPath, queryTerm, categoryTerm, pageSize) %>
</center>
<!--- END: Page Navigation --->


<!--- END: Only display results if there is a query --->
<% } else {  %>
    Please enter a search term.
<% }  %>


<!--- Modify Search Box --->
<%= searchBean.getAdvancedSearchHTML(queryTerm, categoryTerm, pageSize) %>
<!--- END: Modify Search Box --->

<script language="JavaScript" src="/footer.js"></script>