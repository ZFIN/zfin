<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.feature.presentation.GenotypeBean" scope="request"/>

<div class="summary">
    <div class="summaryTitle">
        All ${formBean.numberOfPhenoDisplays} phenotypes for:
        <zfin:link entity="${formBean.genotype}"/>
    </div>
    <zfin2:all-phenotype phenotypeDisplays="${formBean.phenoDisplays}" showNumberOfRecords="10000"/>
</div>
