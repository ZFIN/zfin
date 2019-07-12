<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="image" type="org.zfin.expression.Image" rtexprvalue="true" required="true" %>
<%@ attribute name="autoplayVideo" type="java.lang.Boolean" rtexprvalue="true" required="false" %>

<div style="text-align:center; max-width:100%">
    <table border=0 cellpadding=20>
        <tr>
            <td align="center" bgcolor="#000000">
                <c:if test="${!empty image.figure}">
                    <zfin-figure:placeholderImages figure="${image.figure}"/>
                </c:if>

                <c:if test="${empty image.figure || image.figure.publication.canShowImages}">
                    <zfin-figure:showSingleImage image="${image}" autoplayVideo="${autoplayVideo}"/>
                </c:if>
            </td>
        </tr>
    </table>
</div>

<table class="summary">
    <tr>
        <th>Figure Caption/Comments:</th>
    </tr>
</table>

<c:if test="${!empty image.comments}">
    <p class="fig">
        ${image.comments}
    </p>
</c:if>

<c:if test="${!empty image.figure}">
    <zfin-figure:figureLabelAndCaption figure="${image.figure}"/>
</c:if>
