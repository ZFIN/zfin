<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width="100%">
    <tbody>
        <tr>
            <td width="90%">
                <font size="+1">
                    <center>
                        Antibodies List (<b>${fn:length(formBean)}</b> records) in <zfin:link entity="${publication}"/>

                    </center>
                </font>
            </td>
            <td width="10%">

                <input name="MIval" value="aa-input_welcome_generic.apg" type="hidden">
                <input name="page_name" value="ZFIN Antibodis per Publications " type="hidden">

                <table leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" border="0" cellpadding="0"
                       cellspacing="0">
                    <form method="post" action="/zezem/webdriver" target="comments"></form>
                    <tbody>
                        <tr>
                            <td>
                                <input value="Your Input Welcome" type="submit">
                            </td>
                        </tr>
                    </tbody>
                </table>
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
