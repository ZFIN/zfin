<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishBean" scope="request"/>

<div class="summary">
    <div class="summaryTitle">
        All ${formBean.numberOfPhenoDisplays} phenotypes for Genotype + Morpholinos:
        <a href="/action/fish/fish-detail/${formBean.fish.fishID}"><span
                class="name-value">${formBean.fish.name}</span></a>
    </div>
    <zfin2:all-phenotype phenotypeDisplays="${formBean.phenoDisplays}" showNumberOfRecords="10000"/>
</div>
