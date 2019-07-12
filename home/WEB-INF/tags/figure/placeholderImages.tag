<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="figure" type="org.zfin.expression.Figure" rtexprvalue="true" required="true" %>

<c:choose>
    <c:when test="${figure.type == 'TOD'}">
        <img class="figure-image placeholder" src="/images/imagenotavailable.gif"/>
    </c:when>

    <c:when test="${!figure.publication.canShowImages}">
        <img class="figure-image placeholder" src="/images/onlyfrompublisher.jpg">
    </c:when>

    <c:when test="${figure.publication.canShowImages && empty figure.images}">
        <img class="figure-image placeholder" src="/images/imagenotavailable.gif"/>
    </c:when>
</c:choose>

