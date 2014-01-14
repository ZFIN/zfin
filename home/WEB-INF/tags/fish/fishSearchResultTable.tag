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

    <div style="float:left ; margin-top: 1px;">

        Sort by
        <label for="sort-by-pulldown">
            <select name="sortByPulldown" id="sort-by-pulldown">
                <option value="<%= SortBy.BEST_MATCH %>" id="sort-by-best-match">Fish (Best Match)
                </option>
                <option value="<%= SortBy.GENES %>" id="sort-by-genes">Affected Gene
                </option>
                <%--
                        <option value="<%= SortBy.GENES_REVERSE %>" id="sort-by-genes-reverse"
                                onclick="setSortingOption('<%= SortBy.GENES_REVERSE %>');">Affected Genes (Reversed)
                        </option>
                --%>
                <option value="<%= SortBy.FEATURES %>" id="sort-by-features">Line/Reagent
                </option>
                <%--
                        <option value="<%= SortBy.FEATURES_REVERSE %>" id="sort-by-features-reverse"
                                onclick="setSortingOption('<%= SortBy.FEATURES_REVERSE %>');">Genomic Features (Reversed)
                        </option>
                --%>
            </select>
        </label>
    </div>

    <div style="float:right ; margin-top: 2px;">
<%--
        <form:select path="maxDisplayRecords" items="${formBean.recordsPerPageList}"
                     onchange="submitFishSearchWithNumOfRecords(50);return true;"></form:select>
--%>
        <select name="maxDisplayRecordsTop" id="max-display-records-top" >
            <c:forEach items="${formBean.recordsPerPageList}" var="option">
                <option>${option}</option>
            </c:forEach>
        </select>
        <label for="max-display-records-top">results per page</label>

    </div>


    <script>

        jQuery('#max-display-records-top').val(${formBean.maxDisplayRecords});
        jQuery('#max-display-records-bottom').val(${formBean.maxDisplayRecords});

        function setMaxDisplayRecords(value) {
            jQuery('#max-display-records-hidden').val(value);
            jQuery('#max-display-records-hidden').change();
        }

        jQuery('#max-display-records-top').change(function () {
            setMaxDisplayRecords(jQuery('#max-display-records-top option:selected').val());
        });

        jQuery('#sort-by-pulldown').change(function () {
            setSortingOption(jQuery('#sort-by-pulldown option:selected').attr('value'))
        });

    </script>

    <script language="JavaScript">
        function setSortingOption(value) {
            document.getElementById("sort-by").value = value;
            submitForm(1);

        }
        function setSortingPulldown(value) {
            jQuery('#sort-by-pulldown option[value="' + value + '"]').attr('selected', 'selected');
        }

        function showAll() {
            jQuery('.showAll').each(function () {
                jQuery(this).click();
            });
            jQuery('#showAllLink').hide();
            jQuery('#hideAllLink').show();
        }

        function hideAll() {
            jQuery('.hideAll').each(function () {
                jQuery(this).click();
            });
            jQuery('#showAllLink').show();
            jQuery('#hideAllLink').hide();
        }
    </script>

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
        <th rowspan="2" width="10%" id="fish-column-header"
        <%--
                    class="secretly-clickable sortable-column ${fishColumnClass}"
        --%>
            style="vertical-align: text-top;"
            title="Fish = genotype + reagents">
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
        <th width="12%"
        <%--
                    class="secretly-clickable sortable-column ${geneColumnClass}"
        --%>
            id="affected-gene-column-header" style="white-space: nowrap;">
            Affected Gene
            <%--
                        <img class="column-sort-button gene-column-sort-button" src="/images/transp.gif"/>
            --%>
        </th>

        <th width="15%" id="feature-column-header"
        <%--
                    class="secretly-clickable sortable-column ${featureColumnClass}"
        --%>
            style="white-space: nowrap;">Line/Reagent
            <img class="column-sort-button feature-column-sort-button" src="/images/transp.gif" alt=""/>
        </th>
        <th>Construct</th>
        <th width="15%">Mutation Type</th>
        <th></th>
        <th width="18%">
            <div id="showAllLink" style="float: right; font-size:small; font-weight:normal;">
                <a href="javascript:showAll();">All Matching Details</a>
            </div>
            <div id="hideAllLink" style="float: right; display: none;font-size:small; font-weight:normal;">
                <a href="javascript:hideAll();">Hide Matching Details</a>
            </div>
        </th>
    </tr>
    <script>
        jQuery('#affected-gene-column-header, #feature-column-header').tipsy({gravity:'s', opacity:1, delayIn:650, delayOut:200});
        jQuery('#fish-column-header').tipsy({gravity:'sw', opacity:1, delayIn:750, delayOut:200});
    </script>

    <c:forEach var="fish" items="${formBean.fishList}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td class="bold" colspan="5">
                <c:if test="${fish.genotypeID!=null}">
                <zfin:link entity="${fish}"/>
                    <c:if test="${fn:length(fish.morpholinos) > 0 }">
                        <a class="popup-link data-popup-link" href="/action/genotype/genotype-detail-popup?zdbID=${fish.fishID}"></a>
                     </c:if>
                <c:if test="${fn:length(fish.morpholinos) == 0 }">
                    <a class="popup-link data-popup-link" href="/action/genotype/genotype-detail-popup?zdbID=${fish.genotypeID}"></a>
                </c:if>

                </c:if>

            </td>
            <td>
               <c:if test="${fish.expressionFigureCount > 0}">
                <zfin2:fishSearchExpressionFigureLink queryKeyValuePair="fishID=${fish.fishID}"
                                                      figureCount="${fish.expressionFigureCount}"/>
                    <span id="image-icon-${loop.index}">
                    </span>
                </c:if>
                <zfin2:showCameraIcon hasImage="${fish.expressionImageAvailable}"/>
            </td>
            <td>
                <c:if test="${fish.phenotypeFigureCount > 0}">
                    <%-- Case of a single figure --%>
                    <c:if test="${fish.phenotypeFigureCount ==1}">
                        <zfin:link entity="${fish.singleFigure}"/>
                    </c:if>
                    <%-- case of multiple figures --%>
                    <c:if test="${fish.phenotypeFigureCount > 1}">
                        <a href="phenotype-summary?fishID=${fish.fishID}&<%= request.getQueryString()%>">
                            <zfin:choice choicePattern="0# Figures| 1# Figure| 2# Figures" includeNumber="true"
                                         integerEntity="${fish.phenotypeFigureCount}"/>
                        </a>
                    </c:if>
                    <zfin2:showCameraIcon hasImage="${fish.imageAvailable}"/>
                </c:if>
            </td>
        </zfin:alternating-tr>
        <c:forEach var="featureGene" items="${fish.featureGenes}" varStatus="fgIndex">
            <zfin:alternating-tr loopName="loop">
                <td></td>
                <td>
                    <zfin:link entity="${featureGene.gene}"/>
                </td>
                <td>
                    <zfin:link entity="${featureGene.feature}"/>
                </td>
                <td>
                    <zfin:link entity="${featureGene.construct}"/>
                </td>
                <td>
                        ${featureGene.typeDisplay}
                </td>
                <td>
                </td>
                <td>
                    <c:if test="${(fgIndex.last) && (!formBean.showAllMutantFish)}">
                        <authz:authorize ifAnyGranted="root">
                        <span style="font-size:small ; opacity: 0.33;">
                          <a style=" " class="clickable" onclick="jQuery('#${fish.ID}-text').slideToggle(); ">Score</a>
                          | <a style=" " href="/action/database/view-record/FISH-${fish.ID}">DB</a> | 
                        </span>
                        </authz:authorize>

                        <span style="float:right" id="matching-details-show-link${loop.index}">
                            <c:if test="${fish.genotypeID!=null}">
                            <a style="font-size:smaller; margin-right: 1em;" class="clickable showAll"
                               onclick="jQuery('#matching-details-show-link${loop.index}').hide();
                                       jQuery('#matching-details-hide-detail${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').load('/action/fish/matching-detail?fishID=${fish.fishID}&<%= request.getQueryString()%>', function() { processPopupLinks('#matching-details-${loop.index}'); });">
                                Matching Detail</a>
                                </c:if>
                            <c:if test="${fish.genotypeID==null}">
                            <a style="font-size:smaller; margin-right: 1em;" class="clickable showAll"
                               onclick="jQuery('#matching-details-show-link${loop.index}').hide();
                                       jQuery('#matching-details-hide-detail${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').show();
                                       jQuery('#matching-details-${loop.index}').load('/action/fish/matching-detail?fishID=${fish.ID}&<%= request.getQueryString()%>', function() { processPopupLinks('#matching-details-${loop.index}'); });">
                                Matching Detail</a>
                        </span>
                            </c:if>
                        <span style="text-align:right; display: none;" id="matching-details-hide-detail${loop.index}">
                            <a style="font-size:small; margin-right: 1em;" class="clickable hideAll"
                               onclick="jQuery('#matching-details-${loop.index}').hide();
                                       jQuery('#matching-details-hide-detail${loop.index}').hide();
                                       jQuery('#matching-details-show-link${loop.index}').show();">Hide Detail</a>
                        </span>
                    </c:if>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
        <zfin:alternating-tr loopName="loop">
            <td colspan="7">
                <authz:authorize ifAnyGranted="root">
                    <div id="${fish.ID}-text"
                         style="width: 800px ; display:none; margin: 0.5em 2em; padding: .5em; ">
                        <strong>Gene Or Feature Text:</strong> ${fish.geneOrFeatureText}
                        <div>${fish.scoringText}</div>
                    </div>
                </authz:authorize>
                <div align="right" style="font-size:smaller;" id="matching-details-${loop.index}"></div>
            </td>
        </zfin:alternating-tr>
    </c:forEach>
</table>
<input name="page" type="hidden" value="1" id="page"/>

<div style="float:right ; margin-top: 2px;">
    <select name="maxDisplayRecordsBottom" id="max-display-records-bottom">
        <c:forEach items="${formBean.recordsPerPageList}" var="option">
            <option>${option}</option>
        </c:forEach>
    </select>
    <label for="max-display-records-bottom">results per page</label>

</div>

<zfin2:pagination paginationBean="${formBean}"/>


<script type="text/javascript">
    jQuery('#max-display-records-bottom').change(function () {
        setMaxDisplayRecords(jQuery('#max-display-records-bottom option:selected').val());
    });

</script>
