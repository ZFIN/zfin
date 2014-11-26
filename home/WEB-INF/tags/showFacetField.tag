<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="open" type="java.lang.Boolean" required="true" %>
<%@attribute name="facet" type="org.zfin.search.presentation.Facet"%>
<%@attribute name="showLabel" type="java.lang.Boolean" required="true" %>


<c:set var="name" value="${facet.name}"/>

<ol class="facet-value-list" id="${name}-facet-value-list">

    <a name="${name}"/></a>

    <c:if test="${showLabel}">
        <div id="${name}-facet-label-container" class="facet-label-container">

            <%-- Only show the widgets if there are values --%>
            <c:choose>
                <c:when test="${(fn:length(facet.selectedFacetValues) + fn:length(facet.facetValues)) > 0}">
                    <i class="fa fa-caret-down facet-control-widget ${name}-outer-toggle"
                            <c:if test="${!open}">style="display:none;"</c:if>
                     ></i>
                    <i class="fa fa-caret-right facet-control-widget ${name}-outer-toggle"
                                <c:if test="${open}">style="display:none;"</c:if>
                     ></i>
                </c:when>
                <c:otherwise>
                    <c:set var="addLeftLabelSpacing" value="true"/>
                </c:otherwise>
            </c:choose>
            <span class="facet-label"
                  id="${name}-facet-label"
                    <c:if test="${addLeftLabelSpacing}">style="padding-left: 16px;"</c:if>
                    >

                  ${facet.label}
            </span>
<%--
            <c:if test="${!empty facet.nonEmptyDocumentCount}">
--%>
                <span id="${name}-facet-field-count" class="facet-field-count pull-right"
                        <c:if test="${open}">style=" display:none" </c:if>
                        >[${facet.nonEmptyDocumentCount}]
                </span>
           <%-- </c:if>--%>
<%--            <span class="facet-sort-links ${name}-toggle">
                Sort:
                <a href="/prototype?fq=category%3A%22Mutant+%2F+Tg%22&category=Mutant+%2F+Tg&f.type.facet.sort=count#type">#</a>
                |
                <a href="/prototype?fq=category%3A%22Mutant+%2F+Tg%22&category=Mutant+%2F+Tg&f.type.facet.sort=index#type">A-Z</a>
            </span>--%>
        </div>
    </c:if>

    <div class="facet-value-outer-box"
         id="${name}-facet-value-outer-box"
         <c:if test="${open == false}">style="display: none"</c:if>
         >

        <%-- only make it clickable if there's something inside to display --%>
        <c:if test="${(fn:length(facet.selectedFacetValues) + fn:length(facet.facetValues)) > 0}">
        <script>jQuery('#${name}-facet-label-container').click(function () {
            jQuery('#${name}-facet-value-outer-box').slideToggle(100);
            jQuery('.${name}-outer-toggle').toggle();
            jQuery('#${name}-facet-field-count').toggle();
        });  </script>
        </c:if>

           <div class="single-facet-value-container" id="${name}-facet-value-container">

            <c:forEach var="facetValue" items="${facet.selectedFacetValues}">
                <zfin2:showSelectedFacetValue value="${facetValue}"/>
            </c:forEach>

            <c:forEach var="facetValue" items="${facet.facetValues}" varStatus="loop">
                <c:if test="${loop.index == 4 && !facet.alwaysShowAllFacets}">
                    <div id="${name}-additional-values"
                         class="additional-facet-values ${name}-toggle">
                </c:if>
                <zfin2:showFacetValue gaCategory="${zfn:buildFacetedSearchGACategory(category, facet.label)}" value="${facetValue}" showIncludeExclude="${facet.showIncludeExcludeIcons}"/>
                <c:if test="${loop.count > 4 && loop.last && !facet.alwaysShowAllFacets}">
                    </div>
                </c:if>
            </c:forEach>

        </div>

        <c:if test="${fn:length(facet.facetValues) > 4 && !facet.alwaysShowAllFacets}">
        <li>
            <div id="${name}-facet-expand-contract-links" class="facet-expand-contract-links">
                <span <%--id="${name}"--%>
                   class="facet-expand-more-link ${name}-toggle "
                   href="#$<%--{name}--%>"
                   onClick="jQuery('.${name}-toggle').toggle();">Show More</span>
                <span <%--id="${name}"--%> class="facet-expand-less-link ${name}-toggle "
                   href="#<%--${name}--%>"
                   onClick="jQuery('.${name}-toggle').toggle() ; /*jQuery('html,body').animate({scrollTop:jQuery('#${name}-facet-label').offset().top}, 50);*/  ">
                    Show Less
                </span>
                <div>
                    <a class="facet-show-all-facets-link facet-value-modal-link" href="#"
                       onclick="ga('send', 'event', '${zfn:buildFacetedSearchGACategory(category, facet.label)} Facet', 'show all');"
                       data-toggle="modal" data-target="#facet-value-modal"
                       category="${category}" field="${facet.facetField.name}" modal-title="${facet.label}">Show All</a>
                </div>
            </div>
        </li>
        </c:if>
    </div>
</ol>
