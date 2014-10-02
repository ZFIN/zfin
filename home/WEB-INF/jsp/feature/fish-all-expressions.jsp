<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td width="100%" class="titlebar">
            <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
            Expression Summary
                </span>

        <span style="float: right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="Expression Summary from Fish search"/>
            </tiles:insertTemplate>
        </td>
    </tr>
</table>

<table class="primary-entity-attributes">
    <tr>
        <th>Genotype:</th>
        <td>
            <zfin:link entity="${formBean.genotype}"/>
        </td>
    </tr>
    <c:if test="${!empty morpholinos}">
        <tr>
            <th>Morpholinos:</th>
            <td>
                <c:forEach var="morpholino" items="${morpholinos}" varStatus="index">
                    <zfin:link entity="${morpholino}"/><c:if test="${!index.last}">, </c:if>

                </c:forEach>
            </td>
        </tr>
    </c:if>
</table>
<p/>

<div class="summary">
    <div class="summaryTitle">
        All ${fn:length(geneCentricExpressionDataList)} expressed genes for:
        <zfin:link entity="${fish}"/>
    </div>

    <zfin2:all-expression expressionSummaryDisplay="${geneCentricExpressionDataList}"
                          queryKeyValuePair="fishID=${fish.fishID}"
                          suppressMoDetails="true"/>
</div>
