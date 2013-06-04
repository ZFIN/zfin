<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="termList" type="java.util.Collection" required="true" %>
<%@ attribute name="query" type="java.lang.String" required="true" %>

<table class="searchresults rowstripes">
    <tr>
        <th width="30%">Term Name</th>
        <th>Ontology</th>
        <th>Synonyms</th>
    </tr>
    <c:forEach var="term" items="${termList}" varStatus="rowCounter">
        <zfin:alternating-tr loopName="rowCounter">
            <td>
                <zfin:link entity="${term}"/>
            </td>
            <td>${term.ontology.displayName}</td>
            <td>
                <c:if test="${fn:length(term.aliases) > 0}">
                    <c:forEach var="alias" items="${term.aliases}" varStatus="index">
                        <zfin:highlight highlightEntity="${alias}"
                                        highlightString="${query}"/>
                        <c:if test="${!index.last}">,</c:if>
                    </c:forEach>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
