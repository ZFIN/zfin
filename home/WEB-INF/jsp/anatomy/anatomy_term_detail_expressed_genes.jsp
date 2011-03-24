<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.AnatomySearchBean" scope="request"/>

<div class="summary">
<div class="summaryTitle">
    <span title="Genes with Most Figures, annotated to ${formBean.aoTerm.termName}, substructures excluded">
        Genes with Most Figures
    </span>
</div>
<c:if test="${formBean.expressedGenesExist}">
    <table class="summary rowstripes">
        <tbody>
        <tr>
            <th width="40%" colspan="2">
                Gene
            </th>
            <th width="60%">
                Figures
            </th>
        </tr>
        <c:forEach var="expressedGene" items="${formBean.allExpressedMarkers}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td colspan="2">
                    <zfin:link entity="${expressedGene.markerStat.gene}"/>
                </td>
                <td>
                    <c:if test="${expressedGene.markerStat.numberOfFigures > 0}">
                        <c:if test="${expressedGene.markerStat.numberOfFigures > 1}">
                            <zfin:createFiguresLink marker="${expressedGene.markerStat.gene}"
                                                    term="${formBean.aoTerm}"
                                                    numberOfFigures="${expressedGene.markerStat.numberOfFigures}"
                                                    wildtypeOnly="true"
                                                    useGeneZdbID="true"/>
                        </c:if>
                        <c:if test="${expressedGene.markerStat.numberOfFigures == 1 }">
                            <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${expressedGene.markerStat.figure.zdbID}'>
                                <zfin2:figureOrTextOnlyLink figure="${expressedGene.markerStat.figure}"
                                                            integerEntity="${expressedGene.markerStat.numberOfFigures}"/>
                            </a>
                        </c:if>
                    </c:if>
                    <c:if test="${expressedGene.markerStat.numberOfFigures > 1 }"><img src="/images/camera_icon.gif" border="0" alt="with image">&nbsp;from</c:if>
                    <c:if test="${expressedGene.markerStat.numberOfFigures == 1 }"><c:if test="${expressedGene.markerStat.figure.label ne 'text only' }"><img src="/images/camera_icon.gif" border="0" alt="with image">&nbsp;</c:if>from</c:if>
                    <c:if test="${expressedGene.markerStat.numberOfPublications ==1}">
                        <zfin:link entity="${expressedGene.markerStat.singlePublication}"/>
                    </c:if>
                    <c:if test="${expressedGene.markerStat.numberOfPublications > 1}">
                        <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                     integerEntity="${expressedGene.markerStat.numberOfPublications}"
                                     includeNumber="true"/>
                    </c:if>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
        </tbody>
    </TABLE>
</c:if>
<zfin2:anatomyTermDetailSectionCaption anatomyItem="${formBean.aoTerm}"
                                       recordsExist="${formBean.expressedGenesExist}"
                                       anatomyStatistics="${formBean.anatomyStatistics}"
                                       structureSearchLink="/${formBean.expressionSearchLink}"
                                       substructureSearchLink="/${formBean.expressionSearchLinkSubstructures}"
                                       choicePattern="0# genes| 1# gene| 2# genes"
                                       allRecordsAreDisplayed="${formBean.allExpressedGenesAreDisplayed}"
                                       totalRecordCount="${formBean.totalNumberOfExpressedGenes}"
                                       displayImages="true"
                                       imageCount="${formBean.totalNumberOfFiguresPerAnatomyItem}"/>
</div>