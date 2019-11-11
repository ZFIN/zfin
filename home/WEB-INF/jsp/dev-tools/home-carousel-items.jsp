<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<dl class="row">
    <dt class="col-md-2 text-md-right">Total items:</dt>
    <dd class="col-md-10 mb-md-0">${fn:length(carouselImages)}</dd>

    <dt class="col-md-2 text-md-right">Images:</dt>
    <dd class="col-md-10 mb-md-0">
        <ul class="list-unstyled">
            <c:forEach items="${carouselImages}" var="image">
                <li><a href="/${image.zdbID}">${image.zdbID}</a> from <zfin:link entity="${image.figure}" /> of <zfin:link entity="${image.figure.publication}"/></li>
            </c:forEach>
        </ul>

    </dd>
</dl>