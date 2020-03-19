    <%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

        <%@ attribute name="sections" required="true" rtexprvalue="true" type="java.util.Collection" %>
        <%@ attribute name="entityabbrev" required="false" rtexprvalue="true" type="java.lang.String" %>

        <div class="d-flex">
        <div class="data-page-nav-container">
        <ul class="nav nav-pills flex-column">
        <li>
        <h4>${entityabbrev}</h4>
        </li>
        <c:forEach var="section" items="${sections}">
            <li class="nav-item" role="presentation"><a class="nav-link" href="#${zfn:makeDomIdentifier(section)}
            ">${section}</a></li>
        </c:forEach>
        </ul>
        </div>

        <div class="data-page-content-container">
        <jsp:doBody/>
        </div>
        </div>