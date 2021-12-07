<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="title" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="cssClass" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="infoPopup" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="appendedText" required="false" rtexprvalue="true" type="java.lang.String" %>

<section class="section ${cssClass}" id="${zfn:makeDomIdentifier(title)}">
    <c:if test="${!empty title}">
    <div class="heading">${title}
            <c:if test="${!empty appendedText}">${appendedText}</c:if>
        <c:if test="${not empty infoPopup}"><a class="popup-link info-popup-link"></a></c:if>
        </c:if>
        <z:ifHasData test="${hasData}">
            <jsp:doBody/>
        </z:ifHasData>
</section>
