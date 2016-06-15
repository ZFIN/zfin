<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="aliasAttribute" value="<%=ResultService.SYNONYMS%>"/>
<c:set var="affGenesAttribute" value="<%=ResultService.AFFECTED_GENES%>"/>

<table class="table-results searchresults" style="display: none;">
    <th>Name</th>
    <th>Synonym</th>
    <th>Affected Genes</th>
    <th>Feature Type</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>

            <td>${result.attributes[aliasAttribute]}</td>

            <td>${result.attributes[affGenesAttribute]}</td>
                        <td>${result.type}</td>

            <%--<td style="white-space: nowrap"> <c:if test="${!empty result.displayedID}">${result.id}</c:if> </td>--%>
            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>