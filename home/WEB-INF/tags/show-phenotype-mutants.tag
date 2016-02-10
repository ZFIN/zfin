<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.anatomy.presentation.AnatomySearchBean" required="true" %>
<%@ attribute name="includingSubstructures" type="java.lang.Boolean" required="false" %>

<div class="summary">
    <div class="summaryTitle">Phenotypes in <i><zfin:name entity="${formBean.aoTerm}"/></i> caused by Genes</div>

    <c:if test="${formBean.mutantsExist}">
        <table class="summary rowstripes">
            <tbody>
            <tr>
                <th width="15%">
                    Affected Gene
                </th>
                <th width="15%">
                    Fish
                </th>
                <th width="50%">
                    Phenotype
                </th>
                <th width="20%">
                    Figures
                </th>
            </tr>
            <c:forEach var="genoStat" items="${formBean.genotypeStatistics}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        <zfin:link entity="${genoStat.affectedMarkers}"/>
                    </td>
                    <td>
                        <zfin:link entity="${genoStat.fish}"/>
                    </td>
                    <td>
                        <c:forEach var="statement" items="${genoStat.phenotypeObserved}" varStatus="loop">
                            <zfin:link entity="${statement}"/> <c:if test="${!loop.last}"><br/></c:if>
                        </c:forEach>
                    </td>
                    <td>
                        <c:if test="${genoStat.numberOfFigures > 0}">
                            <c:if test="${genoStat.numberOfFigures > 1}">
                                <a href="/action/ontology/${formBean.aoTerm.oboID}/phenotype-summary/${genoStat.fish.zdbID}">
                                    <zfin:choice choicePattern="0#figures| 1#figure| 2#figures"
                                                 integerEntity="${genoStat.numberOfFigures}" includeNumber="true"/></a>
                            </c:if>
                            <c:if test="${genoStat.numberOfFigures == 1 }">
                                <a href="/${genoStat.figure.zdbID}">
                                    <zfin2:figureOrTextOnlyLink figure="${genoStat.figure}"
                                                                integerEntity="${genoStat.numberOfFigures}"/>
                                </a>
                            </c:if>
                        </c:if>
                        <c:if test="${genoStat.numberOfFigures == 0}">
                            --
                        </c:if>
                        <zfin2:showCameraIcon hasImage="${genoStat.imgInFigure}"/> from
                        <c:if test="${genoStat.publicationSet.size() ==1 }">
                            <zfin:link entity="${genoStat.publicationSet.iterator().next()}"/>
                        </c:if>
                        <c:if test="${genoStat.publicationSet.size() > 1}">
                            <zfin:choice choicePattern="0#publications| 1#publication| 2#publications"
                                         collectionEntity="${genoStat.publicationSet}"
                                         includeNumber="true"/>
                        </c:if>
                        <c:if test="${genoStat.numberOfFigures == 0}">
                            --
                        </c:if>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
            </tbody>
        </table>
    </c:if>
</div>