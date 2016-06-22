<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="modal-dialog">
    <div class="modal-content">
        <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <h4 class="modal-title">${image.figure.label} from <zfin:link entity="${image.figure.publication}"/></h4>
            <zfin-figure:expressionSummary summary="${expressionSummary}"/>
        </div>
        <div class="modal-body figure-gallery-modal-body">
            <a href="/${image.figure.zdbID}">
                <img class="figure-gallery-modal-image" src="${image.url}">
            </a>
        </div>
    </div>
</div>

