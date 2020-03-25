    <%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

        <%@ attribute name="sections" required="true" rtexprvalue="true" type="java.util.Collection" %>


        <div class="d-flex">
        <div class="data-page-nav-container">
        <ul class="nav nav-pills flex-column">

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