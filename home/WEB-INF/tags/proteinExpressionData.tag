<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="fishZdbID" type="java.lang.String" required="false" %>
<%@ attribute name="proteinExpressionDisplays" type="java.util.Collection" required="false" %>

<table width="100%" class="summary rowstripes">
    <thead>
    <tr>
        <th width="12%">
            Antibody
        </th>
        <th width="12%">
            Antigen Genes
        </th>
        <th width="30%">
            Structure
        </th>
        <th width="14%">
            Conditions
        </th>
        <th width="32%">
            Figures
        </th>
    </tr>
    </thead>
    <c:forEach var="xp" items="${proteinExpressionDisplays}" varStatus="loop">
        <zfin:alternating-tr loopName="loop"
                             groupBeanCollection="${proteinExpressionDisplays}"
                             groupByBean="antiGene">
            <td valign="top">
                    <zfin:link entity="${xp.antibody}"/>
            </td>
            <td valign="top">
                <zfin:groupByDisplay loopName="loop"
                                     groupBeanCollection="${proteinExpressionDisplays}"
                                     groupByBean="antiGene">
                    <zfin:link entity="${xp.antiGene}"/>
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
                                <a href='/${fig.zdbID}'>${fig.label}</a><c:if test="${!fig.imgless}">&nbsp;<img src="/images/camera_icon.gif" alt="with image" image="" border="0"></c:if><c:if test="${!figloop.last}">,&nbsp;</c:if>
                            </c:forEach>
                            from <zfin:link entity="${figsPub.key}"/><br/>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <c:if test="${!xp.experiment.standard}">
                            <a href='/action/expression/fish-expression-figure-summary-experiment?fishZdbID=${fishZdbID}&expZdbID=${xp.experiment.zdbID}&geneZdbID=${xp.antiGene.zdbID}&imagesOnly=false'>
                                    ${xp.numberOfFigures} figures</a>
                        </c:if>
                        <c:if test="${xp.experiment.standard}">
                            <a href='/action/expression/fish-expression-figure-summary-standard?fishZdbID=${fishZdbID}&geneZdbID=${xp.antiGene.zdbID}&imagesOnly=false'>
                                    ${xp.numberOfFigures} figures</a>
                        </c:if>
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
