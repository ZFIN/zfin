<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sections" required="true" rtexprvalue="true" type="java.util.Collection" %>
<%@ attribute name="sectionStatus" required="false" rtexprvalue="true" type="java.util.Map" description="optional section-name → status map; renders a status badge in the left nav next to each section" %>
<%@ attribute name="subSections" required="false" rtexprvalue="true" type="java.util.Map" description="optional section-name → Collection of child item titles; renders each as an indented sub-item under the parent section" %>
<%@ attribute name="subSectionStatus" required="false" rtexprvalue="true" type="java.util.Map" description="optional child-title → status map; renders a status badge next to each sub-item" %>
<%@ attribute name="subSubSections" required="false" rtexprvalue="true" type="java.util.Map" description="optional sub-item-title → Collection of grandchild titles; renders each as a deeper-indented sub-sub-item under the parent sub-item. Anchor is '#'+makeDomIdentifier(parent)+'-'+makeDomIdentifier(child)." %>
<%@ attribute name="subSubSectionStatus" required="false" rtexprvalue="true" type="java.util.Map" description="optional Map<sub-item-title, Map<sub-sub-title, status>> — status badges for the third level" %>
<%@ attribute name="entityName" required="false" fragment="true" %>
<%@ attribute name="entityNameAddendum" required="false" fragment="true" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="pageBar" required="false" %>
<%@ attribute name="additionalBodyClass" required="false" type="java.lang.String" %>
<%@ attribute name="useNavigationCounter" required="false" type="java.lang.Boolean" %>
<%@ attribute name="navigationMenu" required="false" type="org.zfin.framework.presentation.NavigationMenu" %>

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

<%--            If we have access to a navigationMenu object, use that to generate the left hand navigation, otherwise --%>
<%--            use the array of sections. I would like to remove the array of sections version eventually. --%>
                <c:choose>
                    <c:when test="${empty navigationMenu}">
                        <c:forEach var="section" items="${sections}" varStatus="loop">
                            <z:navigationItem title="${section}"
                                              status="${sectionStatus[section]}"
                                              order="${loop.index}"
                            />
                            <c:forEach var="sub" items="${subSections[section]}">
                                <z:navigationItem title="${sub}" status="${subSectionStatus[sub]}" indent="true"/>
                                <c:forEach var="subsub" items="${subSubSections[sub]}">
                                    <z:navigationItem title="${subsub}"
                                                      status="${subSubSectionStatus[sub][subsub]}"
                                                      cssClass="pl-5"
                                                      href="#${zfn:makeDomIdentifier(sub)}-${zfn:makeDomIdentifier(subsub)}"/>
                                </c:forEach>
                            </c:forEach>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="item" items="${navigationMenu.displayedNavigationItems}" varStatus="loop">
                            <z:navigationItem title="${item.toString()}"
                                              useNavigationCounter="${item.showCount && zfn:isRoot()}"
                                              borderBottom="${item.showBorder}"
                                              order="${loop.index}"
                            />
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
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