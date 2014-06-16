<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="title" type="java.lang.String" required="false" %>


<zfin2:subsection title="${title}" showNoData="true" test="${!empty evidenceOrthologyList}">
    <table class="summary rowstripes">
        <tr>
            <th>Evidence Code<a class="popup-link info-popup-link" href="/zf_info/oev.html"></a></th>
            <th> Zebrafish</th>
            <th> Human</th>
            <th> Mouse</th>
            <th> Fruit fly</th>
            <th> Publication</th>
            <th width="30%"></th>
        </tr>
        <c:forEach var="orthology" items="${evidenceOrthologyList}" varStatus="loop">
            <zfin:alternating-tr loopName="loop"
                                 groupBeanCollection="${evidenceOrthologyList}"
                                 groupByBean="evidenceCode.code"
                                 showRowStyleClass="true">
                <td>
                    <zfin:groupByDisplay loopName="loop"
                                         groupBeanCollection="${evidenceOrthologyList}"
                                         groupByBean="evidenceCode.code">
                        ${orthology.evidenceCode.code}
                    </zfin:groupByDisplay>

                </td>
                <td><img src="/images/fill_green_ball.gif" border="0" height="10"></td>
                <td>
                    <c:if test="${orthology.containsSpeciesString('Human')}">
                        <img src="/images/fill_green_ball.gif" border="0" height="10">
                    </c:if>
                </td>
                <td>
                    <c:if test="${orthology.containsSpeciesString('Mouse')}">
                        <img src="/images/fill_green_ball.gif" border="0" height="10">
                    </c:if>
                </td>
                <td>
                    <c:if test="${orthology.containsSpeciesString('Fruit fly')}">
                        <img src="/images/fill_green_ball.gif" border="0" height="10">
                    </c:if>
                </td>
                <td><zfin:link entity="${orthology.publication}"/></td>
                <td></td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</zfin2:subsection>

