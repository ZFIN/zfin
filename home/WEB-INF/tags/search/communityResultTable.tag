<%@ tag import="org.zfin.search.service.ResultService" %>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="results" required="true" type="java.util.List" %>
<c:set var="emailAttribute" value="<%=ResultService.EMAIL%>"/>
<c:set var="addressAttribute" value="<%=ResultService.ADDRESS%>"/>
<c:set var="lineDesigAttribute" value="<%=ResultService.LINE_DESIGNATION%>"/>

<c:choose>
    <c:when test="${fn:contains(result.id, 'PERS')}">
<table class="table-results searchresults" style="display: none;">
    <th>Name</th>
    <th>Email</th>
    <th>Address</th>
    <th>Image</th>
    <th>ZDB ID</th>
    <th>Related Data</th>
    <c:forEach var="result" items="${results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
            <td>${result.link}</td>

            <td>${result.attributes[emailAttribute]}</td>




            <td style="word-wrap: break-word">${result.attributes[addressAttribute]}</td>
            <td>
            <c:if test="${not empty result.image}">
                <zfin-search:imageModal result="${result}"/>
            </c:if>
            </td>
            <td>${result.id}</td>

            <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
</c:when>
<c:otherwise>
    <table class="table-results searchresults" style="display: none;">
        <th>Name</th>
        <th>Email</th>
        <th>Address</th>
        <th>Image</th>
        <th>Line Designation</th>
        <th>ZDB ID</th>
        <th>Related Data</th>
        <c:forEach var="result" items="${results}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="id">
                <td>${result.link}</td>

                <td>${result.attributes[emailAttribute]}</td>




                <td style="word-wrap: break-word">${result.attributes[addressAttribute]}</td>
                <td>
                    <c:if test="${not empty result.profileImage}">
                        <div class="pull-right result-thumbnail-container">
                            <div class="search-result-thumbnail">
                                <a href="${result.url}">
                                    <img style="max-width: 150px; max-height: 70px;"
                                         src="<%=ZfinPropertiesEnum.IMAGE_LOAD.value()%>/${result.profileImage}">
                                </a>
                            </div>
                        </div>
                    </c:if>
                </td>
                <td>${result.attributes[lineDesigAttribute]}</td>
                <td>${result.id}</td>

                <td><zfin-search:relatedLinkMenu links="${result.relatedLinks}"/></td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</c:otherwise>
</c:choose>