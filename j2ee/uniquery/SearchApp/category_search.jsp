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
                 java.util.List,
                 java.util.Iterator,
                 java.net.URLEncoder" %>


<jsp:useBean id="searchBean" class="org.zfin.uniquery.search.SearchBean" scope="request" />

<%
    String indexPath = getServletContext().getInitParameter("path_to_index");

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

    int startIndex;
    int pageSize;
    try
        {
        startIndex = Integer.parseInt(request.getParameter("startIndex"));
        }
    catch (NumberFormatException e)
        {
        startIndex = 0;
        }
    try
        {
        pageSize = Integer.parseInt(request.getParameter("pageSize"));
        }
    catch (NumberFormatException e)
        {
        pageSize = 10;
        }

    ArrayList resultCounts = new ArrayList(SearchCategory.CATEGORIES.size());
    SearchResults results = null;
    int totalResultsCount = 0;
    String resultsLabel = "records";
    List ignoredWords = null;
    if (queryTerm.length() > 0)
        {
        if ("all".equals(categoryTerm))
            {
            for (int i=0; i<SearchCategory.CATEGORIES.size(); i++)
                {
                SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);
                int resultCount = searchBean.getSearchResultsCount(indexPath, queryTerm, category.getId());
                totalResultsCount += resultCount;
                resultCounts.add(new Integer(resultCount));
                }
            if (totalResultsCount == 1) 
                {
                resultsLabel = "record";
                }
            }
        else
            {
            results = searchBean.doCategorySearch(indexPath, queryTerm, categoryTerm, pageSize, startIndex);
            if (results.getTotalHits() == 1) 
                {
                resultsLabel = "record";
                }
            }
        ignoredWords = searchBean.getIgnoredWords(queryTerm);
        }
%>

<html>
<head>
    <title>ZFIN Search</title>
    <style type="text/css">
        .category_header       { font-size: 14pt; font-weight: bold; font-family:arial, sans-serif; }
        .category_label        { font-size: 10pt; font-weight: bold; font-family:arial, sans-serif; }
        .category_results      { font-size: 9pt; font-family:arial, sans-serif; }
        .category_results_list { margin-bottom: 9pt; bold; font-family:arial, sans-serif; font-weight: bold; }
    </style>
</head>
<script language="JavaScript" src="/header.js"></script>
    
    
    <% if (queryTerm.length() > 0)
        {
        if (results == null) // show all categories
            {
            %>
            <center>
            <div class="category_header">Quick Search Results for '<%=queryTerm%>' (<%=totalResultsCount%> <%=resultsLabel%>)</div>
                <% 
                if (ignoredWords != null && ignoredWords.size() > 0)
                    {
                    %>
                     <font size="-1" color="#000000">
                     The following words are very common and were not included in your search: &nbsp;&nbsp; 
                     <%
                     for (int i=0; i<ignoredWords.size(); i++)
                        {
                        String word = (String) ignoredWords.get(i);
                        %>
                        <b><%= word %></b>&nbsp;&nbsp;
                        <%
                        }
                     %>
                     </font>
                    <%
                    }
                %>
            <br>
            <table border="0">
                <tr>
                    <td align="left" valign="top">
                        <ul>
                        <%
                        for (int i=0; i<=(int) (SearchCategory.CATEGORIES.size()/2); i++)
                            {
                            SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);                    
                            if (((Integer)resultCounts.get(i)).intValue() > 0)
                                {
                                %>
                                <li class="category_results_list"><a href="category_search.jsp?query=<%=URLEncoder.encode(queryTerm, "UTF-8")%>&category=<%=category.getId()%>"><%=resultCounts.get(i).toString()%> in <%=category.getDescription()%> </a></li>
                                <%
                                }
                            else
                                {
                                %>
                                <li class="category_results_list">0 in <%=category.getDescription()%> </li>
                                <%
                                }
                            }
                        %>
                        </ul>
                    </td>
                    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
                    <td align="left" valign="top">
                        <ul>
                        <%
                        for (int i=(int) (SearchCategory.CATEGORIES.size() / 2) + 1; i<SearchCategory.CATEGORIES.size(); i++)
                            {
                            SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);                    
                            if (((Integer)resultCounts.get(i)).intValue() > 0)
                                {
                                %>
                                <li class="category_results_list"><a href="category_search.jsp?query=<%=URLEncoder.encode(queryTerm, "UTF-8")%>&category=<%=category.getId()%>"><%=resultCounts.get(i).toString()%> in <%=category.getDescription()%> </a></li>
                                <%
                                }
                            else
                                {
                                %>
                                <li class="category_results_list">0 in <%=category.getDescription()%> </li>
                                <%
                                }
                            }
                        %>
                        </ul>
                    </td>
                </tr>
            </table>
            </center>
            <%
            }
        else // show detailed results for specific category
            {
            String categoryDescription = "Other";
            for (int i=0; i<SearchCategory.CATEGORIES.size(); i++)
                {
                SearchCategory category = (SearchCategory) SearchCategory.CATEGORIES.get(i);
                if (category.getId().equals(categoryTerm))
                    {
                    categoryDescription = category.getDescription();
                    break;
                    }
                }
            %>
            
            <table width="100%" border="0">
                <tr>
                    <td width="20%">&nbsp;</td>
                    <td width="60%" align="center">
                        <b><%=categoryDescription%> Search Results for '<%=queryTerm%>' (<%= results.getTotalHits() %> <%= resultsLabel %>)</b> 
                    </td>
                    <td width="20%" align="center">
                        <a href="#modify">Modify Search</a><br>
                    </td>
                </tr>
                <% 
                if (ignoredWords != null && ignoredWords.size() > 0)
                    {
                    %>
                    <tr>
                        <td colspan="2" align="center">
                             <font size="-1" color="#000000">
                             The following words are very common and were not included in your search: &nbsp;&nbsp; 
                             <%
                             for (int i=0; i<ignoredWords.size(); i++)
                                {
                                String word = (String) ignoredWords.get(i);
                                %>
                                <b><%= word %></b>&nbsp;&nbsp;
                                <%
                                }
                             %>
                             </font>
                        </td>
                    </tr>
                    <%
                    }
                %>
            </table>
    
            <%
            Iterator hitsIterator = results.getResults();
            while (hitsIterator.hasNext())
                {
                Hit hit = (Hit) hitsIterator.next();
                Document doc = hit.getDocument();
                String pageTitle = doc.get(SearchBean.TITLE);
                if (pageTitle.trim().length() < 1)
                    {
                    pageTitle = "Untitled";
                    }
                %>
                <p>
                    <a href="<%= doc.get(SearchBean.URL) %>"><%=pageTitle%></a><br>
                    <%= hit.getHighlightedText() %><br>
                    <font color="green" size="-2"><%= doc.get(SearchBean.URL) %></font>
                </p>
                <%
                }
            %>
            <center>
            <%
            int totalPageCount = results.getPageCount();
            int currentPage = results.getCurrentPageIndex();
            int startOfPageRange = Math.max(0, currentPage - 5);
            int endOfPageRange = Math.min(startOfPageRange + 10, totalPageCount);
            
            if (totalPageCount > 1)
                {
                if (startOfPageRange > 0)
                    {
                    %>
                   <a href="category_search.jsp?query=<%=URLEncoder.encode(queryTerm, "UTF-8")%>&category=<%=categoryTerm%>&pageSize=<%=pageSize%>&startIndex=0">First Page</a> &nbsp;&nbsp;
                    <%
                    }
        
                for (int i=startOfPageRange; i<endOfPageRange; i++)
                    {
                     if (i == currentPage)
                        {
                        %>
                        <b><%=(i + 1)%>&nbsp;</b>
                        <%
                        }
                    else
                        {
                        %>
                        <a href="category_search.jsp?query=<%=URLEncoder.encode(queryTerm, "UTF-8")%>&category=<%=categoryTerm%>&pageSize=<%=pageSize%>&startIndex=<%=(i * pageSize)%>"><%=(i + 1)%>&nbsp;</a>
                        <%
                        }
                    }
                if (endOfPageRange < totalPageCount)
                    {
                    %>
                    &nbsp;&nbsp;<a href="category_search.jsp?query=<%=URLEncoder.encode(queryTerm, "UTF-8")%>&category=<%=categoryTerm%>&pageSize=<%=pageSize%>&startIndex=<%=((totalPageCount-1) * pageSize)%>">Last page</a>
                    <%
                    }
                }
            }
            %>
        </center>
        <p>&nbsp;</p>
        <%
        }
    %>            

<TABLE class="search" width="100%" border="0" cellpadding="1" cellspacing="0">
    <TR>
        <TD align="left" class="titlebar">
        <%
        if (queryTerm.length() > 0)
            {
            %>
            <a name="modify">
                <font size="+1"><b>Modify your search</b></font>
            </a>
            <%
            }
        %>&nbsp;
        </TD>
        <TD align="right" class="titlebar">
            &nbsp;
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
