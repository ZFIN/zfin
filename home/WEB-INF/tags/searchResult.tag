<%@ tag import="org.zfin.search.Category" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="geneCategoryName" value="<%=Category.GENE.getName()%>"/>
<c:set var="fishCategoryName" value="<%=Category.FISH.getName()%>"/>
<c:set var="publicationCategoryName" value="<%=Category.PUBLICATION.getName()%>"/>


<%@attribute name="result" required="true" type="org.zfin.search.presentation.SearchResult" %>

<c:choose>
    <c:when test="${result.category == geneCategoryName}">
        <zfin-search:geneResult result="${result}"/>
    </c:when>
    <c:when test="${result.category == publicationCategoryName}">
        <zfin-search:publicationResult result="${result}"/>
    </c:when>
    <c:otherwise>
        <zfin-search:genericResult result="${result}"/>
    </c:otherwise>
</c:choose>

