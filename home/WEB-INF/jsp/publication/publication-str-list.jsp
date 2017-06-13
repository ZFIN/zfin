<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin-figure:publicationInfo publication="${publication}"
                             showThisseInSituLink="false"
                             showErrataAndNotes="false"/>

<table class="summary rowstripes" style="margin-top: 1em;">
    <caption>
        ${markerType.displayName} List (${numSTRs} Records)
    </caption>
    <tr>
        <th>Target</th>
        <th>Reagent</th>
    </tr>
    <c:forEach var="row" items="${rows}" varStatus="loop">
        <zfin:alternating-tr loopName="loop" groupBeanCollection="${rows}" groupByBean="target">
            <td>
                <zfin:groupByDisplay loopName="loop" groupBeanCollection="${rows}" groupByBean="target">
                    <zfin:link entity="${row.target}" />
                </zfin:groupByDisplay>
            </td>
            <td><zfin:link entity="${row.str}" /></td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
