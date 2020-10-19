<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <table class="primary-entity-attributes">
        <tr>
            <th><span class="name-label">${totalRecords} records directly attributed to:</span></th>
            <td><span class="name-value"><zfin:link entity="${publication}"/></span</td>
        </tr>
    </table>

    <table class="summary rowstripes" style="margin-top: 1em;">
        <c:forEach var="dataId" items="${directedAttributedData}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>${dataId}</td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</z:page>