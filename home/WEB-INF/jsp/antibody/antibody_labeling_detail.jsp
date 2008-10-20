<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<c:if test="${fn:length(formBean.antibodyStat.antibodyDetailedLabelings) ne null && fn:length(formBean.antibodyStat.antibodyDetailedLabelings) > 0}">
    <div id="short-version">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr bgcolor="#ccccc0">
                <td><b>Anatomy</b></td>
                <td><b>Stage</b></td>
                <td><b><a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxassayabbrev.apg">Assay</a></b></td>
                <td><b>Gene</b></td>
                <td><b>Data</b></td>
            </tr>

            <% int alternateShadingIndx = 0; %>
            <c:forEach var="detailedLabeling" items="${formBean.antibodyStat.antibodyDetailedLabelings}"
                       varStatus="loop"
                       end="4">
                <c:if test="${loop.index == 0 || formBean.antibodyStat.antibodyDetailedLabelings[loop.index].aoAndPostCompostTerm ne formBean.antibodyStat.antibodyDetailedLabelings[status.index -1].aoAndPostCompostTerm}">
                    <% alternateShadingIndx++; %>
                </c:if>
                <% if (alternateShadingIndx % 2 == 1) { %>
                <tr class="odd">
                        <% } else { %>
                <tr>
                    <% } %>
                    <td>
                        <c:if test="${loop.index == 0 || formBean.antibodyStat.antibodyDetailedLabelings[loop.index].aoAndPostCompostTerm ne formBean.antibodyStat.antibodyDetailedLabelings[loop.index -1].aoAndPostCompostTerm}">
                            <zfin:link entity="${detailedLabeling.anatomyItem}"/><c:if
                                test="${detailedLabeling.cellularComponent != null}"> : <zfin:link
                                entity="${detailedLabeling.cellularComponent}"/></c:if>
                        </c:if>
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
                        <c:forEach var="gene" items="${detailedLabeling.antigenGenes}" varStatus="status">
                            <zfin:link entity="${gene}"/>
                            <c:if test="${!status.last}">
                                ,&nbsp;
                            </c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <c:if test="${detailedLabeling.numberOfFigures > 0}">
                            <c:choose>
                                <c:when test="${detailedLabeling.numberOfFigures == 1}">
                                    <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxfigureview.apg&OID=${detailedLabeling.singleFigure.zdbID}">
                                    ${detailedLabeling.numberOfFiguresDisplay}
                                </c:when>
                                <c:otherwise>
                                    <a href="figure-summary?antibody.zdbID=${formBean.antibody.zdbID}&anatomyItem.zdbID=${detailedLabeling.anatomyItem.zdbID}&startStage.zdbID=${detailedLabeling.startStage.zdbID}&endStage.zdbID=${detailedLabeling.endStage.zdbID}&onlyFiguesWithImg=false">
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
                                ${detailedLabeling.singlePublication.shortAuthorList}
                            </c:if>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td>
                    <c:if test="${fn:length(formBean.antibodyStat.antibodyDetailedLabelings) > 5}">
                        <br/>&nbsp;&nbsp;
                        <a href="javascript:expand()">
                            <img onclick="showAnatomyList('${id}', true)"
                                 src="/images/darrow.gif" alt="expand"
                                 title="Show all ${fn:length(formBean.antibodyStat.antibodyDetailedLabelings)} structures"
                                 border="0">
                            Show all</a>
                        ${fn:length(formBean.antibodyStat.antibodyDetailedLabelings)} labeled structures
                    </c:if>
                </td>
            </tr>
        </table>
    </div>
</c:if>

<div style="display:none" id="long-version">
    <c:if test="${fn:length(formBean.antibodyStat.antibodyDetailedLabelings) ne null && fn:length(formBean.antibodyStat.antibodyDetailedLabelings) > 0}">
        <table cellpadding="0" cellspacing="0" border="0" width="100%">
            <tr bgcolor="#ccccc0">
                <td><b>Anatomy</b></td>
                <td><b>Stage</b></td>
                <td><b><a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-fxassayabbrev.apg">Assay</a></b></td>
                <td><b>Gene</b></td>
                <td><b>Data</b></td>
            </tr>

            <% int alternateShadingIndx = 0; %>
            <c:forEach var="detailedLabeling" items="${formBean.antibodyStat.antibodyDetailedLabelings}"
                       varStatus="loop">
                <c:if test="${loop.index == 0 || formBean.antibodyStat.antibodyDetailedLabelings[loop.index].aoAndPostCompostTerm ne formBean.antibodyStat.antibodyDetailedLabelings[loop.index -1].aoAndPostCompostTerm}">
                    <% alternateShadingIndx++; %>
                </c:if>
                <% if (alternateShadingIndx % 2 == 1) { %>
                <tr class="odd">
                        <% } else { %>
                <tr>
                    <% } %>
                    <td>
                        <c:if test="${loop.index == 0 || formBean.antibodyStat.antibodyDetailedLabelings[loop.index].aoAndPostCompostTerm ne formBean.antibodyStat.antibodyDetailedLabelings[loop.index -1].aoAndPostCompostTerm}">
                            <zfin:link entity="${detailedLabeling.anatomyItem}"/><c:if
                                test="${detailedLabeling.cellularComponent != null}"> : <zfin:link
                                entity="${detailedLabeling.cellularComponent}"/></c:if>
                        </c:if>
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
                        <c:forEach var="gene" items="${detailedLabeling.antigenGenes}" varStatus="status">
                            <zfin:link entity="${gene}"/>
                            <c:if test="${!status.last}">
                                ,&nbsp;
                            </c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <c:if test="${detailedLabeling.numberOfFigures > 0}">
                            <c:choose>
                                <c:when test="${detailedLabeling.numberOfFigures == 1}">
                                    <a href="/<%= ZfinProperties
                                        .
                                                getWebDriver
                                                        (
                                                        )%>?MIval=aa-fxfigureview.apg&OID=${detailedLabeling.singleFigure.zdbID}">
                                    ${detailedLabeling.numberOfFiguresDisplay}
                                </c:when>
                                <c:otherwise>
                                    <a href="figure-summary?antibody.zdbID=${formBean.antibody.zdbID}&anatomyItem.zdbID=${detailedLabeling.anatomyItem.zdbID}&startStage.zdbID=${detailedLabeling.startStage.zdbID}&endStage.zdbID=${detailedLabeling.endStage.zdbID}&onlyFiguesWithImg=false">
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
                                ${detailedLabeling.singlePublication.shortAuthorList}
                            </c:if>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td>
                    <br/>&nbsp;&nbsp;
                    <a href="javascript:collapse()">
                        <img onclick="showAnatomyList('${id}', true)"
                             src="/images/up.gif" alt="expand" title="Show first 5 structures" border="0">
                        Show first</a> 5 labeled structures
                </td>
            </tr>
        </table>
    </c:if>
</div>


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