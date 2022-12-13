<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="title" type="java.lang.String" required="true" description="title of this navigation item" %>
<%@ attribute name="useNavigationCounter" required="false" %>
<%@ attribute name="borderBottom" required="false" %>
<%@ attribute name="order" required="false" %>

<c:set var="additionalCssClasses" value=""/>
<c:if test="${borderBottom}">
    <c:set var="additionalCssClasses" value="border-bottom"/>
</c:if>

<c:if test="${useNavigationCounter}">
    <c:set var="additionalCssClasses" value="${additionalCssClasses} with-counts"/>
</c:if>

<li class="nav-item ${additionalCssClasses}" role="presentation">
    <a class="nav-link pr-2" href="#${zfn:makeDomIdentifier(title)}">
        <span class="nav-text">${title}</span>
        <c:if test="${useNavigationCounter}">
            <span class="__react-root __use-navigation-counter nav-count" id="NavigationItem__${order}" data-title="${title}"></span>
        </c:if>
    </a>
</li>
