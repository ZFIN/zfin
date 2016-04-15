<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.search.Category" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<c:set var="geneCategoryName" value="<%=Category.GENE.getName()%>"/>
<c:set var="publicationCategoryName" value="<%=Category.PUBLICATION.getName()%>"/>
<c:set var="constructCategoryName" value="<%=Category.CONSTRUCT.getName()%>"/>


<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>

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

<%-- placed this outside of the search container below so that it won't inherit odd css, content of modal is loaded via remote --%>

<!-- Modal -->
<div class="modal fade" id="beta-message" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
        </div>
    </div>
</div>



<div class="container-fluid">


    <div class="row">
        <div class="search-box col-md-offset-1 col-md-11">
            <form id="query-form" class="form-inline" method="get" action="/search">
                <div class="search-input-container">

                        <a data-remote="/action/quicksearch/message" data-target="#beta-message" data-toggle="modal"
                           class="clickable">
                            <span class="badge alert-success">Beta</span>
                        </a>


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
                <a href="${baseUrl}&hl=true">highlight</a>
                <a href="${baseUrl}&explain=true">debug</a>
            </authz:authorize>

    </div>



    <zfin-search:feedbackModal/>


    <c:if test="${!empty message}">
        <div style="margin-top: 1em;" class="row">
            <div class="col-md-offset-2 col-md-8 alert alert-info">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                    ${message}
            </div>
        </div>
    </c:if>

    <c:if test="${isDashQuery}">
        <div style="margin-top: 1em;" class="row">
            <div class="col-md-offset-2 col-md-8 alert alert-info">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                Did you mean to search for <a href="#" onclick="javascript:replaceQuery('${newQuery}')">${newQuery}</a>?
                A leading dash means NOT.
            </div>
        </div>
    </c:if>



    <div class="row">
        <zfin:horizontal-breadbox query="${query}" queryResponse="${response}" baseUrl="${baseUrl}"/>
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
                    <div class="col-md-2 col-sm-3 col-xs-4">
                        <a href="${downloadUrl}" class="btn btn-default">
                            <i class="fa fa-download"></i> Download
                        </a>
                    </div>
                    <div class="result-count col-md-10 col-sm-9 col-xs-8">
                        <fmt:formatNumber value="${numFound}" pattern="##,###"/> results
                        <div class="pull-right">
<%--                            <authz:authorize access="hasRole('root')">--%>
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
<%--                            </authz:authorize>--%>

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
                    </div>
                </div>

                <c:forEach var="result" items="${results}">
                    <zfin2:searchResult result="${result}"/>
                </c:forEach>


                <c:choose>
                    <c:when test="${category eq geneCategoryName}">
                        <zfin-search:geneResultTable results="${results}"/>
                    </c:when>
                    <c:otherwise>
                        <zfin-search:mixedResultTable results="${results}"/>
                    </c:otherwise>
                </c:choose>


                <div style="clear: both ; width: 80%">
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
         style="display:none; clear: both; background-color: pink ; border:5px solid magenta; display: none">
        ${debug}
    </div>


</div>


<zfin-search:allFacetsModal/>


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

$(document).ready(function () {

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
    })
    $('#table-result-button').click(function() {
        showTabularResults();
    })

    if(localStorage.getItem("results-type") == "table") {
        showTabularResults();
    } else {
        showBoxyResults();
    }

});



</script>

</body>
</html>
