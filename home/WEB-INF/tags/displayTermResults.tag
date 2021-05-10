<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="termList" type="java.util.Collection" required="true" %>
<%@ attribute name="query" type="java.lang.String" required="true" %>
<%@ attribute name="showOntologyColumn" type="java.lang.Boolean" required="false" %>

<table class="searchresults rowstripes">
    <tr>
        <th width="30%">Term Name</th>
        <c:if test="${showOntologyColumn}">
            <th>Ontology</th>
        </c:if>
        <th>Synonyms</th>
    </tr>
    <c:forEach var="term" items="${termList}" varStatus="rowCounter">
        <zfin:alternating-tr loopName="rowCounter">
            <td>
                <zfin:link entity="${term}"/>
            </td>
            <c:if test="${showOntologyColumn}">
                <td>${term.ontology.displayName}</td>
            </c:if>
            <td>
                <c:if test="${fn:length(term.aliases) > 0}">
                    <c:forEach var="alias" items="${term.aliases}" varStatus="index">
                        <zfin:highlight highlightEntity="${alias}"
                                        highlightString="${query}"/><c:if test="${!index.last}">,</c:if>
                    </c:forEach>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
