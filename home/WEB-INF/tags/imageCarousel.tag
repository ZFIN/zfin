<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="id" type="java.lang.String" required="true" %>
<%@ attribute name="images" type="java.util.List" required="true" %>
<%@ attribute name="captions" type="java.util.List" %>
<%@ attribute name="interval" type="java.lang.String" %>

<c:set var="interval" value="${(empty interval) ? '5000' : interval}" />

<div id="${id}" class="carousel carousel-wrapper slide" data-ride="carousel" data-interval="${interval}">

    <div class="carousel-inner" role="listbox">
        <c:forEach items="${images}" var="image" varStatus="status">
            <a href="/${image.figure.zdbID}" class="carousel-item ${status.first ? 'active' : ''}" style="background-image: url('@IMAGE_LOAD@/${image.imageFilename}')">
                <div class="carousel-caption">
                    <div><b>${image.figure.label} of ${image.figure.publication.shortAuthorList}</b></div>
                    <div class="carousel-figure-caption">${captions[status.index]}</div>
                </div>
            </a>
        </c:forEach>
    </div>

    <div class="carousel-controls">
        <a href="#${id}" role="button" data-slide="prev">
            <i class="fas fa-chevron-left"></i>
            <span class="sr-only">Previous</span>
        </a>

        <ol class="carousel-indicators">
            <c:forEach items="${images}" varStatus="status">
                <li data-target="#${id}" data-slide-to="${status.index}" class="${status.first ? 'active' : ''}"></li>
            </c:forEach>
        </ol>

        <a href="#${id}" role="button" data-slide="next">
            <i class="fas fa-chevron-right"></i>
            <span class="sr-only">Next</span>
        </a>
    </div>
</div>