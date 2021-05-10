<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="antibodyTableRows" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="showQualifierColumn" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<zfin2:subsection title="Antibody Labeling Details " anchor="antibody"
                  test="${!empty antibodyTableRows }" showNoData="true">
    <table class="summary groupstripes">
        <tr>
            <th class="antibody">Antibody</th>
            <th> Assay <a class="popup-link info-popup-link" href="/action/expression/assay-abbrev-popup"></a></th>
            <th class="fish"><span class="fish-label" title="Fish = Genotype + STR">Fish</span></th>
            <th>Conditions</th>
            <th class="stage">Stage</th>
            <c:if test="${showQualifierColumn}">
                <th class="qualifier">Qualifier</th>
            </c:if>
            <th class="anatomy">Anatomy</th>
        </tr>
        <c:forEach var="row" items="${antibodyTableRows}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${antibodyTableRows}" groupByBean="antibodyGenoxZdbIDs" newGroup="true">
                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${antibodyTableRows}"
                                         groupByBean="antibody">
                        <zfin:link entity="${row.antibody}"/>
                    </zfin:groupByDisplay>
                </td>
                <td> ${row.assay.abbreviation} </td>
                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${antibodyTableRows}"
                                         groupByBean="antibodyGenoxZdbIDs">
                        <zfin:link entity="${row.fish}"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${antibodyTableRows}"
                                         groupByBean="antibodyGenoxZdbIDs">
                        <zfin:link entity="${row.experiment}" suppressMoDetails="true"/>
                    </zfin:groupByDisplay>
                </td>
                <td>
                    <zfin:link entity="${row.start}"/>
                    <c:if test="${row.start != row.end}">
                        - <zfin:link entity="${row.end}"/>
                    </c:if>
                </td>
                <c:if test="${showQualifierColumn}">
                    <td class="qualifier">${row.qualifier}</td>
                </c:if>
                <td> <zfin:link entity="${row.entity}"/> </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

</zfin2:subsection>