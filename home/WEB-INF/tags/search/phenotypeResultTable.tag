<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>

<table class="table-results searchresults" style="display: none;">
    <thead>
    <tr>
        <th>Fish</th>
        <th>Construct</th>
        <th>Conditions</th>
        <th>Stage</th>
        <th>Phenotype</th>
        <th>Figure</th>
        <th></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>
                <zfin:link entity="${result.entity.fishExperiment.fish}" suppressPopupLink="true"/>
            </td>
            <td>${result.attributes['Construct:']}</td>
            <td>${result.attributes['Conditions:']}</td>
            <td>${result.attributes['Stage:']}</td>
            <td class="anatomy">
                <div class="list-collapse">
                    ${result.attributes['Phenotype:']}
                </div>
            </td>
            <td>
                <a href="/${result.entity.figure.zdbID}">
                    ${result.entity.figure.publication.shortAuthorList}, ${result.entity.figure.label}
                </a>
            </td>
            <td>
                <c:if test="${not empty result.image}">
                    <zfin-search:imageModal result="${result}"/>
                </c:if>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
    </tbody>
</table>