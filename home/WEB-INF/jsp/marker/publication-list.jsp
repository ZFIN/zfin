<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

Total: ${fn:length(publications)}

<table>
    <c:forEach var="publication" items="${publications}" varStatus="loop">
        <c:if test="${publication.type ne 'Unpublished' and publication.type ne 'Curation'}">
            <tr>
                <%--<td>${loop.index}</td>--%>
                <td>
                    <zfin2:expandedPubLink webdriverPath="<%=ZfinProperties.getWebDriver()%>" publication="${publication}"/>
                </td>
            </tr>
        </c:if>
    </c:forEach>
</table>

<h4>Additional Citations</h4>
<table>
    <c:forEach var="publication" items="${publications}" varStatus="loop">
        <c:if test="${publication.type eq 'Unpublished' or publication.type eq 'Curation'}">
            <tr>
                <%--<td>${loop.index}</td>--%>
                <td>
                    <zfin2:expandedPubLink webdriverPath="<%=ZfinProperties.getWebDriver()%>" publication="${publication}"/>
                </td>
            </tr>
        </c:if>
    </c:forEach>
</table>

