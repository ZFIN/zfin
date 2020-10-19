<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <table class="primary-entity-attributes">
        <tr>
            <th><span class="name-label">Fish:</span></th>
            <td><span class="name-value"><zfin:link entity="${publication}"/></span></td>
        </tr>
    </table>

    <TABLE width="100%">
        <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Fish Name
            </TD>
        </TR>
        <c:forEach var="fish" items="${fishList}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${fish}"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
        </tbody>
    </TABLE>
</z:page>


