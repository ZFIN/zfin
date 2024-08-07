<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="value" type="org.zfin.search.presentation.FacetValue"%>

<li class="facet-value selected-facet-value row">
    <div class="col-lg-2 col-3 tight-on-the-right">
        <div class="float-right">
            <i class="fas fa-check-square facet-selected"></i>
        </div>
    </div>
    <div class="col-lg-10 col-9 tight-on-the-left">
        <a class="facet-link" href="${value.url}">
            ${value.label}
        </a>
    </div>
</li>
<c:if test="${!empty value.childFacets}">
    <li style="margin-left: 20px">
        <ol class="facet-value-list child-facet list-unstyled">
            <c:forEach var="childFacet" items="${value.childFacets}">
                <c:choose>
                    <c:when test="${childFacet.selected}">
                        <zfin2:showSelectedFacetValue value="${childFacet}"/>
                    </c:when>
                    <c:otherwise>
                        <zfin2:showFacetValue gaCategory="${gaCategory}" value="${childFacet}" showIncludeExclude="${showIncludeExclude}"/>
                    </c:otherwise>
                </c:choose>


            </c:forEach>
        </ol>
    </li>
</c:if>
