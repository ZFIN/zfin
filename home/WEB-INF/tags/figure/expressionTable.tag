<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="expressionTableRows" type="java.util.List" rtexprvalue="true" required="true" %>
<%@ attribute name="showQualifierColumn" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<a name="expDetail">
    <zfin2:subsection title="Gene Expression Details" anchor="expression"
                      test="${!empty expressionTableRows }" showNoData="true">

        <table class="summary groupstripes">
            <tr>
                <th class="gene">Gene</th>
                <th class="antibody">Antibody</th>
                <th class="fish"><span class="fish-label" title="Fish = Genotype + STR">Fish</span></th>
                <th>Conditions</th>
                <th class="stage">Stage</th>
                <c:if test="${showQualifierColumn}">
                    <th class="qualifier">Qualifier</th>
                </c:if>
                <th class="anatomy">Anatomy</th>
                <th> Assay <a class="popup-link info-popup-link" href="/action/expression/assay-abbrev-popup"></a></th>
            </tr>
            <c:forEach var="row" items="${expressionTableRows}" varStatus="loop">
                <zfin:alternating-tr loopName="loop" groupBeanCollection="${expressionTableRows}" groupByBean="geneGenoxZdbIDs" newGroup="true">
                    <td>
                        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${expressionTableRows}"
                                             groupByBean="gene">
                            <zfin:link entity="${row.gene}"/>
                        </zfin:groupByDisplay>
                    </td>
                    <td> <zfin:link entity="${row.antibody}"/> </td>
                    <td>
                        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${expressionTableRows}"
                                             groupByBean="geneGenoxZdbIDs">
                            <zfin:link entity="${row.fish}"/>
                        </zfin:groupByDisplay>
                    </td>
                    <td>
                        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${expressionTableRows}"
                                             groupByBean="geneGenoxZdbIDs">
                            <zfin:link entity="${row.experiment}" suppressMoDetails="true"/>
                        </zfin:groupByDisplay>
                    </td>
                    <td>
                        <zfin:link entity="${row.start}"/>
                        <c:if test="${row.start != row.end}">
                            to <zfin:link entity="${row.end}"/>
                        </c:if>
                    </td>
                    <c:if test="${showQualifierColumn}">
                        <td class="qualifier">${row.qualifier}</td>
                    </c:if>
                    <td> <zfin:link entity="${row.entity}"/> </td>
                    <td> ${row.assay.abbreviation} </td>

                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </zfin2:subsection>
</a>