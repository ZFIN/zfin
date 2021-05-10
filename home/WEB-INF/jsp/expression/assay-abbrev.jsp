<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<meta name="assay-abbrev-page"/> <%-- this is used by the web testing framework to know which page it is --%>


<%--This file is not in use anymore. assay-abbrev-popup.jsp shows the same content in a popup format.--%>
<div style="font-weight:bold; font-size:18px">ZFIN Abbreviations for Assays</div>
<br/>

<table class="summary groupstripes">
    <tr>
        <th class="name">Name</th>
        <th class="abbreviation">Abbreviation</th>
    </tr>
    <c:forEach var="row" items="${assay}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${assay}" groupByBean="name" newGroup="true">
            <td> <b>${row.abbreviation}</b> </td>
            <td> ${row.name} </td>
        </zfin:alternating-tr>
    </c:forEach>

</table>
<br/>
