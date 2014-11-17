<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="phenotypeTableRows" type="java.util.List" rtexprvalue="true" required="true" %>

<a name="phenoDetail">
    <zfin2:subsection title="Phenotype Details " anchor="phenotype"
                      test="${!empty phenotypeTableRows }" showNoData="true">

        <table class="summary groupstripes">
            <tr>
                <th><span class="fish-label" title="Fish = Genotype + Reagents">Fish</span></th>
                <th>Stage</th>
                <th>Phenotype</th>
            </tr>
            <c:forEach var="row" items="${phenotypeTableRows}" varStatus="loop">
                <zfin:alternating-tr loopName="loop"
                                     groupBeanCollection="${phenotypeTableRows}"
                                     groupByBean="genotypeExperiment" newGroup="true">

                    <td>
                        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${phenotypeTableRows}"
                                             groupByBean="genotypeExperiment">
                            <zfin:link entity="${row.genotypeExperiment}"/>
                        </zfin:groupByDisplay>
                    </td>
                    <td>
                        <zfin:link entity="${row.start}"/>
                        <c:if test="${row.start != row.end}">
                            - <zfin:link entity="${row.end}"/>
                        </c:if>
                    </td>
                    <td> <zfin:link entity="${row.phenotypeStatement}"/> </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>

    </zfin2:subsection>
</a>

