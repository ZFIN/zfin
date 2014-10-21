<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %><%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Solr Example Search</title>
</head>
<body>


<link rel=stylesheet type="text/css" href="/css/faceted-search.css">

<div class="search-box">
    <form method="get" action="/search">
        <input name="q" type="text" size="60" value="${q}"/>
        <button type="submit">Search</button>
<%--
        I think this is a little too broken to use right now, so I'll comment it out for now..
--%>
        <a href="javascript:void(0);" onclick="jQuery('.advanced-search-box').slideToggle();">Advanced</a>

        <div class="advanced-search-box">
            <div class="advanced-search-input-group">
                <select name="af">
                    <c:forEach items="${advancedFields}" var="field">
                        <option value="${field}"
                                <c:if test="${field == af[0]}" >selected="true"</c:if>
                                >${field}</option>
                    </c:forEach>
                </select>
                <input name="av" size="40" value="${av[0]}"/>
            </div>
            <div class="advanced-search-input-group">
                <select name="af">
                    <c:forEach items="${advancedFields}" var="field">
                        <option value="${field}"
                                <c:if test="${field == af[1]}" >selected="true"</c:if>
                                >${field}</option>
                    </c:forEach>
                </select>
                <input name="av" size="40" value="${av[1]}"/>
            </div>
            <div class="advanced-search-input-group">
                <select name="af">
                    <c:forEach items="${advancedFields}" var="field">
                        <option value="${field}"
                                <c:if test="${field == af[2]}" >selected="true"</c:if>
                                >${field}</option>
                    </c:forEach>
                </select>
                <input name="av" size="40" value="${av[2]}"/>
            </div>
        </div>


    </form>
</div>


<div class="refinement-section">
    <zfin:breadbox query="${query}" queryResponse="${response}" baseUrl="${baseUrl}"/>
    <zfin:facets queryResponse="${response}" baseUrl="${baseUrl}"/>
</div>

<div class="search-result-container">
    <div class="result-count">    ${numFound} results </div>

    <c:forEach var="result" items="${results}">
        <div class="search-result">
            <div class="search-result-category">${result.category}</div>
            <div class="search-result-name"><zfin:link entity="${result}"/></div>
            <div class="search-result-snippet">${result.matchingText}</div>
        </div>
    </c:forEach>
</div>

<zfin2:pagination paginationBean="${paginationBean}"/>


<div style="clear:both; width:100%">
    <a style="clear:both; font-size: smaller;" href="javascript:void(0);" onclick="jQuery('.debug-output' ).slideToggle();">don't look at my debug output!</a>
</div>
<div class="debug-output" style="clear: both; background-color: pink ; border:5px solid magenta; display: none">
${debug}
</div>


</body>
</html>