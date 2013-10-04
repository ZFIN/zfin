<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishBean" scope="request"/>

<authz:authorize ifAnyGranted="root">
    <zfin2:dataManager zdbID="${formBean.fish.fishID}"
                       rtype="genotype"/>
</authz:authorize>

<div style="float: right;">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.fish.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.fish.ID}"/>
    </tiles:insertTemplate>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th class="fish-name-label" style="vertical-align: bottom;">
            <span class="name-label">Genotype + Reagents:</span>
        </th>
        <td class="fish-name-value" style="vertical-align: bottom;">
            <span class="name-value">${formBean.fish.name}</span>
        </td>
    </tr>
</table>
</p>

<b>FISH COMPOSITION</b>
<table class="summary rowstripes">
    <tr>
        <th width="20%">Genotype</th>
        <th>Affected Gene</th>
        <th>Construct</th>
        <th>Parental Zygosity</th>
    </tr>
    <c:choose>
        <c:when test="${fn:length(formBean.genotype.genotypeFeatures) > 0}">
            <c:forEach var="genotypeFeature" items="${formBean.genotype.genotypeFeatures}">
                <jsp:useBean id="genotypeFeature" class="org.zfin.mutant.GenotypeFeature" scope="request"/>
                <tr>
                    <td style="vertical-align: bottom;"><zfin:link entity="${formBean.genotype}"/>   <a class="popup-link data-popup-link" href="/action/genotype/genotype-detail-popup?zdbID=${formBean.genotype.zdbID}"></a></td>

                    <td style="vertical-align: bottom;">
                        <zfin2:listOfAffectedGenes markerCollection="${genotypeFeature.feature.affectedGenes}"/>
                    </td>
                    <td style="vertical-align: bottom;"><zfin2:listOfTgConstructs markerCollection="${genotypeFeature.feature.tgConstructs}"/></td>

                    <td style="vertical-align: bottom;">
                            ${genotypeFeature.parentalZygosityDisplay}
                    </td>
                </tr>
            </c:forEach>
        </c:when>
        <c:otherwise>
            <tr>
                <td><zfin:link entity="${formBean.genotype}"/></td>
                <td>
                </td>
                <td>
                </td>
            </tr>
        </c:otherwise>
    </c:choose>
</table>
</p>
<table class="summary rowstripes">
    <tr>
        <th width="20%">Reagent</th>
        <th>Targeted Gene</th>
    </tr>
    <c:if test="${fn:length(formBean.fish.morpholinos) ne null && fn:length(formBean.fish.morpholinos) > 0}">
        <c:forEach var="morpholino" items="${formBean.sequenceTargetingReagents}" varStatus="loop">
            <jsp:useBean id="morpholino" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>
            <tr>
                <td><zfin:link entity="${morpholino}"/></td>
                <td>
                    <zfin2:listOfAffectedGenes markerCollection="${morpholino.targetGenes}"/>
                </td>
            </tr>
        </c:forEach>
    </c:if>
</table>
</p>

<div class="summary">
    <b>GENE EXPRSSION</b>&nbsp;
    <small><a class='popup-link info-popup-link' href='/action/marker/note/expression'></a></small>
    <br/>
    <b>Gene expression in <zfin:name entity="${formBean.fish}"/></b>
    <c:choose>
        <c:when test="${geneCentricExpressionDataList != null }">
            <zfin2:all-expression expressionSummaryDisplay="${geneCentricExpressionDataList}"
                                  showNumberOfRecords="5" suppressMoDetails="true" queryKeyValuePair="fishID=${formBean.fish.fishID}"/>
            <c:if test="${fn:length(geneCentricExpressionDataList)> 5}">
                <table width="100%">
                    <tr align="left">
                        <td>
                            Show all <a
                                href="/action/fish/fish-show-all-expression/${formBean.fish.fishID}">${fn:length(geneCentricExpressionDataList)}
                            expressed genes</a>
                        </td>
                    </tr>
                </table>
            </c:if>
        </c:when>

        <c:otherwise>
            <br/>No data available
        </c:otherwise>
    </c:choose>
</div>

<div class="summary">
    <b>PHENOTYPE</b>&nbsp;
    <small><a class='popup-link info-popup-link' href='/action/marker/note/phenotype'></a></small>
    <br/>
    <b>Phenotype in <zfin:name entity="${formBean.fish}"/></b>
    <c:choose>
        <c:when test="${formBean.numberOfPhenoDisplays > 0 }">
            <zfin2:all-phenotype phenotypeDisplays="${formBean.phenoDisplays}" showNumberOfRecords="5"
                                 suppressMoDetails="true"/>
            <c:if test="${formBean.numberOfPhenoDisplays > 5}">
                <table width="100%">
                    <tr align="left">
                        <td>
                            Show all <a
                                href="/action/fish/fish-show-all-phenotypes/${formBean.fish.fishID}">${formBean.numberOfPhenoDisplays}&nbsp;phenotypes</a>
                        </td>
                    </tr>
                </table>
            </c:if>
        </c:when>

        <c:otherwise>
            <br>No data available</br>
        </c:otherwise>
    </c:choose>
</div>

<p>
    <a href='/action/fish/fish-publication-list?fishID=${formBean.fish.fishID}'><b>CITATIONS</b></a>&nbsp;&nbsp;(${formBean.totalNumberOfPublications})
