<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figure" type="org.zfin.expression.Figure" rtexprvalue="true" required="true"  %>
<%@ attribute name="autoplayVideo" type="java.lang.Boolean" rtexprvalue="true" required="false" %>
<%@ attribute name="showMultipleMediumSizedImages" type="java.lang.Boolean" rtexprvalue="true" required="true" %>

<table class="figure-image-and-caption">
    <tr>
        <td>
            <zfin-figure:placeholderImages figure="${figure}" />

            <%--has permission to show, single image, can be video--%>
            <c:if test="${figure.publication.canShowImages && !empty figure.images && (fn:length(figure.images) == 1 || showMultipleMediumSizedImages) }">
                <div style="max-width: 510px;"> <%-- this div causes multiple medium sized images to stack on top of one another, ZDB-PUB-990628-12 has an example--%>
                    <c:forEach var="image" items="${figure.images}">
                        <zfin-figure:showSingleImage image="${image}" medium="true" autoplayVideo="${autoplayVideo}" />
                    </c:forEach>
                </div>
            </c:if>

            <%--has permission to show, multiple images, should show them as thumbnails --%>
            <c:if test="${figure.publication.canShowImages && !empty figure.images && fn:length(figure.images) > 1 && !showMultipleMediumSizedImages}">
                <c:forEach var="image" items="${figure.images}">
                    <zfin:link entity="${image}"/>
                </c:forEach>
            </c:if>

            <zfin-figure:figureLabelAndCaption figure="${figure}" />

            <%-- on all figure view, we want to also show some data tables, so they'll be passed in as the
             'body' of this tag --%>
            <div style="padding-top: 1em; clear: both">
                <jsp:doBody/>
            </div>
        </td>
    </tr>
</table>