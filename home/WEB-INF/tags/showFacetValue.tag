<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="gaCategory" type="java.lang.String" required="true" %>
<%@attribute name="value" type="org.zfin.search.presentation.FacetValue" required="true" %>
<%@attribute name="showIncludeExclude" type="java.lang.Boolean" required="true"%>

<c:set var="specialTitle"></c:set>
<c:set var="showHover">false</c:set>
<c:choose>
    <c:when test="${value.label == 'Fish'}">
        <c:set var="specialTitle">Fish = Genotype + STR</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
    <c:when test="${value.label == 'Sequence Targeting Reagent (STR)'}">
        <c:set var="specialTitle">MO, CRISPR, TALEN</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
    <c:when test="${value.label == 'Construct'}">
        <c:set var="specialTitle">Includes reporter expression</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
    <c:when test="${value.label == 'Expression'}">
        <c:set var="specialTitle">Expression Figures</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
    <c:when test="${value.label == 'Phenotype'}">
        <c:set var="specialTitle">Phenotype Figures</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
</c:choose>

<li class="facet-value">
    <span>
        <c:if test="${showIncludeExclude}">
            <span style="white-space: nowrap">
                <a href="${value.url}" onclick="ga('send', 'event', '${gaCategory} Facet', 'include', '${value.label}')">
                    <i title="include term" class="include-exclude-icon facet-include fa fa-plus-circle"></i>
                </a>
                <a href="${value.excludeUrl}" onclick="ga('send', 'event', '${gaCategory} Facet', 'exclude', '${value.label}')">
                    <i title="exclude term" class="include-exclude-icon facet-exclude fa fa-minus-circle"></i>
                </a>
            </span>
        </c:if>
    </span>
    <span>
        <a class="facet-value-hover facet-link"
           title="${specialTitle}"
           href="${value.url}"
           onclick="ga('send', 'event', '${gaCategory} Facet', 'include', '${value.label}')">
            ${value.label}
        </a>
    </span>
    <span class="facet-count">
        (<fmt:formatNumber value="${value.count}" pattern="##,###"/>)
    </span>
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

