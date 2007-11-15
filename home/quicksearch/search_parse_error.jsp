<%@ page import="org.zfin.uniquery.SearchCategory,
                 org.zfin.uniquery.search.SearchBean,
                 org.zfin.uniquery.search.Hit,
                 org.zfin.uniquery.search.SearchResults,
                 org.apache.lucene.search.Hits,
                 org.apache.lucene.document.Document,
                 org.apache.lucene.document.Field,
                 org.apache.commons.lang.StringUtils,
                 java.util.Enumeration,
                 java.util.ArrayList,
                 java.util.Iterator,
                 java.net.URLEncoder" %>

<%@ page isErrorPage="true" %>


<%
    String queryTerm = request.getParameter("query");
    if (queryTerm == null)
        {
        queryTerm = "";
        }
    queryTerm = queryTerm.trim();
    
    String categoryTerm = request.getParameter("category");
    if (categoryTerm == null)
        {
        categoryTerm = "all";
        }
    categoryTerm = categoryTerm.trim();
    
%>


<html>
<head>
    <title>ZFIN Search: Search Error</title>
    <style type="text/css">
        .query_parse_error        { font-size: 10pt; margin-left: 60px; margin-right: 60px; }
        .query_parse_error_header { font-size: 14pt; font-weight: bold; } 
        .search_terms             { font-weight: bold; }
    </style>
</head>
<script language="JavaScript" src="/header.js"></script>

    <div class="query_parse_error">
        <span class="query_parse_error_header">Search Error</span><br>
        <p>
            Your search for '<span class="search_terms"><%=queryTerm%></span>' has encountered an error.   
        </p>
        <p>
            This quick search feature supports advanced searches through the use of special 
            characters.  Errors may occur if these special characters are a part of your 
            search string.  
        </p>
        <p>
            Please review our <a href="syntax_help.jsp">search tips page</a> or contact 
            <a href="mailto:zfinadmn@zfin.org">Sherry Giglia</a> at ZFIN for help with your query.
        </p>
    </div>

<p>&nbsp;</p>
<TABLE class="search" width="100%" border="0" cellpadding="1" cellspacing="0">
    <TR>
        <TD align="left" class="titlebar">
            <font size="+1"><b>Invalid query syntax</b></font>
        </TD>
        <TD align="right" class="titlebar">
            <a href="syntax_help.jsp">Quick Search tips</a>
        </TD>
    </TR>
    <TR>
        <TD colspan="2">
            <form name="uniquery" action="/SearchApp/search" method="get">
                <p>
                    Search in Category
                    <select name="category">
                    <option value="all">All
                    <% 
                    for (int i=0; i<SearchCategory.CATEGORIES.size(); i++)
                        {
                        SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);
                        String selected = "";
                        if (category.getId().equals(categoryTerm))
                            {
                            selected = "SELECTED";
                            }
                        %>
                        <option value=<%=category.getId()%> <%=selected%> ><%=category.getDescription()%>
                        <%
                        }
                    %>
                    </select>
                    for term <input type="text" name="query" value="<%= StringUtils.replace(queryTerm, "\"", "&quot;") %>" size="44"/> <input type="submit" value="Search"/>
                </p>
            </form>    
        </TD>
    </TR>
    <TR>
        <TD colspan="2" class="titlebar">
        &nbsp;
        </TD>
    </TR>
</TABLE>

<script language="JavaScript" src="/footer.js"></script>