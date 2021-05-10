<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="bean" type="org.zfin.figure.presentation.FigureGalleryImagePresentation" scope="request"/>

<div class="modal-content">
    <div class="modal-header">
        <div>
            <h4 class="modal-title">
                <c:if test="${!empty bean.figureExpressionSummary || !empty bean.figurePhenotypeSummary || !empty bean.details}">
                    <a role="button" class="figure-gallery-modal-collapse icon-toggle open"><span aria-hidden="true"><i class="fas fa-fw fa-chevron-right"></i></span></a>
                </c:if>
                <zfin:link entity="${bean.titleLinkEntity}"/>
            </h4>
            <div class="figure-gallery-modal-details">
                <c:if test="${!empty bean.figureExpressionSummary}">
                    <zfin-figure:expressionSummary summary="${bean.figureExpressionSummary}"/>
                </c:if>
                <c:if test="${!empty bean.figurePhenotypeSummary}">
                    <zfin-figure:phenotypeSummary summary="${bean.figurePhenotypeSummary}"/>
                </c:if>
                <c:if test="${!empty bean.details}">
                    <div class="summary">${bean.details}</div>
                </c:if>
            </div>
        </div>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
            <span aria-hidden="true"><i class="fas fa-fw fa-times"></i></span>
        </button>
    </div>
    <div class="modal-body figure-gallery-modal-body">
        <a href="#" class="figure-gallery-modal-nav prev" role="button">
            <i class="fas fa-chevron-left"></i>
        </a>
        <zfin:link entity="${bean.imageLinkEntity}">
            <img class="figure-gallery-modal-image" src="${bean.image.url}">
        </zfin:link>
        <a href="#" class="figure-gallery-modal-nav next" role="button">
            <i class="fas fa-chevron-right"></i>
        </a>
        <div class="figure-gallery-modal-loader d-none">
            <i class="fas fa-spinner fa-spin"></i>
        </div>
    </div>
</div>
