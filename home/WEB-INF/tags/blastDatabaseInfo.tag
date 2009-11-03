<%@ tag import="org.zfin.sequence.blast.Origination" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="database" type="org.zfin.sequence.blast.presentation.DatabasePresentationBean" rtexprvalue="true" required="true" %>
<%@ attribute name="showOnlyDefinitions" type="java.lang.Boolean" rtexprvalue="true" required="false"  %>

<c:if test="${!showOnlyDefinitions}">
    <c:forEach begin="0" end="${database.indent}" step="1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</c:forEach>
    &bull; <span class="summaryTitle">${database.database.name}
    (${database.database.abbrev})
</span>
</c:if>
<span>
    <c:set var="orginationType"><%= Origination.Type.GENERATED %></c:set>
    ${database.database.description}
    <c:if test="${!empty database.databaseStatistics.modifiedDate}">
        (<fmt:formatDate value="${database.databaseStatistics.modifiedDate}"  dateStyle="medium" />)
    </c:if>
    <c:if test="${database.unavailable}">
        <span class="error-inline">(Unavailable)</span>
    </c:if>
</span>
<c:if test="${fn:length(database.directChildren)>1 and !showOnlyDefinitions}">
<span class="staticcontent">
<c:forEach var="childDatabase" items="${database.directChildren}" varStatus="loopStatus">
    <%--show label for the first one--%>
    ${loopStatus.first ? "includes [": ""}

    ${childDatabase.name}

    <%--show the last one--%>
    ${!loopStatus.last ? "::": ""}
    ${loopStatus.last ? "]": ""}
</c:forEach>
</span>
</c:if>


<authz:authorize ifAnyGranted="root">
    <c:if test="${!empty database.databaseStatistics.numSequences && database.databaseStatistics.numSequences>=0}">
        <fmt:formatNumber value="${database.databaseStatistics.numSequences}"  pattern="##,###" />
        sequences
    </c:if>
<span class="staticcontent">
    <br>
    <c:if test="${fn:length(database.leaves)>1}">
        <c:forEach var="childDatabase" items="${database.leaves}" varStatus="loopStatus">
            <%--show label for the first one--%>
            ${loopStatus.first ? "actual blast databases [": ""}

            ${childDatabase.name}

            <%--show the last one--%>
            ${!loopStatus.last ? "::": ""}
            ${loopStatus.last ? "]": ""}
        </c:forEach>
    </c:if>

    <zfin2:databaseOriginationColor origination="${database.database.origination}"/>
    ${ (database.database.publicDatabase) ? "public" : "private"}
    </span>
</authz:authorize>
