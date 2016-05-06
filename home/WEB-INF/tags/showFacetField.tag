<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="open" type="java.lang.Boolean" required="true" %>
<%@attribute name="facet" type="org.zfin.search.presentation.Facet"%>
<%@attribute name="showLabel" type="java.lang.Boolean" required="true" %>


<c:set var="name" value="${facet.name}"/>

<ol class="facet-value-list list-unstyled" id="${name}-facet-value-list">

    <a name="${name}"/></a>

    <c:if test="${showLabel}">
        <div id="${name}-facet-label-container" class="facet-label-container">

            <%-- Only show the widgets if there are values --%>
            <c:choose>
                <c:when test="${(fn:length(facet.selectedFacetValues) + fn:length(facet.facetValues)) > 0}">
                    <i class="fa fa-fw fa-caret-down facet-control-widget ${name}-outer-toggle"
                            <c:if test="${!open}">style="display:none;"</c:if>
                     ></i>
                    <i class="fa fa-fw fa-caret-right facet-control-widget ${name}-outer-toggle"
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

                <span id="${name}-facet-field-count" class="facet-field-count pull-right"
                        <c:if test="${open}">style=" display:none" </c:if>
                        >[${facet.nonEmptyDocumentCount}]
                </span>

        </div>
    </c:if>

    <div class="facet-value-outer-box"
         id="${name}-facet-value-outer-box"
         <c:if test="${open == false}">style="display: none"</c:if>
         >

        <%-- only make it clickable if there's something inside to display --%>
        <c:if test="${(fn:length(facet.selectedFacetValues) + fn:length(facet.facetValues)) > 0}">
        <script>
            $('#${name}-facet-label-container').click(function () {
                $('#${name}-facet-value-outer-box').slideToggle(100);
                $('.${name}-outer-toggle').toggle();
                $('#${name}-facet-field-count').toggle();
            });
        </script>
        </c:if>

           <div class="single-facet-value-container" id="${name}-facet-value-container">

               <c:forEach var="facetQuery" items="${facet.facetQueries}" varStatus="loop">
                   <li><zfin-search:showFacetQuery open="true" gaCategory="${zfn:buildFacetedSearchGACategory(category, facet.label)}" facetQuery="${facetQuery}"/></li>
               </c:forEach>

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
        <c:if test="${(fn:length(facet.facetValues) + fn:length(facet.selectedFacetValues)) > 4 && !facet.alwaysShowAllFacets}">
        <li class="row">
            <div class="col-md-2 col-xs-2"> <%-- this is a placehold because tight-on-the-left breaks the offsets --%></div>
            <div id="${name}-facet-expand-contract-links" class="col-md-8 col-xs-9 tight-on-the-left facet-expand-contract-links">
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
