<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%">
    <tbody>
        <tr>
            <td width="85%">
                <font size="+1">
                    <center>
                        Antibodies List (<b>${fn:length(formBean)}</b> records) in <zfin:link entity="${publication}"/>

                    </center>
                </font>
            </td>
        </tr>
    </tbody>
</table>

<TABLE width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Antibody Name
            </TD>
        </TR>
        <c:forEach var="antibody" items="${formBean}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${antibody}"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </tbody>
</TABLE>
