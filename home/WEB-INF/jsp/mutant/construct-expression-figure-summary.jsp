<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.mutant.presentation.ConstructSearchFormBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td width="100%" class="titlebar">
            <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
            Construct Expression Summary
                </span>

        <span style="float: right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Expression summary from Constructs search"/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>

<%--<zfin2:constructExpressionSummaryCriteria criteria="${formBean.constructSearchCriteria}"/>--%>

<table class="primary-entity-attributes">
    <tr>
       <c:if test="${!empty formBean.constructObj}">
        <th>Construct:</th>
        <td>
            <zfin:link entity="${formBean.constructObj}"/>
        </td>
 </c:if>
    </tr>
    <tr>
<c:if test="${!empty formBean.expressedGene}">
        <th>Expressed Reporter:</th>
        <td>
            <zfin:link entity="${formBean.expressedGene}"/>


        </td>
</c:if>
    </tr>
    <tr>
        <th>Conditions:</th>
        <td>
            standard or control


        </td>
    </tr>

    <%--<c:if test="${!empty phenotypeSummaryCriteria.searchCriteriaPhenotype}">
        <tr>
            <th>Matching Terms:</th>
            <td>
                <c:forEach var="term" items="${phenotypeSummaryCriteria.searchCriteriaPhenotype}" varStatus="index">
                    ${term.name}<c:if test="${!index.last}">,</c:if>
                </c:forEach>
            </td>
        </tr>
    </c:if>--%>
</table>
<p/>

<p/>

<div class="summary">
<span class="summaryTitle">Expression Summary</span>

<%-- todo: need a class name for this --%>
(${formBean.constructService.numberOfFiguresDisplay} from ${formBean.constructService.numberOfPublicationsDisplay})

<zfin2:figureSummary figureSummaryList="${formBean.constructService.figureSummary}" showMarker="false" showGenotype="true"
                     expressionGenotypeData="true"/>
</div>

