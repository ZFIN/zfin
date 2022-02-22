<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sections" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="entityName" required="false" fragment="true" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="pageBar" required="false" %>

<jsp:invoke fragment="entityName" var="entityNameValue"/>

<z:page bodyClass="data-page" bootstrap="true" title="${title}">
    <div class="d-flex h-100">
        <div class="data-page-nav-container">
            <ul class="nav nav-pills flex-column">
                <c:if test="${!empty entityNameValue}">
                    <li class="nav-item w-100">
                        <a href="#" class="back-to-top-link" title="Back to top">
                            <h5 class="p-3 m-0 border-bottom text-truncate">
                                    ${entityNameValue}
                            </h5>
                        </a>
                    </li>
                </c:if>
                <c:forEach var="section" items="${sections}">
                    <li class="nav-item" role="presentation">
                        <a class="nav-link" href="#${zfn:makeDomIdentifier(section)}">${section}</a>
                    </li>
                </c:forEach>
            </ul>
        </div>

        <div class="data-page-content-container">
            <c:if test="${not empty pageBar}">
                <span>${pageBar}</span>
            </c:if>
            <jsp:doBody/>
        </div>
    </div>

    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>