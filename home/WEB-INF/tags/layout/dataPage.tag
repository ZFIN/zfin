<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sections" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="entityName" required="false" fragment="true" %>
<%@ attribute name="entityNameAddendum" required="false" fragment="true" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="pageBar" required="false" %>

<%@ attribute name="additionalBodyClass" required="false" type="java.lang.String" %>
<c:set var="additionalBodyClass" value="${(empty additionalBodyClass) ? '' : additionalBodyClass}" />

<jsp:invoke fragment="entityName" var="entityNameValue"/>
<jsp:invoke fragment="entityNameAddendum" var="entityNameAddendumValue"/>

<z:page bodyClass="data-page" additionalBodyClass="${additionalBodyClass}" bootstrap="true" title="${title}">
    <div class="d-flex h-100">
        <div class="data-page-nav-container">
            <ul class="nav nav-pills flex-column">
                <c:if test="${!empty entityNameValue}">
                    <li class="nav-item w-100">
                        <h5 class="p-3 m-0 border-bottom text-truncate back-to-top-link">
                            <a href="#" class="back-to-top-link" title="Back to top">
                                ${entityNameValue}
                            </a>
                            <c:if test="${!empty entityNameAddendumValue}">
                                ${entityNameAddendumValue}
                            </c:if>
                        </h5>
                    </li>
                </c:if>
                <c:forEach var="section" items="${sections}">
                    <!-- TODO: wrap this in a tag or something that can handle optional logic of using react or not -->
                    <li class="nav-item" role="presentation">
                        <a class="nav-link" href="#${zfn:makeDomIdentifier(section)}">
                            <span class="__react-root __redux" id="NavigationItem"
                                 data-title="${section}"></span>
                        </a>
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