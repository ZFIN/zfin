<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="__react-root" id="OrthologyTable" data-gene-id="${formBean.marker.zdbID}"></div>

<c:if test="${!empty orthologyNote}">
    <div>
        <b>Orthology Note</b>
        <div>${orthologyNote}</div>
    </div>
</c:if>
