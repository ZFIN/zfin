<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%--<div class="summary">
    <span class="summaryTitle">All ${fn:length(engineeredRegions)} Engineered Regions</span>
</div>--%>

    <table>
    <tr>
        <%--<th align="left" width="5%"></th>--%>
        <th align="left">Name</th>
        <th align="left">Previous Name</th>
        <th align="left"> Comment</th>
<authz:authorize access="hasRole('root')">
        <th align="left"> Note</th>
</authz:authorize>
    </tr>
    <c:forEach var="marker" items="${engineeredRegions}" varStatus="status">
        <jsp:useBean id="marker" class="org.zfin.marker.Marker" scope="request"/>
        <zfin:alternating-tr loopName="status">
            <%--<td>${status.index+1}</td>--%>
            <%--<td>
                ${marker.name}
            </td>--%>
            <td>
                <zfin:link entity="${marker}"/>
            </td>
            <td>
                <c:forEach var="alias" items="${marker.aliases}" varStatus="loop">
                    ${alias.alias} <br/>
                </c:forEach>
            </td>
            <td>
                ${marker.publicComments}
            </td>
            <authz:authorize access="hasRole('root')">
            <td>
                <c:forEach var="note" items="${marker.dataNotes}" varStatus="loop">
                    ${note.note} <br/>
                </c:forEach>
            </td>
            </authz:authorize>
        </zfin:alternating-tr>
    </c:forEach>
</table>


