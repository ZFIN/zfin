<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>
<%@ attribute name="data" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="title" required="true" rtexprvalue="true" type="java.lang.String" %>


<c:if test="${!empty data}">
    <div class="summary">
        <span class="summaryTitle">${title}</span>
        <table class="summary horizontal-solidblock">
            <tr>
                <td>
                    <c:forEach var="entry" items="${data}" varStatus="loop">
                        <zfin:link entity="${entry}"/><zfin:attribution entity="${entry}"/>${!loop.last ? ", " : ""}
                    </c:forEach>
                </td>
            </tr>
        </table>
    </div>
</c:if>
