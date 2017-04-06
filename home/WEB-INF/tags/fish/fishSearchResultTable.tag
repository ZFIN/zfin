<%@ tag import="org.zfin.fish.presentation.SortBy" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="formBean" type="org.zfin.fish.presentation.FishSearchFormBean" required="false" %>

<div style="margin-top: 2em ; margin-bottom: .3em">

<span style="text-align: center; margin-top: 8px; margin-left: 4px;">
    <c:if test="${formBean.totalRecords > 0}">
        <span class=bold>
            <fmt:formatNumber value="${formBean.totalRecords}" pattern="##,###"/> Fish found
        </span>
    </c:if>
</span>

    <p></p>

<%--    <div style="float:left ; margin-top: 1px;">

        Sort by
        <label for="sort-by-pulldown">
            <select name="sortByPulldown" id="sort-by-pulldown">
                <option value="<%= SortBy.BEST_MATCH %>" id="sort-by-best-match">Fish (Best Match)</option>
                <option value="<%= SortBy.GENES %>" id="sort-by-genes">Affected Genomic Region</option>
                <option value="<%= SortBy.FEATURES %>" id="sort-by-features">Line/Reagent</option>
            </select>
        </label>
    </div>--%>

    <div style="float:right ; margin-top: 2px;">
        <select name="maxDisplayRecordsTop" id="max-display-records-top" class="max-results">
            <c:forEach items="${formBean.recordsPerPageList}" var="option">
                <option>${option}</option>
            </c:forEach>
        </select>
        <label for="max-display-records-top">results per page</label>

    </div>

    <zfin2:pagination paginationBean="${formBean}"/>
</div>

<%-- set defaults & change as necessary--%>
<c:set var="fishColumnClass" value="unselected-sort-column column-sort-asc"/>
<c:set var="geneColumnClass" value="unselected-sort-column column-sort-asc"/>
<c:set var="featureColumnClass" value="unselected-sort-column column-sort-asc"/>


<c:choose>
    <c:when test="${formBean.bestMatchSort}">
        <c:set var="fishColumnClass" value="selected-sort-column column-sort-asc"/>
    </c:when>
    <c:when test="${formBean.geneSortAscending}">
        <c:set var="geneColumnClass" value="selected-sort-column column-sort-asc"/>
    </c:when>
    <c:when test="${formBean.geneSortDescending}">
        <c:set var="geneColumnClass" value="selected-sort-column column-sort-desc"/>
    </c:when>
    <c:when test="${formBean.featureSortAscending}">
        <c:set var="featureColumnClass" value="selected-sort-column column-sort-asc"/>
    </c:when>
    <c:when test="${formBean.featureSortDescending}">
        <c:set var="featureColumnClass" value="selected-sort-column column-sort-desc"/>
    </c:when>
</c:choose>

<table class="searchresults rowstripes" style="clear: both;">

    <tr>
        <th rowspan="2" width="10%" id="fish-column-header" style="vertical-align: text-top;" title="Fish = Genotype + STR">
            Fish
            <img class="column-sort-button fish-column-sort-button " src="/images/transp.gif" alt=""/>
        </th>
        <th>&nbsp;</th>
        <th></th>
        <th></th>
        <th></th>
        <th>Expression</th>
        <th>Phenotype</th>
    </tr>
    <tr>
        <th width="12%" id="affected-gene-column-header" style="white-space: nowrap;">
            Affected Genomic Region
        </th>
        <th width="15%" id="feature-column-header" style="white-space: nowrap;">
            Line/Reagent
            <img class="column-sort-button feature-column-sort-button" src="/images/transp.gif" alt=""/>
        </th>
        <th>Construct</th>
        <th width="15%">Mutation Type</th>
        <th></th>
        <th width="18%">
<%--            <div id="showAllLink" style="float: right; font-size:small; font-weight:normal;">
                <a href="#">All Matching Details</a>
            </div>
            <div id="hideAllLink" style="float: right; display: none;font-size:small; font-weight:normal;">
                <a href="#">Hide Matching Details</a>
            </div>--%>
        </th>
    </tr>
    <script>
        jQuery('#affected-gene-column-header, #feature-column-header').tipsy({gravity:'s', opacity:1, delayIn:650, delayOut:200});
        jQuery('#fish-column-header').tipsy({gravity:'sw', opacity:1, delayIn:750, delayOut:200});
    </script>
    <c:forEach var="result" items="${formBean.fishSearchResult.results}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td class="bold" colspan="5">
                <zfin:link entity="${result.fish}"/> <%--[${result.fish.order}]--%>
            </td>
            <td>

                <c:if test="${result.expressionFigureCount > 0}">
                    <a href="/action/expression/fish-expression-figure-summary?fishID=${result.fish.zdbID}&imagesOnly=false">
                        <zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                                     integerEntity="${result.expressionFigureCount}"/>
                    </a>
                </c:if>
                <zfin2:showCameraIcon hasImage="${result.expressionImageAvailable}"/>
            </td>
            <td>
                <c:if test="${result.phenotypeFigureCount > 0}">
                    <%-- Case of a single figure --%>
                    <c:if test="${result.phenotypeFigureCount ==1}">
                        <zfin:link entity="${result.singleFigure}"/>
                    </c:if>
                    <%-- case of multiple figures --%>
                    <c:if test="${result.phenotypeFigureCount > 1}">
                        <a href="phenotype-summary?fishID=${result.fish.fishID}&<%= request.getQueryString()%>">
                            <zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                                         integerEntity="${result.phenotypeFigureCount}"/>
                        </a>
                    </c:if>
                    <zfin2:showCameraIcon hasImage="${result.imageAvailable}"/>
                </c:if>
            </td>
        </zfin:alternating-tr>
        <c:forEach var="featureGene" items="${result.featureGenes}" varStatus="fgIndex">
            <zfin:alternating-tr loopName="loop">
                <td></td>
                <td>
                    <zfin:link entity="${featureGene.gene}"/>
                </td>
                <td>
                    <%-- will be one or the other--%>
                    <c:if test="${!empty featureGene.feature}">
                        <zfin:link entity="${featureGene.feature}"/>
                    </c:if>
                    <c:if test="${!empty featureGene.sequenceTargetingReagent}">
                        <zfin:link entity="${featureGene.sequenceTargetingReagent}"/>
                    </c:if>
                </td>
                <td>
                    <i><zfin:link entity="${featureGene.construct}"/></i>
                </td>
                <td>
                    <c:if test="${!empty featureGene.feature}">
                        ${featureGene.feature.type.display}
                    </c:if>
                </td>
                <td>
                </td>
                <td>
                    <c:if test="${(fgIndex.last) && (!formBean.showAllMutantFish)}">
                        <authz:authorize access="hasRole('root')">
                        <span style="font-size:small ; opacity: 0.33;">
                          <a style=" " class="clickable" onclick="jQuery('#${result.fish.zdbID}-text').slideToggle(); ">Score</a>
                          | <a style=" " href="/action/database/view-record/FISH-${fish.ID}">DB</a> | 
                        </span>
                        </authz:authorize>

<%--
                        <span style="float:right" id="matching-details-show-link${loop.index}">
                            <c:if test="${result.fish.genotype!=null}">
                            <a style="font-size:smaller; margin-right: 1em;" class="clickable showAll"
                               onclick="jQuery('#matching-details-show-link${loop.index}').hide();
                                       jQuery('#matching-details-hide-detail${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').load('/action/fish/matching-detail?fishID=${result.fish.zdbID}&<%= request.getQueryString()%>', function() { processPopupLinks('#matching-details-${loop.index}'); });">
                                Matching Detail</a>
                                </c:if>
                            <c:if test="${result.fish.genotype==null}">
                            <a style="font-size:smaller; margin-right: 1em;" class="clickable showAll"
                               onclick="jQuery('#matching-details-show-link${loop.index}').hide();
                                       jQuery('#matching-details-hide-detail${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').load('/action/fish/matching-detail?fishID=${result.fish.zdbID}&<%= request.getQueryString()%>', function() { processPopupLinks('#matching-details-${loop.index}'); });">
                                Matching Detail</a>
                        </span>
                            </c:if>
                        <span style="text-align:right; display: none;" id="matching-details-hide-detail${loop.index}">
                            <a style="font-size:small; margin-right: 1em;" class="clickable hideAll"
                               onclick="jQuery('#matching-details-${loop.index}').hide();
                                       jQuery('#matching-details-hide-detail${loop.index}').hide();
                                       jQuery('#matching-details-show-link${loop.index}').show();">Hide Detail</a>
                        </span>
--%>
                    </c:if>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
        <zfin:alternating-tr loopName="loop">
            <td colspan="7">
                <authz:authorize access="hasRole('root')">
                    <div id="${result.fish.zdbID}-text"
                         style="width: 800px ; display:none; margin: 0.5em 2em; padding: .5em; ">
                        <strong>Gene Or Feature Text:</strong> ${result.geneOrFeatureText}
                        <div>${result.scoringText}</div>
                    </div>
                </authz:authorize>
                <div align="right" style="font-size:smaller;" id="matching-details-${loop.index}"></div>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
<input name="page" type="hidden" value="1" id="page"/>

<div style="float:right ; margin-top: 2px;">
    <select name="maxDisplayRecordsBottom" id="max-display-records-bottom" class="max-results">
        <c:forEach items="${formBean.recordsPerPageList}" var="option">
            <option>${option}</option>
        </c:forEach>
    </select>
    <label for="max-display-records-bottom">results per page</label>

</div>

<zfin2:pagination paginationBean="${formBean}"/>


<script type="text/javascript">

    jQuery('#showAllLink').click( function(evt) {
        evt.preventDefault();
        jQuery('.showAll').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllLink').hide();
        jQuery('#hideAllLink').show();
    });

    jQuery('#hideAllLink').click( function(evt) {
        evt.preventDefault();
        jQuery('.hideAll').each(function () {
            jQuery(this).click();
        });
        jQuery('#showAllLink').show();
        jQuery('#hideAllLink').hide();
    });

    jQuery('.max-results').change(function () {
        var $maxDisplayHidden = jQuery('#max-display-records-hidden');
        $maxDisplayHidden.val(jQuery(this).val());
        $maxDisplayHidden.change();
    });

    jQuery('#sort-by-pulldown').change(function () {
        jQuery("#sort-by").val((jQuery(this).val()));
        submitForm(1);
    });

</script>
