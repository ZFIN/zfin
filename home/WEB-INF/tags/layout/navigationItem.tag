<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="title" type="java.lang.String" required="true" description="title of this navigation item" %>
<%@ attribute name="useNavigationCounter" required="false" %>
<%@ attribute name="borderBottom" required="false" %>
<%@ attribute name="order" required="false" %>
<%@ attribute name="status" required="false" rtexprvalue="true" type="java.lang.Object" description="optional FieldStatus rendered as a trailing badge" %>
<%@ attribute name="indent" required="false" rtexprvalue="true" type="java.lang.Boolean" description="render as a child item with extra left padding (pl-4)" %>
<%@ attribute name="cssClass" required="false" rtexprvalue="true" type="java.lang.String" description="extra CSS classes to append to the <li>" %>
<%@ attribute name="href" required="false" rtexprvalue="true" type="java.lang.String" description="explicit anchor href; overrides the default '#'+makeDomIdentifier(title)" %>

<c:set var="additionalCssClasses" value=""/>
<c:if test="${indent}">
    <c:set var="additionalCssClasses" value="${additionalCssClasses} pl-4"/>
</c:if>
<c:if test="${borderBottom}">
    <c:set var="additionalCssClasses" value="border-bottom"/>
</c:if>
<c:if test="${not empty cssClass}">
    <c:set var="additionalCssClasses" value="${additionalCssClasses} ${cssClass}"/>
</c:if>

<c:if test="${useNavigationCounter}">
    <c:set var="additionalCssClasses" value="${additionalCssClasses} with-counts"/>
</c:if>

<c:choose>
    <c:when test="${not empty href}"><c:set var="resolvedHref" value="${href}"/></c:when>
    <c:otherwise><c:set var="resolvedHref" value="#${zfn:makeDomIdentifier(title)}"/></c:otherwise>
</c:choose>
<li class="nav-item ${additionalCssClasses}" role="presentation">
    <a class="nav-link pr-2" href="${resolvedHref}">
        <c:if test="${not empty status}"><span class="mr-2"><z:zirc-status-badge status="${status}"/></span></c:if>
        <span class="nav-text">${title}</span>
        <c:if test="${useNavigationCounter}">
            <span class="__react-root __use-navigation-counter nav-count" id="NavigationItem__${order}" data-title="${title}"></span>
        </c:if>
    </a>
</li>
