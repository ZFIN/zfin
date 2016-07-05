<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="modal-content">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true"><i class="fa fa-fw fa-close"></i></span>
        </button>
        <button type="button" class="close figure-gallery-modal-collapse open">
            <span aria-hidden="true"><i class="fa fa-fw fa-chevron-up"></i></span>
        </button>
        <h4 class="modal-title"><zfin:link entity="${image.figure}">${image.figure.label}</zfin:link> from <zfin:link entity="${image.figure.publication}"/></h4>
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
        <zfin:link entity="${image.figure}">
            <img class="figure-gallery-modal-image" src="${image.url}">
        </zfin:link>
        <a href="#" class="figure-gallery-modal-nav next" role="button">
            <i class="fa fa-chevron-right"></i>
        </a>
        <div class="figure-gallery-modal-loader hidden">
            <i class="fa fa-spinner fa-spin"></i>
        </div>
    </div>
</div>
