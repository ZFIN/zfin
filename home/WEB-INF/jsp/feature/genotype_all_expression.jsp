<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<div class="summary">
<div class="summaryTitle">
  All ${formBean.totalNumberOfExpressedGenes} expressed genes for:
  <zfin:link entity="${formBean.genotype}"/>
</div>

            <table width="100%" class="summary rowstripes">
                <tbody>
                <tr>
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
                </tr>
                <c:forEach var="xp" items="${formBean.expressionDisplays}" varStatus="loop">
                <zfin:alternating-tr loopName="loop"
                                     groupBeanCollection="${formBean.expressionDisplays}"
                                     groupByBean="expressedGene">
                    <td>
                        <zfin:groupByDisplay loopName="loop"
                                             groupBeanCollection="${formBean.expressionDisplays}"
                                             groupByBean="expressedGene">
                            <zfin:link entity="${xp.expressedGene}"/>
                        </zfin:groupByDisplay>             
                    </td>
                    <td>
                        <zfin2:toggledPostcomposedList expressionResults="${xp.expressionResults}" maxNumber="3"/>
                    </td>
                    <td>
                       <zfin:link entity="${xp.experiment}"/>
                    </td>
                    <td valign="top">
                       <c:choose>
                           <c:when test="${(xp.numberOfFigures >1) && !xp.experiment.standard && !xp.experiment.chemical}">
                             <a href='/action/expression/genotype-figure-summary?genoZdbID=${formBean.genotype.zdbID}&expZdbID=${xp.experiment.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                 ${xp.numberOfFigures} figures</a>
                         </c:when>
                         <c:when test="${(xp.numberOfFigures >1) && xp.experiment.standard && !xp.experiment.chemical}">
                             <a href='/action/expression/genotype-figure-summary-standard?genoZdbID=${formBean.genotype.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                 ${xp.numberOfFigures} figures</a>
                         </c:when>
                         <c:when test="${(xp.numberOfFigures >1) && !xp.experiment.standard && xp.experiment.chemical}">
                             <a href='/action/expression/genotype-figure-summary-chemical?genoZdbID=${formBean.genotype.zdbID}&geneZdbID=${xp.expressedGene.zdbID}&imagesOnly=false'>
                                 ${xp.numberOfFigures} figures</a>
                         </c:when>

                         <c:otherwise>
                           <a href='/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-fxfigureview.apg&OID=${xp.singleFigure.zdbID}'>
                             <zfin2:figureOrTextOnlyLink figure="${xp.singleFigure}" integerEntity="${xp.numberOfFigures}"/></a>                         
                         </c:otherwise>
                       </c:choose>
                       <c:if test="${xp.imgInFigure}"><img src="/images/camera_icon.gif" border="0" alt="with image">&nbsp;</c:if>from
                       <c:choose>
                            <c:when test="${xp.numberOfPublications > 1 }">
                                ${xp.numberOfPublications} publications
                            </c:when>
                            <c:otherwise>
                                <zfin:link entity="${xp.singlePublication}"/>
                            </c:otherwise>
                       </c:choose>
                    </td>

                </zfin:alternating-tr>
                </c:forEach>

                </tbody>
            </table>
</div>
