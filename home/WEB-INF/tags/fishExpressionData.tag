<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequenceTargetingReagentID" type="java.lang.String" required="false" %>
<%@ attribute name="fishZdbID" type="java.lang.String" required="false" %>
<%@ attribute name="expressionDisplays" type="java.util.Collection" required="false" %>
<%@ attribute name="showCondition" type="java.lang.Boolean" required="true" %>

<z:dataTable collapse="true"
             hasData="${expressionDisplays != null && fn:length(expressionDisplays) > 0 }">
    <thead>
        <tr>
            <th>Expressed Gene</th>
            <th>Structure</th>
            <th>Conditions</th>
            <th>Figures</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="xp" items="${expressionDisplays}" varStatus="loop">
            <tr>
                <td valign="top">
                    <zfin:groupByDisplay loopName="loop"
                                         groupBeanCollection="${expressionDisplays}"
                                         groupByBean="expressedGene">
                        <zfin:link entity="${xp.expressedGene}"/>
                    </zfin:groupByDisplay>
                </td>
                <td valign="top">
                    <zfin2:toggledLinkList collection="${xp.expressionResults}" maxNumber="3" commaDelimited="true"/>
                </td>
                <td valign="top">
                    <zfin:link entity="${xp.experiment}"/>
                </td>
                <td valign="top">
                    <c:choose>
                        <c:when test="${xp.numberOfFigures < 10}">
                            <c:forEach var="figsPub" items="${xp.figuresPerPub}">
                                <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                                    <a href='/${fig.zdbID}'>${fig.label}</a><c:if
                                        test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if
                                        test="${!figloop.last}">,&nbsp;</c:if>
                                </c:forEach>
                                from <zfin:link entity="${figsPub.key}"/><br/>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <c:choose>
                                <c:when test="${showCondition}">
                                    <c:if test="${!xp.experiment.standard}">
                                        <a href='/action/expression/fish-expression-figure-summary-experiment?fishZdbID=${fishZdbID}&expZdbID=${xp.experiment.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                                ${xp.numberOfFigures} figures</a>
                                    </c:if>
                                    <c:if test="${xp.experiment.standard}">
                                        <a href='/action/expression/fish-expression-figure-summary-standard?fishZdbID=${fishZdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                                ${xp.numberOfFigures} figures</a>
                                    </c:if>
                                </c:when>
                                <c:otherwise>
<%--                                    Removed the following link due to no traffic hits on it over the last 12 months at least.  Also removed controller method FishExpressionSummaryController.getSequenceTargetingReagentExpressionFigureSummary  --%>
<%--                                    <a href='/action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=${sequenceTargetingReagentID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>${xp.numberOfFigures} figures</a>--%>
                                    <span>${xp.numberOfFigures} figures</span>
                                </c:otherwise>
                            </c:choose>
                            <zfin2:showCameraIcon hasImage="${xp.imgInFigure}"/> from
                            <c:choose>
                                <c:when test="${xp.numberOfPublications > 1 }">
                                    ${xp.numberOfPublications} publications
                                </c:when>
                                <c:otherwise>
                                    <zfin:link entity="${xp.singlePublication}"/>
                                </c:otherwise>
                            </c:choose>
                        </c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>