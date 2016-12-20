<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="aliasAttribute" value="<%=ResultService.SYNONYMS%>"/>
<c:set var="commentAttribute" value="<%=ResultService.COMMENT%>"/>
<c:set var="locationAttribute" value="<%=ResultService.LOCATION%>"/>
<c:set var="ccgAttribute" value="<%=ResultService.CLONE_CONTAINS_GENES%>"/>
<c:set var="ceAttribute" value="<%=ResultService.CLONE_ENCODED_BY_GENES%>"/>
<c:set var="problemTypeAttribute" value="<%=ResultService.CLONE_PROBLEM_TYPE%>"/>
<c:set var="qualityAttribute" value="<%=ResultService.QUALITY%>"/>

<table class="table-results searchresults" style="display: none;">
    <th>Name</th>
    <th>Synonym</th>
    <th>Type</th>
    <th>Comment</th>
    <th>Clone Contains Gene</th>
    <th>Clone Encodes</th>
    <th>Problem Type</th>
    <th>Probe Quality</th>
    <th>Location</th>
    <th>ZDB ID</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>

            <td>${result.attributes[aliasAttribute]}</td>

            <td>${result.type}</td>
            <td>${result.attributes[commentAttribute]}</td>

            <td style="word-wrap: break-word">${result.attributes[ccgAttribute]}</td>
            <td>${result.attributes[ceAttribute]}</td>
            <td>${result.attributes[problemTypeAttribute]}</td>
            <td>${result.attributes[qualityAttribute]}</td>
            <td>${result.attributes[locationAttribute]}</td>
            <td>${result.id}</td>

            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
