<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="__react-root" id="OrthologyTable" data-gene-id="${formBean.marker.zdbID}"></div>

<div style="display: ${hasOrthology ? 'flex' : 'none'}">
    <a href="/action/marker/${formBean.marker.zdbID}/download/orthology">
        <i class="fas fa-download"></i> Download Curated Orthology
    </a>
</div>

<div class="summary" style="display: ${orthologyNote!= null ? 'inline' : 'none'}">
    <b>Orthology Note</b>
    <div>${orthologyNote}</div>
</div>