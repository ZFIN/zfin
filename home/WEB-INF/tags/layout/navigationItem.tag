<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag pageEncoding="UTF-8" %>
<%@ attribute name="title" type="java.lang.String" required="true" description="title of this navigation item" %>
<%@ attribute name="useNavigationCounter" required="false" %>

<li class="nav-item" role="presentation">
    <a class="nav-link" href="#${zfn:makeDomIdentifier(title)}">
        ${title}
        <c:if test="${useNavigationCounter}">
            <span class="__react-root __use-navigation-counter" id="NavigationItem" data-title="${title}"></span>
        </c:if>
    </a>
</li>
