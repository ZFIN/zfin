<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sections" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="entityName" required="false" fragment="true" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<div class="d-flex">
    <div class="data-page-nav-container">
        <ul class="nav nav-pills flex-column">
            <li class="nav-item">
                <h5 class="p-3 m-0 border-bottom">
                    <jsp:invoke fragment="entityName" />
                </h5>
            </li>
            <c:forEach var="section" items="${sections}">
                <li class="nav-item" role="presentation">
                    <a class="nav-link" href="#${zfn:makeDomIdentifier(section)}">${section}</a>
                </li>
            </c:forEach>
        </ul>
    </div>

    <div class="data-page-content-container">
        <jsp:doBody />
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>
