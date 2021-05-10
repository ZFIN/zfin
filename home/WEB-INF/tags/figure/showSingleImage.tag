<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="image" type="org.zfin.expression.Image" rtexprvalue="true" required="true"  %>
<%@ attribute name="autoplayVideo" type="java.lang.Boolean" rtexprvalue="true" required="false" %>
<%@ attribute name="medium" type="java.lang.Boolean" rtexprvalue="true" required="false" %>

<c:set var="autoplay" value=""/>
<c:if test="${autoplayVideo}">
    <c:set var="autoplay" value="autoplay"/>
</c:if>

<c:set var="filename" value="" />
<c:choose>
    <c:when test="${medium}">
        <c:set var="filename"
               value="${!empty image.imageWithAnnotationMediumFilename ? image.imageWithAnnotationMediumFilename : image.medium}" />
    </c:when>
    <c:otherwise>
        <c:set var="filename"
               value="${!empty image.imageWithAnnotationsFilename ? image.imageWithAnnotationsFilename : image.imageFilename}" />
    </c:otherwise>
</c:choose>

<c:choose>
    <c:when test="${image.videoStill}">
        <video controls ${autoplay} loop height="500" poster="/imageLoadUp/${filename}">
            <c:forEach var="video" items="${image.videos}">
                <source src="/videoLoadUp/${video.videoFilename}"/>
            </c:forEach>
            This browser does not support embedded videos.
        </video>
    </c:when>
    <c:otherwise>
        <zfin:link entity="${image}">
            <img class="figure-image ${medium ? 'medium' : ''}" src="/imageLoadUp/${filename}"/>
        </zfin:link>
    </c:otherwise>
</c:choose>
