<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="database" type="org.zfin.sequence.blast.presentation.DatabasePresentationBean" rtexprvalue="true" required="true" %>
<%@ attribute name="selected" type="java.lang.String" rtexprvalue="true" required="false" %>

<option VALUE="${database.database.abbrev.value}"
        <c:if test="${database.indent eq 0}">
            style="font-weight:bold;"
        </c:if>
        <c:if test="${database.database.abbrev eq zfn:getAvailableAbbrev(selected)}">
            selected
        </c:if>
        >
    <c:forEach begin="1" end="${database.indent}" step="1">&nbsp;&nbsp;&nbsp;&nbsp;</c:forEach>
    <%--<c:if test="${database.indent > 0}">&lfloor;</c:if>--%>
    ${database.database.name}
</option>
