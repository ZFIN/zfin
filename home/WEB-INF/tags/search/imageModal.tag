<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult"%>

<div class="search-result-thumbnail">
    <c:forEach var="image" items="${result.images}" varStatus="loop">
        <div class="figure-gallery-result-container"
             data-result="${result.id}"
             data-zdb-id="${result.imageZdbIds[loop.index]}"
             data-category="${result.category}">
            <div class="figure-gallery-image-container ${loop.first ? '' : 'hidden'}">
                <a href="#" role="button" data-toggle="modal">
                    <img src="/imageLoadUp/${result.thumbnails[loop.index]}"/>
                    <div class="hidden figure-gallery-loading-overlay">
                        <i class="fa fa-spinner fa-spin"></i>
                    </div>
                </a>
            </div>
        </div>
    </c:forEach>
</div>
