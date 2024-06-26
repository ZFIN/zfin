<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="open" type="java.lang.Boolean" required="true" %>
<%@attribute name="facetQuery" type="org.zfin.search.presentation.FacetQuery" %>
<%@attribute name="gaCategory" type="java.lang.String" required="true" %>

<ol class="facet-value-list list-unstyled" id="${name}-facet-value-list">
    <div <c:if test="${open == false}">style="display: none"</c:if>>
        <div class="single-facet-value-container" id="${name}-facet-value-container">
            <c:choose>
                <%-- not selected --%>
                <c:when test="${!facetQuery.selected}">
                    <li class="facet-value row">
                        <div class="col-lg-2 col-3 tight-on-the-right">
                        </div>
                        <div class="col-lg-7 col-7 tight-on-the-left">
                            <a class="facet-link"
                               onclick="ga('send', 'event', '${gaCategory} Facet', 'include', '${facetQuery.label}')"
                               href="${facetQuery.url}">
                                    ${facetQuery.label}
                            </a>
                        </div>
                        <div class="col-lg-3 col-2 facet-count">
                            <span class="float-right">
                                (<fmt:formatNumber value="${facetQuery.count}" pattern="##,###"/>)
                            </span>
                        </div>
                    </li>
                </c:when>
                <%-- selected --%>
                <c:otherwise>
                    <li class="facet-value row">
                        <div class="col-lg-2 col-3 tight-on-the-right">
                            <div class="float-right">
                                <i class="fas fa-check-square facet-selected"></i>
                            </div>
                        </div>
                        <div class="col-lg-10 col-9 tight-on-the-left">
                            <a class="facet-link" href="${facetQuery.url}">
                                    ${facetQuery.label}
                            </a>
                        </div>
                    </li>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</ol>
