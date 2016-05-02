<%@ attribute name="formBean" type="org.zfin.ontology.presentation.OntologyBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<c:set var="omimTermDisplay" value="${formBean.omimPhenos}"/>
<zfin2:subsection title="GENES INVOLVED" showNoData="true"
                  test="${fn:length(omimTermDisplay) ne null && fn:length(omimTermDisplay) > 0}">
    <table class="summary groupstripes">
        <tr>
            <th>Human Gene</th>
            <th>
                Zebrafish Ortholog
            </th>
            <th>
                OMIM Term
            </th>
            <th>
                OMIM Phenotype ID
            </th>
        </tr>


        <c:forEach var="omimGene" items="${omimTermDisplay}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${omimTermDisplay}"
                                 groupByBean="orthology.symbol">

                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${omimTermDisplay}"
                                         groupByBean="orthology.symbol">
                        <a href="http://omim.org/entry/${omimGene.omimAccession}">${omimGene.orthology.symbol}</a>

                        <%--${omimGene.orthology.abbreviation}--%>
                    </zfin:groupByDisplay>
                </td>

                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${omimTermDisplay}"
                                         groupByBean="orthology.symbol">
                        <zfin2:toggledHyperlinkList collection="${omimGene.zfinGene}" maxNumber="3"
                                                    id="zfinGene" commaDelimited="true"/>

                    </zfin:groupByDisplay>


                </td>
                <td>
                        ${omimGene.name}
                </td>
                <td>
                    <a href="http://omim.org/entry/${omimGene.omimNum}">${omimGene.omimNum}</a>

                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</zfin2:subsection>