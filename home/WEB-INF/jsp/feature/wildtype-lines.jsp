<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<table width=100% border=0 cellspacing=0 cellpadding=1>
    <tr>
        <td width=20%>&nbsp;</td>
        <td witdh=60% align=center>
            <FONT SIZE=+1>Wild-Type Lines</font>
        </td>
    </tr>
</table>

<p>
<TABLE width=80% class="searchresults rowstripes">
    <tr>
        <th>Strain</th>
        <th>Current Source</th>
    </tr>
    <c:forEach var="genotype" items="${wildtypes}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td><zfin:link entity="${genotype}"/></td>
            <td>
                <c:forEach var="supplier" items="${genotype.suppliers}">
                    <zfin:link entity="${supplier.organization}"/>
                    <zfin2:orderThis organization="${supplier.organization}" accessionNumber="${genotype.zdbID}"/>
                    <br/>
                </c:forEach>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</TABLE>
