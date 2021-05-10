<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="blastResults" type="org.zfin.sequence.blast.results.view.BlastResultBean" rtexprvalue="true" required="true"%>


<%--<strong>Program:</strong>  ${blastResults.program}--%>

<strong>Query:</strong> ${blastResults.defLine}
<span style="font-size:small"> (<fmt:formatNumber value="${blastResults.queryLength}"/> letters) </span>

<br>

<strong>
    <c:choose>
        <c:when test="${fn:length(blastResults.databases) > 1}">
            Databases:
        </c:when>
        <c:otherwise>
            Database:
        </c:otherwise>
    </c:choose>
</strong>
<c:forEach var="database" items="${blastResults.databases}" varStatus="loopStatus">
    ${database.database.name}
    <c:if test="${!loopStatus.last}">
        , 
    </c:if>
</c:forEach>

<br>
    <strong>Number of Sequences:</strong>
    <fmt:formatNumber value="${blastResults.numberOfSequences}"/>

