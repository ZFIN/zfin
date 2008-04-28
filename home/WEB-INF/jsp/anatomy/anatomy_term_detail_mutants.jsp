<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

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
        <c:if test="${!formBean.mutantsExist}">
            <tr>
                <td colspan="3">No data available</td>
            </tr>
        </c:if>
        <c:forEach var="genoStat" items="${formBean.genotypeStatistics}">
            <tr class="search-result-table-entries">
                <td>
                    <zfin:link entity="${genoStat.genotype}"/>
                    <c:if test="${genoStat.genotype.background ne null}">
                        (${genoStat.genotype.background.name})
                    </c:if>

                </td>
                <td>
                    <zfin:link entity="${genoStat.affectedMarkers}"/>
                </td>
                <td>
                    <c:forEach var="phenotypes" items="${genoStat.phenotypeDescriptions}"
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
                        </c:forEach>
                </td>
                <td>
                    <c:if test="${genoStat.numberOfFigures > 0}">
                        <c:if test="${genoStat.numberOfFigures > 1}">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-pheno_summary.apg&OID=${genoStat.genotype.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${genoStat.numberOfFigures}" includeNumber="true"/></a>
                        </c:if>
                        <c:if test="${genoStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${genoStat.figure.zdbID}'>
                                <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                             integerEntity="${genoStat.numberOfFigures}" includeNumber="true"/>
                            </a>
                        </c:if>
                        from
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${genoStat.numberOfPublications}" includeNumber="true"/>
                    </c:if>
                    <c:if test="${genoStat.numberOfFigures == 0}">
                        --
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<c:if test="${!formBean.allGenotypesAreDisplayed}">
    <table width="100%">
        <tbody>
            <tr align="left">
                <td>
                    Show all
                    <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fishselect.apg&fsel_anatomy_item_id=<c:out value='${formBean.anatomyItem.zdbID}' />&WINSIZE=20">
                            ${formBean.genotypeCount} 
                        <zfin:choice choicePattern="0# genotypes| 1# genotype| 2# genotypes"
                                     integerEntity="${formBean.genotypeCount}"/>
                    </a> &nbsp;
                    (including substructures
                    <a href='/${formBean.mutantSearchLinkSubstructures}'>
                        <zfin:choice choicePattern="0#genotypes| 1#genotype| 2#genotypes"
                                     integerEntity="${formBean.anatomyStatisticsMutant.numberOfTotalDistinctObjects}"
                                     includeNumber="true"/>
                    </a>)
                </td>
            </tr>
        </tbody>
    </table>
</c:if>