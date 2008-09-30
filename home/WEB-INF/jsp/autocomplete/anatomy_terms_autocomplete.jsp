<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<c:if test="${fn:length(anatomyTerms) == 0}">
    <ul>
        <li style="list-style-type:none;color:red; font-size:10pt">
            <c:out value="No Match..." escapeXml="true"/></li>
    </ul>
</c:if>
<c:if test="${fn:length(anatomyTerms) > 0}">
    <ul style="list-style-type:none;"><c:forEach var="anatomyTerm" items="${anatomyTerms}">
        <li><zfin:hightlight highlightEntity="${anatomyTerm.name}" highlightString="${formBean.query}"/></li>
    </c:forEach>
    </ul>
</c:if>
