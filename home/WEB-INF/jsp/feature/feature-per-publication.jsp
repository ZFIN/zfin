<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Features:</span></th>
        <td><span class="name-value"><zfin:link entity="${publication}"/></span> (${featureList.size()} features)</td>
    </tr>
</table>

<TABLE width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Feature
            </TD>
        </TR>
<c:forEach var="feature" items="${featureList}"  varStatus="loop">

            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${feature}"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </tbody>
</TABLE>
