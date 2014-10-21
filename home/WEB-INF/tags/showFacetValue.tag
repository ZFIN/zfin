<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="gaCategory" type="java.lang.String" required="true" %>
<%@attribute name="value" type="org.zfin.search.presentation.FacetValue" required="true" %>
<%@attribute name="showIncludeExclude" type="java.lang.Boolean" required="true"%>



<c:set var="specialTitle"></c:set>
<c:set var="showHover">false</c:set>
<c:choose>
    <c:when test="${value.label == 'Fish'}">
        <c:set var="specialTitle">Fish = genotype + reagents</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
    <c:when test="${value.label == 'Sequence Targeting Reagent'}">
        <c:set var="specialTitle">MO, CRISPR, TALEN</c:set>
        <c:set var="showHover">true</c:set>
    </c:when>
    <c:when test="${value.label == 'Construct'}">
        <c:set var="specialTitle">includes reporter expression</c:set>
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




<li style="min-height:10px" class="facet-value row-fluid">
    <span style="min-height:10px"
          class="span9 selectable-facet-value">
        <c:if test="${showIncludeExclude}">
            <a href="${value.url}" onclick="ga('send', 'event', '${gaCategory} Facet', 'include', '${value.label}')">
                <i title="include term" class="include-exclude-icon facet-include fa fa-plus-circle"></i>
            </a>
            <a href="${value.excludeUrl}" onclick="ga('send', 'event', '${gaCategory} Facet', 'exclude', '${value.label}')">
                <i title="exclude term" class="include-exclude-icon facet-exclude fa fa-minus-circle"></i>
            </a>
        </c:if>
        <a style="padding-right: 2px; min-height:10px" class=" facet-value-hover <c:if test="${showHover == 'true'}">facet-value-hover</c:if> "
           title="${specialTitle}"
           href="${value.url}"
           onclick="ga('send', 'event', '${gaCategory} Facet', 'include', '${value.label}')">
            ${value.label}
        </a>
        </span>
    <ul style="min-height:10px"  <%--title="${specialTitle}"--%>
        class="facet-count-container span3 unstyled">
          <li class="unstyled"><span class="facet-count">
              (<fmt:formatNumber value="${value.count}" pattern="##,###"/>)</span></li>
<%--        <li class="dropdown">

            <a class="facet-count dropdown-toggle"
               data-toggle="dropdown"
               href="#">
                 <b class="caret"></b>
            </a>
            <ul class="dropdown-menu">
                <li>
                    <a href="${value.url}">Require</a>
                </li>
                <li>
                    <a href="${value.excludeUrl}">Exclude</a>
                </li>
            </ul>
        </li>--%>
    </ul>
</li>

