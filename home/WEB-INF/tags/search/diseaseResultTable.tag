<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="aliasAttribute" value="<%=ResultService.SYNONYMS%>"/>

<table class="table-results searchresults" style="display: none;">
    <th>Name</th>
    <th>Synonyms</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>
            <td>${result.attributes[aliasAttribute]}</td>
             <td>
                <div class="btn-group">
                    <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        Related <span class="caret"></span>
                    </button>
                    <ul class="dropdown-menu pull-right">
                        <c:forEach var="link" items="${result.relatedLinks}">
                            <li>${link}</li>
                        </c:forEach>
                    </ul>
                </div>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>