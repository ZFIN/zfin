<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<c:set var="conditionsAttribute" value="<%=ResultService.CONDITIONS%>"/>
<c:set var="expressionAttribute" value="<%=ResultService.EXPRESSION%>"/>


<table class="table-results searchresults" style="display: none;">
    <th>Expressed Gene</th>
    <th>Antibody</th>
    <th>Fish</th>
    <th>Conditions</th>
    <th>Expression</th>
    <th>Figure</th>
    <th></th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td><zfin:link entity="${result.entity.gene}"/> </td>
            <td><zfin:link entity="${result.entity.antibody}"/> </td>
            <td>
               <zfin:link entity="${result.entity.fishExperiment.fish}" suppressPopupLink="true"/>
            </td>
            <td>${result.attributes[conditionsAttribute]}</td>
            <td class="anatomy">
                <div class="list-collapse">
                        ${result.attributes[expressionAttribute]}
                </div>
            </td>
            <td>
                <a href="/${result.figure.zdbID}">
                    ${result.entity.publication.shortAuthorList}, ${result.figure.label}
                </a>
            </td>
            <td>
                <c:if test="${not empty result.image}">
                    <zfin-search:imageModal result="${result}"/>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>