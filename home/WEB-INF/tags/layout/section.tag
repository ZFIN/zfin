<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="title" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="entity" required="false" rtexprvalue="true" type="org.zfin.infrastructure.ZdbID" %>
<%@ attribute name="hasData" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="cssClass" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="infoPopup" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="appendedText" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="navigationMenu" required="false" rtexprvalue="true" type="org.zfin.framework.presentation.NavigationMenu" %>

<c:set var="anchorTitle" value="${title}"/>

<c:if test="${!empty navigationMenu}">
    <c:set var="hide" value="${!navigationMenu.include(title)}"/>
</c:if>

<c:if test="${!empty appendedText}">
    <c:set var="title" value="${title} ${appendedText}"/>
</c:if>

<%-- Skip this section if "hide" attribute is true (ie. navigationMenu includes this section) --%>
<c:if test="${!hide}">
    <section class="section ${cssClass}" id="${zfn:makeDomIdentifier(anchorTitle)}">
        <c:if test="${!empty title}">
            <div class="heading">
                <c:choose>
                    <c:when test="${empty entity }">
                        ${title}
                    </c:when>
                    <c:otherwise>
                        <a href="/${entity.zdbID}">${title}</a>
                    </c:otherwise>
                </c:choose>
                <c:if test="${not empty infoPopup}"><a class="popup-link info-popup-link" href="${infoPopup}"></a></c:if>
            </div>
        </c:if>
        <z:ifHasData test="${hasData}">
            <jsp:doBody/>
        </z:ifHasData>
    </section>
</c:if>
