<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    <div style="font-weight:bold; font-size:18px">ZFIN Abbreviations for Assays</div>
</div>
<div class="popup-body">

    <div>
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
    </div>
</div>
