<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figure" type="org.zfin.expression.Figure" rtexprvalue="true" required="true" %>
<%@ attribute name="autoplayVideo" type="java.lang.Boolean" rtexprvalue="true" required="false" %>
<%@ attribute name="showCaption" type="java.lang.Boolean" rtexprvalue="true" required="false" %>
<%@ attribute name="showMultipleMediumSizedImages" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<c:set var="showCaption" value="${!showCaption ? false : true }"/>
<table class="figure-image-and-caption hover-shadow">
    <tr>
        <td>
            <zfin-figure:placeholderImages figure="${figure}"/>

            <%--has permission to show, single image, can be video--%>
            <!-- before branch A -->
            <c:choose>
            <c:when test="${figure.publication.canShowImages && !empty figure.images && (fn:length(figure.images) == 1 || showMultipleMediumSizedImages) }">
                <c:choose>
                    <c:when test="${fn:length(figure.images) > 1}">
                        <%--CAPTION--%>
                        <c:if test="${showCaption}">
                            <!-- show caption -->
                            <zfin-figure:figureLabelAndCaption figure="${figure}" hideLabel="true"/>
                        </c:if>
                        <div class="multiple-medium-images">
                            <c:forEach var="image" items="${figure.images}">
                                <!-- single image -->
                                <zfin-figure:showSingleImage image="${image}" medium="true" autoplayVideo="${autoplayVideo}"/>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="single-image" style="max-width: 510px;">
                            <c:forEach var="image" items="${figure.images}">
                                <!-- should only be a single image -->
                                <zfin-figure:showSingleImage image="${image}" medium="true" autoplayVideo="${autoplayVideo}"/>
                            </c:forEach>
                        </div>

                        <%--CAPTION--%>
                        <c:if test="${showCaption}">
                            <!-- show caption -->
                            <zfin-figure:figureLabelAndCaption figure="${figure}" hideLabel="true"/>
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:when test="${figure.publication.canShowImages && !empty figure.images && fn:length(figure.images) > 1 && !showMultipleMediumSizedImages}">
                <!-- branch B -->
                <c:forEach var="image" items="${figure.images}">
                    <!-- multiple images, show as thumbnails -->
                    <zfin:link entity="${image}"/>
                </c:forEach>

                <%--CAPTION--%>
                <c:if test="${showCaption}">
                    <!-- show caption -->
                    <zfin-figure:figureLabelAndCaption figure="${figure}"/>
                </c:if>

            </c:when>
            <c:otherwise>
                <%--CAPTION--%>
                <c:if test="${showCaption}">
                    <!-- show caption -->
                    <zfin-figure:figureLabelAndCaption figure="${figure}" hideLabel="true"/>
                </c:if>
            </c:otherwise>

            </c:choose>

            <%-- on all figure view, we want to also show some data tables, so they'll be passed in as the
             'body' of this tag --%>
            <div style="padding-top: 1em; clear: both">
                <jsp:doBody/>
            </div>
        </td>
    </tr>
</table>