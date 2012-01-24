<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.anatomy.presentation.ExpressionPhenotypeReportBean" scope="request"/>

<table width="100%" cellpadding="0" cellspacing="0">
    <tr>
        <td colspan="6" align="right"><a href="#modify-search">Modify Search</a></td>
    </tr>
    <tr>
        <td>
            <div align="center">
                <c:choose>
                    <c:when test="${formBean.totalRecords == 0}">
                        <b>No Expression records were found matching your query.</b><br><br>
                    </c:when>
                    <c:otherwise>
                        <b>
                            <zfin:choice choicePattern="0#Expressions| 1#Expression| 2#Expressions"
                                         integerEntity="${formBean.totalRecords}" includeNumber="true"/>
                        </b>
                    </c:otherwise>
                </c:choose>
            </div>
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
    </tr>
</table>

<c:if test="${formBean.totalRecords > 0}">
    <table class="searchresults rowstripes">
        <tr>
            <th width=5%>Gene</th>
            <th width=5%>Antibody</th>
            <th width=20%>Fish</th>
            <th width=10%>Environment</th>
            <th width=10%>Assay</th>
            <th width=30%>Structure</th>
            <th width=15%>Start Stage</th>
            <th width=15%>End Stage</th>
        </tr>
        <c:forEach var="phenotype" items="${formBean.allExpressions}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <zfin:link entity="${phenotype.expressionExperiment.gene}"/>
                </td>
                <td>
                    <zfin:link entity="${phenotype.expressionExperiment.antibody}"/>
                </td>
                <td>
                        ${phenotype.expressionExperiment.genotypeExperiment.genotype.name}
                </td>
                <td>
                        ${phenotype.expressionExperiment.genotypeExperiment.experiment.name}
                </td>
                <td>
                        ${phenotype.expressionExperiment.assay.abbreviation}
                </td>
                <td>
                    <zfin:link entity="${phenotype}"/>
                </td>
                <td>
                        ${phenotype.startStage.abbreviation}
                </td>
                <td>
                        ${phenotype.endStage.abbreviation}
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

    <input name="page" type="hidden" value="1" id="page"/>
    <zfin2:pagination paginationBean="${formBean}"/>
</c:if>

<p></p>
<table width="100%">
    <tr>
        <td class="titlebar">
                <span style="font-size: larger; margin-left: 0.5em; font-weight: bold;">
                        <a name="modify-search">Modify your search </a>
            </span>
            &nbsp;&nbsp; <a href="javascript:start_tips();">Search Tips</a>
        </td>
    </tr>
</table>

<table width="100%" class="error-box">
    <tr>
        <td>
            <form:errors path="*" cssClass="Error"/>
        </td>
    </tr>
</table>

<zfin-ontology:expression-report-form formBean="${formBean}"/>