<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<div class="titlebar">
    <h1>Phenotype Summary</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Phenotype summary from genotype"/>
        </tiles:insertTemplate>
    </span>
</div>

<table class="primary-entity-attributes">
    <tr>
        <th>Genotype:</th>
        <td>
            <zfin:link entity="${formBean.genotype}"/>
        </td>
    </tr>
</table>
<p/>

<div class="summary">
    <span class="summaryTitle">Phenotypes</span>
    (<zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                  collectionEntity="${figureSummaryDisplay}"/>)

    <zfin2:figurePhenotypeSummary figureSummaryDisplayList="${figureSummaryDisplay}" />
</div>





