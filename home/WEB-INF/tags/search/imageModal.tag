<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult"%>

<div class="result-thumbnail-container pull-right" style="min-width: 100px;">
    <div class="search-result-thumbnail">
        <c:forEach var="image" items="${result.images}" varStatus="loop">
            <div class="figure-gallery-result-container" data-zdb-id="${result.imageZdbIds[loop.index]}">
                <div class="figure-gallery-image-container ${loop.first ? '' : 'hidden'}">
                    <a href="#" role="button" data-toggle="modal">
                        <img src="/imageLoadUp/${result.thumbnails[loop.index]}"/>
                    </a>
                </div>
            </div>
        </c:forEach>
    </div>
</div>
