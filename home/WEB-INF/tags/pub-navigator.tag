<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="pages" type="org.zfin.publication.presentation.PublicationDashboardController.Page[]" rtexprvalue="true" required="true" %>
<%@ attribute name="currentPage" type="org.zfin.publication.presentation.PublicationDashboardController.Page" rtexprvalue="true" required="true" %>

<nav class="pub-navigator navbar navbar-expand-sm success">
    <div class="container-fluid">
        <ul class="navbar-nav">
            <c:forEach items="${pages}" var="page">
                <li class="nav-item">
                    <a class="nav-link ${page == currentPage ? 'active' : ''}" href="${page.url}">${page.title}</a>
                </li>
            </c:forEach>
        </ul>
    </div>
</nav>
