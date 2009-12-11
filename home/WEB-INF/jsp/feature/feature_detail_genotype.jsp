<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<b>Mutant and Transgenic Lines</b>


    <table width="100%">
        <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Genotype (Background)
            </TD>
            <TD width="20%">
                Affected Genes
            </TD>
            <TD width="20%">
                Phenotype
            </TD>
            <TD width="40%">
                Figures
            </TD>
        </TR>
        <c:forEach var="featgenoStat" items="${formBean.featgenoStats}">
            <tr class="search-result-table-entries">
                <td>
                    <zfin:link entity="${featgenoStat.genotype}"/>
                    <c:if test="${featgenoStat.genotype.background ne null}">
                        (${featgenoStat.genotype.background.name})
                    </c:if>

                </td>
                <td>
                    <zfin:link entity="${featgenoStat.affectedMarkers}"/>
                </td>
                <td>
                    <%--<c:forEach var="phenotypes" items="${genoStat.phenotypeDescriptions}"
                               varStatus="ontologyLoop">
                    <c:if test="${phenotypes.key != 'ANATOMY'}">
                    [${phenotypes.key}]:
                    <div style="margin-left:20px">
                        </c:if>
                        <c:if test="${phenotypes.key == 'ANATOMY'}">
                        <div style="margin-left:0px">
                            </c:if>
                            <c:forEach var="phenotype" items="${phenotypes.value}" varStatus="loop">
                                ${phenotype}<c:if test="${!loop.last}">, </c:if>
                            </c:forEach>
                            <c:if test="${!ontologyLoop.last}">
                        </div>
                        </c:if>
                        </c:forEach>--%>
                </td>
                <td>
                   <%-- <c:if test="${genoStat.numberOfFigures > 0}">
                        <c:if test="${genoStat.numberOfFigures > 1}">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pheno_summary.apg&OID=${genoStat.genotype.zdbID}&anatID=${formBean.anatomyItem.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${genoStat.numberOfFigures}" includeNumber="true"/></a>
                        </c:if>
                        <c:if test="${genoStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${genoStat.figure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${genoStat.figure}"
                                                            integerEntity="${genoStat.numberOfFigures}"/>
                            </a>
                        </c:if>
                    </c:if>
                    <c:if test="${genoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                    from
                    <c:if test="${genoStat.numberOfPublications ==1}">
                        <zfin:link entity="${genoStat.singlePublication}"/>
                    </c:if>
                    <c:if test="${genoStat.numberOfPublications > 1}">
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${genoStat.numberOfPublications}"
                                     includeNumber="true"/>
                    </c:if>
                    <c:if test="${genoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                </td>
            </tr> --%>
        </c:forEach>
        </tbody>
    </table>

<%--<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.anatomyItem}"
                                       recordsExist="${formBean.mutantsExist}"
                                       anatomyStatistics="${formBean.anatomyStatisticsMutant}"
                                       structureSearchLink="?MIval=aa-fishselect.apg&fsel_anatomy_item_id=${formBean.anatomyItem.zdbID}&WINSIZE=20&include_substructures=unchecked"
                                       substructureSearchLink="/${formBean.mutantSearchLinkSubstructures}"
                                       choicePattern="0# genotypes| 1# genotype| 2# genotypes"
                                       allRecordsAreDisplayed="${formBean.allGenotypesAreDisplayed}"
                                       totalRecordCount="${formBean.genotypeCount}"
                                       useWebdriverURL="true"/>--%>
</html>