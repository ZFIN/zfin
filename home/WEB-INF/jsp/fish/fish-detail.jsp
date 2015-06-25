<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishBean" scope="request"/>

<authz:authorize ifAnyGranted="root">
    <zfin2:dataManager zdbID="${fish.fishID}"
                       rtype="genotype"/>
</authz:authorize>

<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${fish.name}"/>
    </tiles:insertTemplate>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th class="fish-name-label" style="vertical-align: bottom;">
            <span class="name-label">Fish name:</span>
        </th>
        <td class="fish-name-value" style="vertical-align: bottom;">
            <span class="name-value">${fish.name}</span>
        </td>
    </tr>
    <tr>
        <th>Genotype:</th>
        <td>
            <c:choose>
                <c:when test="${!empty fish.genotype}">
                    <zfin:link entity="${fish.genotype}"/>
                </c:when>
                <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
            </c:choose>

        </td>
    </tr>
    <tr>
        <th>Targeting Reagent:</th>
        <td>
            <c:choose>
                <c:when test="${!empty fish.strList}">
                    <zfin:link entity="${fish.strList}"/>
                </c:when>
                <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
            </c:choose>

        </td>
    </tr>
</table>
</p>


</p>

<c:if test="${!fishIsWildtypeWithoutReagents}">
    <div class="summary">
        <span class="summaryTitle">HUMAN DISEASE MODELED by ${fish.name}</span>
        <c:choose>
            <c:when test="${!empty diseases}">
            <table class="summary rowstripes">
                <thead>
                    <tr>
                        <th>Human Disease</th>
                        <th>Conditions</th>
                        <th>Citations</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach items="${diseases}" var="disease" varStatus="loop">
                        <zfin:alternating-tr loopName="loop" groupBeanCollection="${diseases}" groupByBean="disease.termName">
                            <td>
                                <zfin:groupByDisplay loopName="loop" groupBeanCollection="${diseases}" groupByBean="disease.termName">
                                    <zfin:link entity="${disease.disease}"/>
                                </zfin:groupByDisplay>
                            </td>
                            <td><zfin:link entity="${disease.experiment.experiment}"/></td>
                            <td>
                                <c:choose>
                                    <c:when test="${fn:length(disease.publications) == 1}">
                                        <a href="${disease.publications[0].zdbID}">(1)</a>
                                    </c:when>
                                    <c:otherwise>
                                        <a href="/action/ontology/fish-model-publication-list/${disease.disease.oboID}/${disease.experiment.zdbID}">
                                            (${fn:length(disease.publications)})
                                        </a>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </zfin:alternating-tr>
                    </c:forEach>
                </tbody>
            </table>
            </c:when>
            <c:otherwise>
                <br/><span class="no-data-tag">No data available</span>
            </c:otherwise>
        </c:choose>
    </div>


    <div class="summary">
        <b>GENE EXPRESSION</b>&nbsp;
        <small><a class='popup-link info-popup-link' href='/action/marker/note/expression'></a></small>
        <br/>
        <b>Gene expression in <zfin:name entity="${fish}"/></b>
        <c:choose>
            <c:when test="${geneCentricExpressionDataList != null }">
                <zfin2:all-expression expressionSummaryDisplay="${geneCentricExpressionDataList}"
                                      showNumberOfRecords="5" suppressMoDetails="true" queryKeyValuePair="fishID=${fish.fishID}"/>
                <c:if test="${fn:length(geneCentricExpressionDataList)> 5}">
                    <table width="100%">
                        <tr align="left">
                            <td>
                                Show all <a
                                    href="/action/fish/fish-show-all-expression/${fish.fishID}">${fn:length(geneCentricExpressionDataList)}
                                expressed genes</a>
                            </td>
                        </tr>
                    </table>
                </c:if>
            </c:when>

            <c:otherwise>
                <br/><span class="no-data-tag">No data available</span>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="summary">
        <b>PHENOTYPE</b>&nbsp;
        <small><a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a></small>
        <br/>
        <b>Phenotype in <zfin:name entity="${fish}"/></b>
        <c:choose>
            <c:when test="${fn:length(phenotypeDisplays) > 0 }">
                <zfin2:all-phenotype phenotypeDisplays="${phenotypeDisplays}" showNumberOfRecords="5"
                                     suppressMoDetails="true" secondColumn="condition"/>
                <c:if test="${fn:length(phenotypeDisplays) > 5}">
                    <table width="100%">
                        <tr align="left">
                            <td>
                                Show all <a
                                    href="/action/fish/fish-show-all-phenotypes/${fish.fishID}">${fn:length(phenotypeDisplays)}&nbsp;phenotypes</a>
                            </td>
                        </tr>
                    </table>
                </c:if>
            </c:when>

            <c:otherwise>
                <br><span class="no-data-tag">No data available</span>
            </c:otherwise>
        </c:choose>
    </div>
</c:if>
<p>
<c:choose>
    <c:when test="${totalNumberOfPublications > 0}">
        <a href='/action/fish/fish-publication-list?fishID=${fish.fishID}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${totalNumberOfPublications})
    </c:when>
    <c:otherwise>
        CITATIONS&nbsp;&nbsp;(0)
    </c:otherwise>
</c:choose>

