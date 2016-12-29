<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.search.Category" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:set var="geneCategoryName" value="<%=Category.GENE.getName()%>"/>
<c:set var="expressionCategoryName" value="<%=Category.EXPRESSIONS.getName()%>"/>
<c:set var="phenotypeCategoryName" value="<%=Category.PHENOTYPE.getName()%>"/>
<c:set var="diseaseCategoryName" value="<%=Category.DISEASE.getName()%>"/>
<c:set var="mutationCategoryName" value="<%=Category.MUTANT.getName()%>"/>
<c:set var="publicationCategoryName" value="<%=Category.PUBLICATION.getName()%>"/>
<c:set var="constructCategoryName" value="<%=Category.CONSTRUCT.getName()%>"/>
<c:set var="strCategoryName" value="<%=Category.SEQUENCE_TARGETING_REAGENT.getName()%>"/>
<c:set var="abCategoryName" value="<%=Category.ANTIBODY.getName()%>"/>
<c:set var="anatomyCategoryName" value="<%=Category.ANATOMY.getName()%>"/>
<c:set var="markerCategoryName" value="<%=Category.MARKER.getName()%>"/>
<c:set var="communityCategoryName" value="<%=Category.COMMUNITY.getName()%>"/>
<c:set var="figureCategoryName" value="<%=Category.FIGURE.getName()%>"/>

<script src="/javascript/list-collapse.js"></script>
<script src="/javascript/figure-gallery-resize.jquery.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/imagesloaded.pkgd.min.js"></script>
<script src="/javascript/masonry.pkgd.min.js"></script>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<link rel="stylesheet" type="text/css" href="/css/datepicker3.css">
<script type="text/javascript" src="/javascript/bootstrap-datepicker.js"></script>

<script src="/javascript/purl.js"></script>
<script src="/javascript/jquery.validate.min.js"></script>

<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css">
<link rel="stylesheet" type="text/css" href="/css/faceted-search.css">

<style>
    /* remove parts of the header that we don't need */
    #quicksearchBox {
        display: none
    }
</style>

<script>
    hdrSetCookie("tabCookie","Motto","","/");
</script>


<div class="container-fluid">


    <div class="row">
        <div class="search-box col-md-offset-1 col-md-11">
            <form id="query-form" class="form-inline" method="get" action="/search">
                <div class="search-input-container">

                        <select class="form-control" name="category">
                            <option>Any</option>
                            <c:forEach items="${categories}" var="cat">
                                <option <c:if test="${cat eq category}">selected="selected"</c:if>>${cat}</option>
                            </c:forEach>
                        </select>


                        <input class="search-form-input form-control"
                               name="q"
                               id="primary-query-input"
                               autocomplete="off"
                               type="text"
                               value="<c:out value="${q}" escapeXml="true"/>"/>

                        <div class="btn-group search-box-buttons">
                            <button type="submit" class="btn btn-default btn-zfin">Go</button>
                            <authz:authorize access="hasRole('root')">
                                <c:if test="${category eq publicationCategoryName}">
                                    <a id="advanced-search-button" class="btn btn-default" href="#" title="Advanced Search Options"
                                       onClick="jQuery('#advanced-container').slideToggle(200);"><i class="fa fa-list"></i></a>
                                </c:if>
                            </authz:authorize>
                            <a class="btn btn-default" href="/search?q=" onclick="localStorage.clear();">New</a>
                            <a  class="btn btn-default" href="http://wiki.zfin.org/display/general/ZFIN+Single+Box+Search+Help" target="newWindow">
                                <i class="fa fa-question-circle"></i>
                            </a>
                            <a class="btn btn-default feedback-link" href="#">Feedback</a>
                        </div>
                    </div>

                <script>
                    function replaceQuery(query) {
                        jQuery('#primary-query-input').val(query);
                        jQuery('#query-form').submit();
                    }
                </script>
            </form>

            <div id="advanced-container" style="display:none;"  >
                <div class="row" >
                    <div class="col-md-10 col-md-offset-1">
                        <c:choose>
                            <c:when test="${category eq publicationCategoryName}">
                                <zfin-search:publicationAdvanced/>
                            </c:when>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <form style="display:none;">  <%-- packing these away here to be used by javascript --%>
            <input type="hidden" name="queryString" id="query-string" value="${queryString}"/>
            <input type="hidden" name="baseUrl" id="base-url" value="${baseUrl}"/>
            <input type="hidden" name="baseUrlWithoutQ" id="base-url-without-q" value="${baseUrlWithoutQ}"/>
            <input type="hidden" name="baseUrlWithoutPage" id="base-url-without-page" value="${baseUrlWithoutPage}"/>
            <input type="hidden" name="defaultAction" id="default-action" value="/prototype"/>
        </form>

        <script>


            $('.datepicker').datepicker({format: 'yyyy-mm-dd', autoclose: true});
                
        </script>

    </div>


    <div style="display: block; position: absolute; top: 125px; right: 50px; color: #666; font-size: 9px;">

            <authz:authorize access="hasRole('root')">
                <a href="${baseUrl}&explain=true">debug</a>
            </authz:authorize>

    </div>



    <zfin-search:feedbackModal/>


    <c:if test="${!empty message}">
        <div style="margin-top: 1em;" class="row">
            <div class="col-md-offset-2 col-md-6 alert alert-info">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                    ${message}
            </div>
        </div>
    </c:if>

    <c:if test="${isDashQuery}">
        <div style="margin-top: 1em;" class="row">
            <div class="col-md-offset-2 col-md-6 alert alert-info">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                Did you mean to search for <a href="#" onclick="javascript:replaceQuery('${newQuery}')">${newQuery}</a>?
                A leading dash means NOT.
            </div>
        </div>
    </c:if>

    <c:if test="${!empty suggestions}">
        <div style="margin-top: 1em;" class="row">
            <div class="col-md-offset-2 col-md-6 alert alert-info">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                Related:
                <c:forEach var="suggestion" items="${suggestions}" varStatus="loop">
                    <c:if test="${loop.last && !loop.first}"> or </c:if>
                    <a href="/search?q=${suggestion}">${suggestion}</a>
                    <c:if test="${!loop.last}"><span> </span></c:if>
                </c:forEach>
            </div>
        </div>
    </c:if>



    <div class="row">
        <zfin:horizontal-breadbox query="${query}" queryResponse="${response}" baseUrl="${baseUrlWithoutGalleryMode}"/>
    </div>


    <div class="row">

        <div class="col-md-3 col-sm-5 col-xs-6 refinement-section">
            <c:if test="${!empty facetGroups}">
                <zfin2:showFacets facetGroups="${facetGroups}"/>
            </c:if>
        </div>

        <div class="col-md-9 col-sm-7 col-xs-6">

            <c:if test="${showResults eq false}">
                <zfin-search:searchMessage/>
            </c:if>


            <div class="search-result-container">

                <c:if test="${!empty xrefResults}">
                    <div>Related Data for</div>
                    <div class="cross-reference-result-container">
                        <c:forEach var="result" items="${xrefResults}">
                            <zfin2:searchResult result="${result}"/>
                        </c:forEach>
                    </div>
                </c:if>

                <div class="row">
                    <div class="col-xs-12 center">
                        <div class="pull-left">
                            <c:if test="${!galleryMode}">
                                <a href="${downloadUrl}" class="btn btn-default">
                                    <i class="fa fa-download"></i> Download
                                </a>
                            </c:if>
                            <c:if test="${galleryMode}">
                                <a class="btn btn-default" href="${baseUrlWithoutGalleryMode}galleryMode=false">
                                    <i class="fa fa-chevron-left"></i>
                                    See all <fmt:formatNumber value="${numFound}" pattern="##,###"/><zfin:choice choicePattern="0# results| 1# result| 2# results" integerEntity="${numFound}"/>
                                </a>
                            </c:if>
                        </div>

                        <c:if test="${!galleryMode}">
                            <fmt:formatNumber value="${numFound}" pattern="##,###"/><zfin:choice choicePattern="0# results| 1# result| 2# results" integerEntity="${numFound}"/>
                        </c:if>
                        <c:if test="${galleryMode}">
                            <fmt:formatNumber value="${numImages}" pattern="##,###"/><zfin:choice choicePattern="0# images| 1# image| 2# images" integerEntity="${numImages}"/>
                        </c:if>

                        <c:if test="${!galleryMode}">
                            <div class="pull-right">
                                <authz:authorize access="hasRole('root')">
                                    <div class="btn-group">
                                        <button id="boxy-result-button" class="btn btn-default result-action-tooltip" title="Detailed Results">
                                            <i class="fa fa-newspaper-o fa-flip-horizontal"></i>
                                        </button>
                                        <button id="table-result-button" class="btn btn-default result-action-tooltip" title="Tabular Results">
                                            <i class="fa fa-table"></i>
                                        </button>
                                    </div>

                                    <div class="btn-group">
                                        <a href="${baseUrlWithoutRows}${rowsUrlSeparator}rows=20" class="btn btn-default <c:if test="${rows eq 20}">btn-selected disabled</c:if>">20</a>
                                        <a href="${baseUrlWithoutRows}${rowsUrlSeparator}rows=50" class="btn btn-default <c:if test="${rows eq 50}">btn-selected disabled</c:if>">50</a>
                                        <a href="${baseUrlWithoutRows}${rowsUrlSeparator}rows=200" class="btn btn-default <c:if test="${rows eq 200}">btn-selected disabled</c:if>">200</a>
                                    </div>
                                </authz:authorize>

                                <c:if test="${!empty images && !empty category && category != 'Any'}">
                                    <a href="${baseUrlWithoutGalleryMode}galleryMode=true" class="btn btn-default">
                                        <i class="fa fa-camera"></i> Browse Images
                                    </a>
                                </c:if>

                                <div class="btn-group sort-controls">
                                    <a class="btn btn-default dropdown-toggle sort-button" data-toggle="dropdown" href="#">
                                        Sorted ${sortDisplay}
                                        <span class="caret"></span>
                                    </a>
                                    <ul class="dropdown-menu">
                                        <li><a href="${baseUrlWithoutSort}">Relevance</a></li>
                                        <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=A+to+Z">A to Z</a></li>
                                        <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Z+to+A">Z to A</a></li>
                                        <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Newest">Newest</a></li>
                                        <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Oldest">Oldest</a></li>
                                    </ul>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>

                <c:if test="${!galleryMode}">
                    <c:forEach var="result" items="${results}">
                        <zfin2:searchResult result="${result}"/>
                    </c:forEach>

                    <c:choose>
                        <c:when test="${category eq geneCategoryName}">
                            <zfin-search:geneResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq expressionCategoryName}">
                            <zfin-search:expressionResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq phenotypeCategoryName}">
                            <zfin-search:phenotypeResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq diseaseCategoryName}">
                            <zfin-search:diseaseResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq mutationCategoryName}">
                            <zfin-search:mutationResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq constructCategoryName}">
                            <zfin-search:constructResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq strCategoryName}">
                            <zfin-search:strResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq abCategoryName}">
                            <zfin-search:antibodyResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq anatomyCategoryName}">
                            <zfin-search:anatomyResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq markerCategoryName}">
                            <zfin-search:cloneResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq publicationCategoryName}">
                            <zfin-search:publicationResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq communityCategoryName}">
                            <zfin-search:communityResultTable results="${results}"/>
                        </c:when>
                        <c:when test="${category eq figureCategoryName}">
                            <zfin-search:figureResultTable results="${results}"/>
                        </c:when>
                        <c:otherwise>
                            <zfin-search:mixedResultTable results="${results}"/>
                        </c:otherwise>
                    </c:choose>
                </c:if>

                <c:if test="${galleryMode}">
                    <c:if test="${!empty images}">
                        <div class="masonry figure-gallery-results-container clearfix">
                            <div class="figure-gallery-masonry-size"></div>
                            <c:forEach var="image" items="${images}" varStatus="loop">
                                <div class="figure-gallery-result-container figure-gallery-masonry-item"
                                     data-zdb-id="${image.zdbID}"
                                     data-category="${category}">
                                    <div class="figure-gallery-image-container gallery">
                                        <img src="${image.mediumUrl}">
                                        <div class="hidden figure-gallery-loading-overlay">
                                            <i class="fa fa-spinner fa-spin"></i>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:if>
                </c:if>

                <div style="clear: both ; width: 80%" class="clearfix">
                    <zfin2:pagination paginationBean="${paginationBean}"/>
                </div>

            </div>


        </div>
    </div>

    <div style="clear:both; width:100%; display:none;">
        <a style="clear:both; font-size: smaller;" href="#" onclick="$('.debug-output' ).slideToggle();">don't look
            at my debug output!</a>
    </div>
    <div class="debug-output"
         style="display:none; clear: both; background-color: pink ; border:5px solid magenta;">
        ${debug}
    </div>


</div>


<zfin-search:allFacetsModal/>

<div id="figureGalleryModal" class="figure-gallery-modal modal" tabindex="-1" role="dialog">
    <div class="modal-dialog"></div>
</div>


<script>


function submitAdvancedQuery(fields) {
    var query = "${baseUrlWithoutQ}";

    var mainQuery = $('#primary-query-input').val();

    if (mainQuery) {
        query = query + mainQuery;
    }


    for (var i = 0 ; i < fields.length ; i++ ) {

        if (fields[i].type == 'string') {
            var value = $('#' + fields[i].id).val();
            if (value) {
                query = query + "&fq=" + fields[i].field + ":(" + value + ")";
            }
        } else if (fields[i].type == 'date') {
            var start = $('#' + fields[i].startId).val();
            var end = $('#' + fields[i].endId).val();
            if (start != "" && end != "") {
                query = query + "&fq=" + fields[i].field + ':[' + start + 'T00:00:00Z' + ' TO ' + end + 'T00:00:00Z' + ']';
            }
        }
    }

    window.location = query;

}

$(function () {

    $('.list-collapse').listCollapse({label: 'results', itemsToShow: 3});

    $('#primary-query-input').autocompletify('/action/quicksearch/autocomplete?q=%QUERY');

    $('#primary-query-input').bind("typeahead:select", function() {
        $('#query-form').submit();
    });

    if (!${numFound}) {
        ga('send', 'event', 'Search', 'Zero Results', "<c:out value="${q}" escapeXml="true"/>", {'nonInteraction': 1});
    }

    // add GA click handlers for sort options
    $('.sort-controls .dropdown-menu a').click(function () {
        var category = '${empty category ? 'Any' : category}',
            label = $(this).text();
        ga('send', 'event', 'Search', 'Sort By', category + " : " + label);
    });

    $('.search-result-related-links a').click(function () {
        // send a Related Link event with the category and link text minus the number in parenthesis
        var category = $(this).closest(".search-result").find(".search-result-category").text().trim(),
            label = $(this).text().replace(/\s+\(\d+\)\s+$/g, "");
        ga('send', 'event', 'Search', 'Related Link', category + " : " + label);
    });

    //if this gets converted from tipsy to bootstrap, need to handle the jquery-ui collision:
    //http://stackoverflow.com/questions/13731400/jqueryui-tooltips-are-competing-with-twitter-bootstrap
    $('.facet-value-hover').tipsy({gravity: 'w'});
    $('.facet-include').tipsy({gravity: 'nw'});
    $('.facet-exclude').tipsy({gravity: 'sw'});
    $('.result-action-tooltip').tipsy({gravity: 's'});
    $('#advanced-search-button').tipsy({gravity: 'ne'});

    $('.result-explain-link').on('click', function () {
        $(this).closest(".search-result").find(".result-explain-container").slideToggle(50);
    });


    /* this is to get the background to not scroll behind the modals */
    $(".modal").on("show",function () {
        $("body").addClass("modal-open");
    }).on("hidden", function () {
        $("body").removeClass("modal-open");
        $('.modal-backdrop').remove();
    });


    /* this provides event handling for results elements that need a show/hide behavior because they're too long */
    $(".collapsible-attribute").click(function () {
        if ($(this).hasClass("collapsed-attribute")) {
            $(this).removeClass("collapsed-attribute");
        }
        else {
            $(this).addClass("collapsed-attribute");
        }
    });


    function showBoxyResults() {
        $('.boxy-search-result').show();
        $('.table-results').hide();
        $('#boxy-result-button').prop('disabled', true);
        $('#boxy-result-button').addClass('btn-selected');
        $('#boxy-result-button').tipsy('disable');
        $('#boxy-result-button').tipsy('hide');
        $('#table-result-button').prop('disabled', false);
        $('#table-result-button').removeClass('btn-selected');
        $('#table-result-button').tipsy('enable');
        localStorage.setItem("results-type","boxy");
    }
    function showTabularResults() {
        $('.boxy-search-result').hide();
        $('.table-results').show();
        $('#boxy-result-button').prop('disabled', false);
        $('#boxy-result-button').removeClass('btn-selected');
        $('#boxy-result-button').tipsy('enable');
        $('#table-result-button').prop('disabled', true);
        $('#table-result-button').addClass('btn-selected');
        $('#table-result-button').tipsy('disable');
        $('#table-result-button').tipsy('hide');
        localStorage.setItem("results-type","table");
    }


    $('#boxy-result-button').click(function() {
        showBoxyResults();
    });
    $('#table-result-button').click(function() {
        showTabularResults();
    });

    if(localStorage.getItem("results-type") == "table") {
        showTabularResults();
    } else {
        showBoxyResults();
    }

    var $figureGalleryModal = $('#figureGalleryModal');
    $figureGalleryModal.on('hidden.bs.modal', function () {
        $figureGalleryModal.find('.modal-dialog').empty();
    });

    <%-- do a little timeout to prevent lots of animating during resize --%>
    var resizeTimer;
    $(window).resize(function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(function () {
            $figureGalleryModal.figureGalleryResize();
        }, 250);
    });

    function shouldGalleryHeaderBeOpen() {
        var stored = window.localStorage.getItem('FigureGalleryDetails');
        return !stored || stored === 'open';
    }

    function toggleGalleryStorage() {
        window.localStorage.setItem('FigureGalleryDetails', shouldGalleryHeaderBeOpen() ? 'closed' : 'open');
    }

    function loadModal(el) {
        var $el = $(el);
        var loading = $el.find('.figure-gallery-loading-overlay').removeClass('hidden');
        var summaryUrl = '/action/image/' + $el.data('zdb-id') + '/summary' +
                '?category=' + encodeURIComponent($el.data('category')) +
                '&record=' + encodeURIComponent($el.data('result'));
        var prev = $el.prev('.figure-gallery-result-container');
        var next = $el.next('.figure-gallery-result-container');
        $.get(summaryUrl, function (data) {
            var content = $(data);
            var loader = content.find('.figure-gallery-modal-loader');
            $figureGalleryModal.modal();
            $(document).off('keydown.figuregallery').on('keydown.figuregallery', function (evt) {
                switch (evt.which) {
                    case 37: // left
                        if (prev.length) {
                            loader.removeClass('hidden');
                            loadModal(prev);
                        }
                        break;
                    case 39: // right
                        if (next.length) {
                            loader.removeClass('hidden');
                            loadModal(next);
                        }
                        break;
                    default:
                        return;
                }
                evt.preventDefault();
            });
            content.find('.figure-gallery-modal-nav.prev')
                    .toggleClass('hidden', prev.length === 0)
                    .click(function (evt) {
                        evt.preventDefault();
                        loader.removeClass('hidden');
                        loadModal(prev);
                    });
            content.find('.figure-gallery-modal-nav.next')
                    .toggleClass('hidden', next.length === 0)
                    .click(function (evt) {
                        evt.preventDefault();
                        loader.removeClass('hidden');
                        loadModal(next);
                    });
            content.find('.figure-gallery-modal-details').toggle(shouldGalleryHeaderBeOpen());
            content.find('.figure-gallery-modal-collapse')
                    .toggleClass('open', shouldGalleryHeaderBeOpen())
                    .click(function (evt) {
                        evt.preventDefault();
                        $(this).toggleClass('open');
                        toggleGalleryStorage();
                        content.find('.figure-gallery-modal-details').toggle();
                        $figureGalleryModal.figureGalleryResize();
                    });
            content.find('.figure-gallery-modal-image').on('load', function() {
                $figureGalleryModal.find('.modal-dialog').html(content);
                $figureGalleryModal.figureGalleryResize();
                loading.addClass('hidden');
            });
        });
    }

    $('.figure-gallery-result-container').on('click', function () {
        loadModal(this);
    });

    var figureGallery = $('.figure-gallery-results-container');
    figureGallery.imagesLoaded(function () {
        figureGallery.masonry({
            itemSelector: '.figure-gallery-masonry-item',
            columnWidth: '.figure-gallery-masonry-size',
            transitionDuration: 0
        });
    });

});
</script>
