<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.antibody.presentation.AntibodyBean" scope="request"/>

<c:set var="antibodyDetailLabeling" value="${formBean.antibodyStat.antibodyDetailedLabelings}" />

<c:if test="${fn:length(antibodyDetailLabeling) ne null && fn:length(antibodyDetailLabeling) > 0}">
    <div id="short-version">
        <table class="summary groupstripes">
            <tr>
                <th>Anatomy</th>
                <th>Stage</th>
                <th><a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxassayabbrev.apg">Assay</a></th>
                <th>Gene</th>
                <th>Data</th>
            </tr>

            <c:forEach var="detailedLabeling" items="${antibodyDetailLabeling}"
                       varStatus="loop" end="4">
                <zfin:alternating-tr loopName="loop"
                                     groupBeanCollection="${antibodyDetailLabeling}"
                                     groupByBean="expressionStatement">
                    <td>
                        <zfin:groupByDisplay
                                loopName="loop"
                                             groupBeanCollection="${antibodyDetailLabeling}"
                                             groupByBean="expressionStatement">
                            <zfin:link entity="${detailedLabeling.expressionStatement}"/>
                        </zfin:groupByDisplay>
                    </td>
                    <td>
                        <zfin:link entity="${detailedLabeling.startStage}"/>
                        <c:if test="${detailedLabeling.startStage != detailedLabeling.endStage}">
                            &nbsp;to&nbsp;<zfin:link entity="${detailedLabeling.endStage}"/>
                        </c:if>
                    </td>
                    <td>
                        <c:forEach var="assay" items="${detailedLabeling.assays}">
                            ${assay.abbreviation}
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
                                    <a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${detailedLabeling.singleFigure.zdbID}" id="${detailedLabeling.singleFigure.zdbID}">
                                       <zfin2:figureOrTextOnlyLink figure="${detailedLabeling.singleFigure}"
                                                            integerEntity="${detailedLabeling.numberOfFigures}"/>
                                </c:when>
                                <c:otherwise>
                                    <a href="figure-summary?antibody.zdbID=${formBean.antibody.zdbID}&superTerm.zdbID=${detailedLabeling.superterm.zdbID}&subTerm.zdbID=${detailedLabeling.subterm.zdbID}&startStage.zdbID=${detailedLabeling.startStage.zdbID}&endStage.zdbID=${detailedLabeling.endStage.zdbID}&onlyFiguresWithImg=false">
                                    ${detailedLabeling.numberOfFiguresDisplay}
                                </c:otherwise>
                            </c:choose>
                            <c:choose>
                                <c:when test="${detailedLabeling.figureWithImage}">
                                    <img src="/images/camera_icon.gif" alt="with image" image="" border="0"></a>
                                </c:when>
                                <c:otherwise>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                            &nbsp;from&nbsp;
                            <c:if test="${detailedLabeling.numberOfPublications > 1}">${detailedLabeling.numberOfPublicationsDisplay}</c:if>
                            <c:if test="${detailedLabeling.numberOfPublications == 1}">
                                <zfin:link entity="${detailedLabeling.singlePublication}"/>
                            </c:if>
                        </c:if>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
        <div>
            <c:if test="${fn:length(antibodyDetailLabeling) > 5}">
                &nbsp;&nbsp;
                <a href="javascript:expand()">
                    <img src="/images/darrow.gif" alt="expand" border="0">
                    Show all</a>
            ${formBean.antibodyStat.numberOfDistinctComposedTerms} labeled structures
            </c:if>
        </div>
    </div>

    <div style="display:none" id="long-version">
        <table class="summary groupstripes">
            <tr>
                <th>Anatomy</th>
                <th>Stage</th>
                <th><a href="/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxassayabbrev.apg">Assay</a></th>
                <th>Gene</th>
                <th>Data</th>
            </tr>

            <c:forEach var="detailedLabeling" items="${antibodyDetailLabeling}"
                       varStatus="loop">
                <zfin:alternating-tr loopName="loop"
                                     groupBeanCollection="${antibodyDetailLabeling}"
                                     groupByBean="expressionStatement">
                    <td>
                        <zfin:groupByDisplay loopName="loop"
                                             groupBeanCollection="${antibodyDetailLabeling}"
                                             groupByBean="expressionStatement">
                            <zfin:link entity="${detailedLabeling.expressionStatement}"/>
                        </zfin:groupByDisplay>
                    </td>
                    <td>
                        <zfin:link entity="${detailedLabeling.startStage}"/>
                        <c:if test="${detailedLabeling.startStage != detailedLabeling.endStage}">
                            &nbsp;to&nbsp;<zfin:link entity="${detailedLabeling.endStage}"/>
                        </c:if>
                    </td>
                    <td>
                        <c:forEach var="assay" items="${detailedLabeling.assays}">
                            ${assay.abbreviation}
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
                                    <a href="/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${detailedLabeling.singleFigure.zdbID}">
                                       <zfin2:figureOrTextOnlyLink figure="${detailedLabeling.singleFigure}"
                                                            integerEntity="${detailedLabeling.numberOfFigures}"/>
                                </c:when>
                                <c:otherwise>
                                    <a href="figure-summary?antibody.zdbID=${formBean.antibody.zdbID}&superTerm.zdbID=${detailedLabeling.superterm.zdbID}&subTerm.zdbID=${detailedLabeling.subterm.zdbID}&startStage.zdbID=${detailedLabeling.startStage.zdbID}&endStage.zdbID=${detailedLabeling.endStage.zdbID}&onlyFiguresWithImg=false">
                                    ${detailedLabeling.numberOfFiguresDisplay}
                                </c:otherwise>
                            </c:choose>
                            <c:choose>
                                <c:when test="${detailedLabeling.figureWithImage}">
                                    <img src="/images/camera_icon.gif" alt="with image" border="0"></a>
                                </c:when>
                                <c:otherwise>
                                    </a>
                                </c:otherwise>
                            </c:choose>
                            &nbsp;from&nbsp;
                            <c:if test="${detailedLabeling.numberOfPublications > 1}">${detailedLabeling.numberOfPublicationsDisplay}</c:if>
                            <c:if test="${detailedLabeling.numberOfPublications == 1}">
                                <zfin:link entity="${detailedLabeling.singlePublication}"/>
                            </c:if>
                        </c:if>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
            <tr>
                <td>

                </td>
            </tr>
        </table>
        <div>
            &nbsp;&nbsp;
            <a href="javascript:collapse()">
                <img src="/images/up.gif" alt="expand" title="Show first 5 structures" border="0">
                Show first</a> 5 labeled structures
        </div>
    </div>
</c:if>


<script type="text/javascript">
    function expand() {
        document.getElementById('short-version').style.display = 'none';
        document.getElementById('long-version').style.display = 'inline';
    }

    function collapse() {
        document.getElementById('short-version').style.display = 'inline';
        document.getElementById('long-version').style.display = 'none';
    }
</script>
