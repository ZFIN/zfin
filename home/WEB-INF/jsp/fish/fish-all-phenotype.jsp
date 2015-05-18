<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.fish.presentation.FishBean" scope="request"/>

<div class="summary">
    <div class="summaryTitle">
        All ${formBean.numberOfPhenoDisplays} phenotypes for Genotype + Sequence Targeting Reagents:
        <a href="/action/fish/fish-detail/${fish.fishID}"><span
                class="name-value">${fish.name}</span></a>
    </div>
    <zfin2:all-phenotype phenotypeDisplays="${formBean.phenoDisplays}" secondColumn="condition"/>
</div>
