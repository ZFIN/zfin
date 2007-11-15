<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<TABLE width="100%">
    <tbody>
        <TR class="search-result-table-header">
            <TD width="20%">
                Gene Symbol
            </TD>
            <TD width="20%">
                Probe
            </TD>
            <TD width="60%">
                Figures
            </TD>
        </TR>
        <c:if test="${!formBean.inSituProbesExist}">
            <tr>
                <td colspan="4">No data available</td>
            </tr>
        </c:if>
        <c:if test="${formBean.inSituProbesExist}">
            <c:forEach var="gene" items="${formBean.highQualityProbeGenes}">
                <tr class="search-result-table-entries">
                    <td>
                        <zfin:link entity="${gene.gene}"/>
                    </td>
                    <td>
                        <zfin:link entity="${gene.subGene}"/>
                    </td>
                    <td>
                        <c:if test="${fn:length(gene.figures) > 0}">
                            <!-- link to figure search page if more than one figure available-->
                            <c:if test="${fn:length(gene.figures) > 1}">
                                <zfin:createFiguresLink marker="${gene.subGene}" anatomyItem="${formBean.anatomyItem}"
                                                        numberOfFiguresCollection="${gene.figures}" author="Thisse"
                                                        useGeneZdbID="false"/>
                            </c:if>
                            <!-- If one one figure available go directly to the figure page -->
                            <c:if test="${fn:length(gene.figures) == 1}">
                                <a href='/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${gene.figure.zdbID}'>
                                    <zfin:choice choicePattern="0#Figures| 1#Figure| 2#Figures"
                                                 collectionEntity="${gene.figures}"
                                                 includeNumber="true"/>
                                </a>
                            </c:if>
                            from
                            <zfin:link entity="${gene.probePublication}"/>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${formBean.numberOfHighQualityProbes > 5 }">
                <tr>
                    <td colspan="4" align="left">
                        Show all
                        <a href="/action/anatomy/high-quality-probes?anatomyItem.zdbID=<c:out value='${formBean.anatomyItem.zdbID}' />">
                                ${formBean.numberOfHighQualityProbes}
                            Probes
                        </a>
                    </td>
                </tr>
            </c:if>
        </c:if>
    </tbody>
</TABLE>
