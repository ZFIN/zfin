<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="antibodyDetailLabeling" value="${formBean.antibodyDetailedLabelings}"/>

<z:dataTable collapse="true"
             hasData="${fn:length(antibodyDetailLabeling) ne null && fn:length(antibodyDetailLabeling) > 0}">
    <thead>
        <tr>
            <th>Anatomy</th>
            <th>Stage</th>
            <th>Assay <a class="popup-link info-popup-link" href="/action/expression/assay-abbrev-popup"></a></th>
            <th>Gene</th>
            <th>Data</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="detailedLabeling" items="${antibodyDetailLabeling}" varStatus="loop">
            <tr>
                <td>
                    <zfin:link entity="${detailedLabeling.expressionStatement}"/>
                </td>
                <td>
                    <zfin:link entity="${detailedLabeling.startStage}"/>
                    <c:if test="${detailedLabeling.startStage != detailedLabeling.endStage}">
                        &nbsp;to&nbsp;<zfin:link entity="${detailedLabeling.endStage}"/>
                    </c:if>
                </td>
                <td>
                    <c:forEach var="gene" items="${detailedLabeling.assays}">
                        ${gene.abbreviation}
                    </c:forEach>
                </td>

                <td>
                    <c:forEach var="probeStats" items="${detailedLabeling.antigenGenes}" varStatus="status">
                        <zfin:link entity="${probeStats}"/><c:if test="${!status.last}">,&nbsp;</c:if>
                    </c:forEach>
                </td>
                <td>
                    <c:if test="${detailedLabeling.numberOfFigures > 0}">
                        <c:choose>
                            <c:when test="${detailedLabeling.numberOfFigures == 1}">
                                <a href="/${detailedLabeling.singleFigure.zdbID}"
                                   id="${detailedLabeling.singleFigure.zdbID}">
                                    <zfin2:figureOrTextOnlyLink figure="${detailedLabeling.singleFigure}"
                                                                integerEntity="${detailedLabeling.numberOfFigures}"/>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="/action/antibody/antibody-figure-summary?antibodyID=${formBean.marker.zdbID}&superTermID=${detailedLabeling.superterm.zdbID}&subTermID=${detailedLabeling.subterm.zdbID}&startStageID=${detailedLabeling.startStage.zdbID}&endStageID=${detailedLabeling.endStage.zdbID}&figuresWithImg=false">
                                        ${detailedLabeling.numberOfFiguresDisplay}
                                </a>
                            </c:otherwise>
                        </c:choose>
                        <zfin2:showCameraIcon hasImage="${detailedLabeling.figureWithImage}"/></a>
                        &nbsp;from&nbsp;
                        <c:if test="${detailedLabeling.numberOfPublications > 1}">${detailedLabeling.numberOfPublicationsDisplay}</c:if>
                        <c:if test="${detailedLabeling.numberOfPublications == 1}">
                            <zfin:link entity="${detailedLabeling.singlePublication}"/>
                        </c:if>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>