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
                                 groupByBean="symbol">

                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${omimTermDisplay}"
                                         groupByBean="symbol">
            <c:choose>
                <c:when test="${omimGene.orthology != null}">
                    <a href="http://omim.org/entry/${omimGene.omimAccession}">${omimGene.symbol}</a>
                </c:when>
                <c:otherwise>
                    <a href="http://omim.org/entry/${omimGene.humanGeneDetail.geneMimNumber}">${omimGene.symbol}</a>
                </c:otherwise>
            </c:choose>    
                     </zfin:groupByDisplay>
                </td>

                <td>
                    <c:if test="${omimGene.orthology != null}">
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${omimTermDisplay}"
                                         groupByBean="symbol">
                        <zfin2:toggledLinkList collection="${omimGene.zfinGene}" maxNumber="3"
                                               commaDelimited="true"/>

                    </zfin:groupByDisplay>
                    </c:if>

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