<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="hasNotes" type="org.zfin.infrastructure.EntityNotes" rtexprvalue="true" required="true" %>


<authz:authorize ifAnyGranted="root">
<c:if test="${!empty formBean.marker.dataNotes}">
    <b>Curator Notes: </b>
    <c:if test="${fn:length(formBean.marker.dataNotes) > 1}">
        <ul>
    </c:if>
    <c:forEach var="curatorNote" items="${formBean.marker.dataNotes}">
        <c:choose>
            <c:when test="${fn:length(formBean.marker.dataNotes) eq  1}">
                ${curatorNote.note}
            </c:when>
            <%--in this case must be greater than 1--%>
            <c:otherwise>
                    <li> ${curatorNote.note} </li>
            </c:otherwise>
        </c:choose>
    </c:forEach>
    <c:if test="${fn:length(formBean.marker.dataNotes) > 1}">
        </ul>
    </c:if>
   <br>
</c:if>
</authz:authorize>

<c:if test="${!(empty formBean.marker.publicComments)}">
    <b>Note: </b> ${zfn:escapeHtml(formBean.marker.publicComments)}
</c:if>

