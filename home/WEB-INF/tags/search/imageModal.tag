<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="result" type="org.zfin.search.presentation.SearchResult"%>


<c:set var="modalDomID" value="${zfn:generateRandomDomID()}-gallery-modal"/>
<c:set var="carouselDomID" value="${zfn:generateRandomDomID()}-gallery-carousel"/>

<div class="result-thumbnail-container pull-right" style="min-width: 100px;">
    <div class="search-result-thumbnail">
        <a href="#${modalDomID}" id="${modalDomID}-link" role="button" data-toggle="modal">
           <img src="/imageLoadUp/${result.thumbnail}"/>
        </a>
    </div>
</div>
<div id="${modalDomID}" class="modal fade image-modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <span class="result-header search-result-name">${result.link}</span>
            </div>
            <div class="modal-body">

                <c:choose>
                    <c:when test="${fn:length(result.images) == 1}">
                        <img class="img-responsive" src="/imageLoadUp/medium/${result.image}"/>
                    </c:when>
                    <c:when test="${fn:length(result.images) > 1}">
                        <div id="${carouselDomID}" class="carousel slide" data-interval="">
                                <%--these are working now, but do we want them...? --%>
                                 <ol class="carousel-indicators">
                                    <c:forEach var="image" items="${result.images}" varStatus="loop">
                                        <li <c:if test="${loop.index == 0}">class="active"</c:if>
                                            data-target="#${carouselDomID}"
                                            data-slide-to="${loop.index}">
                                        </li>
                                    </c:forEach>
                                </ol>
                            <!-- Carousel items -->
                            <div class="carousel-inner">
                                <c:forEach var="image" items="${result.images}" varStatus="loop">
                                    <div class="<c:if test="${loop.index == 0}">active</c:if> item">
                                        <img src="/images/ajax-loader.gif" class="img-responsive" realsrc="/imageLoadUp/medium/${image}"/>
                                    </div>
                                </c:forEach>
                            </div>
                            <!-- Carousel nav, override 'top' to not be vertically centered, but stick to the top  -->
                            <a style="top: 30px;" class="carousel-control left" href="#${carouselDomID}" data-slide="prev">&lsaquo;</a>
                            <a style="top: 30px;" class="carousel-control right" href="#${carouselDomID}" data-slide="next">&rsaquo;</a>
                        </div>
                    </c:when>
                </c:choose>

            </div>
            <div class="modal-footer">
                <a href="#" data-dismiss="modal" class="btn">Close</a>
            </div>
        </div>
    </div>

</div>
<script>
    jQuery("#${modalDomID}-link").click( function() {
        jQuery("#${modalDomID}").find("div.item img").each(function() {
            $this = jQuery(this);
            $this.attr('src',$this.attr('realsrc'));

        });
    });

</script>