<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="modal-content">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">${image.figure.label} from <zfin:link entity="${image.figure.publication}"/></h4>
        <c:if test="${!empty expressionSummary}">
            <zfin-figure:expressionSummary summary="${expressionSummary}"/>
        </c:if>
        <c:if test="${!empty phenotypeSummary}">
            <zfin-figure:phenotypeSummary summary="${phenotypeSummary}"/>
        </c:if>
    </div>
    <div class="modal-body figure-gallery-modal-body">
        <a href="#" class="figure-gallery-modal-nav prev" role="button">
            <i class="fa fa-chevron-left"></i>
        </a>
        <a href="/${image.figure.zdbID}">
            <img class="figure-gallery-modal-image" src="${image.url}">
        </a>
        <a href="#" class="figure-gallery-modal-nav next" role="button">
            <i class="fa fa-chevron-right"></i>
        </a>
    </div>
</div>
