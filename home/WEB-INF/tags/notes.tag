<%@ attribute name="hasNotes" type="org.zfin.infrastructure.EntityNotes" rtexprvalue="true" required="true" %>
<%@ attribute name="inTable" type="java.lang.Boolean" rtexprvalue="true" required="false" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize ifAnyGranted="root">
    <c:if test="${!empty hasNotes.dataNotes}">
        ${inTable ? "" : "<table>"}
        <%--<tr><td width="100%">Curator Note:</td></tr>--%>
        <tr><td width="100%"><b>Curator Note:</b></td></tr>
        <c:if test="${fn:length(hasNotes.dataNotes) > 1}">
        </c:if>
        <c:forEach var="curatorNote" items="${hasNotes.dataNotes}" varStatus="loopCurNote">
            <%--<tr><td>${curatorNote.note}</td></tr>--%>
            <tr><td>${curatorNote.curator.name}&nbsp;&nbsp;${curatorNote.date}<br/>${curatorNote.note}
            <c:if test="${!loopCurNote.last}"><br/>&nbsp;<br/></c:if>
        </c:forEach>
        <c:if test="${fn:length(hasNotes.dataNotes) > 1}">
            ${inTable ? "" : "</table>"}
        </c:if>
    </c:if>
</authz:authorize>

<c:if test="${!(empty hasNotes.publicComments)}">
    ${inTable ? "" : "<table>"}
    <tr><td><b>Note:</b></td></tr>
   <%--// <tr><td>${zfn:escapeHtml(hasNotes.publicComments, true)}</td></tr>--%>
     <tr><td>${hasNotes.publicComments}</td></tr>
    ${inTable ? "" : "</table>"}
</c:if>

