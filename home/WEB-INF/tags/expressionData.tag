<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequenceTargetingReagentID" type="java.lang.String" required="false" %>
<%@ attribute name="fishZdbID" type="java.lang.String" required="false" %>
<%@ attribute name="expressionDisplays" type="java.util.Collection" required="false" %>
<%@ attribute name="showCondition" type="java.lang.Boolean" required="true" %>

<table width="100%" class="summary rowstripes">
    <thead>
    <tr>
        <c:choose>
            <c:when test="${showCondition}">
                <th width="16%">
                    Expressed Gene
                </th>
                <th width="32%">
                    Structure
                </th>
                <th width="17%">
                    Conditions
                </th>
                <th width="35%">
                    Figures
                </th>
            </c:when>
            <c:otherwise>
                <th width="23%">
                    Expressed Gene
                </th>
                <th width="37%">
                    Anatomy
                </th>
                <th width="40%">
                    Figures
                </th>
            </c:otherwise>
        </c:choose>
    </tr>
    </thead>
    <c:forEach var="xp" items="${expressionDisplays}" varStatus="loop">
        <zfin:alternating-tr loopName="loop"
                             groupBeanCollection="${expressionDisplays}"
                             groupByBean="expressedGene">
            <td valign="top">
                <zfin:groupByDisplay loopName="loop"
                                     groupBeanCollection="${expressionDisplays}"
                                     groupByBean="expressedGene">
                    <zfin:link entity="${xp.expressedGene}"/>
                </zfin:groupByDisplay>
            </td>
            <td valign="top">
                <zfin2:toggledPostcomposedList entities="${xp.expressionResults}" maxNumber="3"
                                               id="${xp.expressedGene.zdbID}" numberOfEntities="${fn:length(xp.expressionResults)}"/>
            </td>
            <c:if test="${showCondition}">
                <td valign="top">
                    <zfin:link entity="${xp.experiment}"/>
                </td>
            </c:if>
            <td valign="top">
                <c:choose>
                    <c:when test="${xp.numberOfFigures < 10}">
                        <c:forEach var="figsPub" items="${xp.figuresPerPub}">
                            <c:forEach var="fig" items="${figsPub.value}" varStatus="figloop">
                                <a href='/${fig.zdbID}'>${fig.label}</a><c:if test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if test="${!figloop.last}">,&nbsp;</c:if>
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
                                <a href='/action/expression/sequence-targeting-reagent-expression-figure-summary?strZdbID=${sequenceTargetingReagentID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                        ${xp.numberOfFigures} figures</a>
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
        </zfin:alternating-tr>
    </c:forEach>
</table>
