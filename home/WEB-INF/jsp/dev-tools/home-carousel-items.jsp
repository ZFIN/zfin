<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<dl class="dl-horizontal">
    <dt>Total items:</dt>
    <dd>${fn:length(carouselImages)}</dd>

    <dt>Images:</dt>
    <dd>
        <ul class="list-unstyled">
            <c:forEach items="${carouselImages}" var="image">
                <li><a href="/${image.zdbID}">${image.zdbID}</a> from <zfin:link entity="${image.figure}" /> of <zfin:link entity="${image.figure.publication}"/></li>
            </c:forEach>
        </ul>

    </dd>
</dl>