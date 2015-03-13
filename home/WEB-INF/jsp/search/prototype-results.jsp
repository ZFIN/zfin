<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.search.Category" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<c:set var="publicationCategoryName" value="<%=Category.PUBLICATION.getName()%>"/>
<c:set var="constructCategoryName" value="<%=Category.CONSTRUCT.getName()%>"/>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>

<link rel=stylesheet type="text/css" href="/css/bootstrap/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap/js/bootstrap.js"></script>

<%-- Bootstrap 2.3.2 comes with it's own typeahead, we don't want it, this removes it --%>
<script>
    $.fn.bootstrapTypeahead = $.fn.typeahead.noConflict();
</script>
<link rel=stylesheet type="text/css" href="/css/datepicker.css">
<script type="text/javascript" src="/javascript/bootstrap-datepicker.js"></script>

<script src="/javascript/purl.js"></script>
<script src="/javascript/jquery.validate.min.js"></script>

<link rel=stylesheet type="text/css" href="/css/faceted-search.css">

<style>
    .ui-menu-item {
        font-size: small
    }

    /* necessary because bootstrap overrides our body margin on top */
    body {
        margin-top: 90px;
    }

    /* don't need the left-right magin fixes from all-content */
    div.allcontent {
        margin: 0px;
    }

    /* remove parts of the header that we don't need */
    /* #hdr-tabs { display: none; } */
    #hdr-tabs {
        line-height: 15px;
    }

    #hdr-navlinks {
        line-height: 15px;
        height: 15px;
    }

    #quicksearchBox {
        display: none
    }

    #feedBox #rss-icon {
        position: absolute;
        top: 1px;
        right: 64px;
    }

</style>

<script>
    showMotto();
</script>

<%-- placed this outside of the search container below so that it won't inherit odd css --%>
<div id="beta-message" class="modal hide fade" tabindex="-1" role="dialog"
     aria-labelledby="myModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        Welcome to the new ZFIN Search (Beta)!
    </div>
    <div class="modal-body">
        <img src="/images/ajax-loader.gif"/>
    </div>
    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
    </div>
</div>

<div class="row-fluid">
    <div class="search-box span12">
        <form id="query-form" class="form-inline" method="get" action="/search">
            <div class="row">
                <div class="search-input-container offset1 span11">

                    <a data-remote="/action/quicksearch/message" data-target="#beta-message" data-toggle="modal"
                       class="clickable">
                        <span class="badge alert-success">Beta</span>
                    </a>


                    <select name="category">
                        <option>Any</option>
                        <c:forEach items="${categories}" var="cat">
                            <option <c:if test="${cat eq category}">selected="selected"</c:if>>${cat}</option>
                        </c:forEach>
                    </select>
                            <input class="search-form-input input input-xxlarge" name="q" id="primary-query-input"
                                   autocomplete="off" type="text" value="<c:out value="${q}" escapeXml="true"/>"/>
                    <div class="btn-group search-box-buttons">
                        <authz:authorize ifAnyGranted="root">
                            <c:if test="${category eq publicationCategoryName}">
                                <a id="advanced-search-button" class="btn" href="#" title="Advanced Search Options"
                                   onClick="$('#advanced-container').slideToggle(200);"><i class="fa fa-list"></i></a>
                            </c:if>
                        </authz:authorize>
                        <button type="submit" class="btn btn-zfin">Go</button>
                        <a class="btn" href="/search?q=" onclick="localStorage.clear();">New</a>
                        <a  class="btn" href="http://wiki.zfin.org/display/general/ZFIN+Single+Box+Search+Help" target="newWindow">
                            <i class="fa fa-question-circle"></i>
                        </a>
                        <a class="btn feedback-link" href="#">Feedback</a>
                    </div>
                </div>

            </div>
            <script>
                function replaceQuery(query) {
                    $('#primary-query-input').val(query);
                    $('#query-form').submit();
                }
            </script>
        </form>

        <div class="container-fluid" id="advanced-container" style="display:none;"  >
            <div class="row-fluid" >
                <div class="span10 offset1">
                    <c:choose>
                        <c:when test="${category eq publicationCategoryName}">
                            <zfin-search:publicationAdvanced/>
                        </c:when>
                    </c:choose>


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

        <authz:authorize ifAnyGranted="root">
            <a href="${baseUrl}&hl=true">highlight</a>
            <a href="${baseUrl}&explain=true">debug</a>
        </authz:authorize>

    </div>

</div>

<zfin-search:feedbackModal/>


<c:if test="${!empty message}">
    <div style="margin-top: 1em;" class="row">
        <div class="offset2 span8 alert alert-info">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message}
        </div>
    </div>
</c:if>

<c:if test="${isDashQuery}">
    <div style="margin-top: 1em;" class="row">
        <div class="offset2 span8 alert alert-info">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
            Did you mean to search for <a href="#" onclick="javascript:replaceQuery('${newQuery}')">${newQuery}</a>?
            A leading dash means NOT.
        </div>
    </div>
</c:if>

<div style="margin: .5em ; padding-left: 0px ; padding-right: 0px ; min-width: 700px;" class="container-fluid">


    <div class="row-fluid">
        <zfin:horizontal-breadbox query="${query}" queryResponse="${response}" baseUrl="${baseUrl}"/>
    </div>

    <div class="row-fluid">

        <div class="span4 refinement-section">
            <c:if test="${!empty facetGroups}">
                <zfin2:showFacets facetGroups="${facetGroups}"/>
            </c:if>
        </div>

        <div style="margin-left: .5em ; " class="span8">

            <c:if test="${showResults eq false}">
                <zfin-search:searchMessage/>
            </c:if>


            <div class="search-result-container row">

                <c:if test="${!empty xrefResults}">
                    <div>Related Data for</div>
                    <div class="cross-reference-result-container">
                        <c:forEach var="result" items="${xrefResults}">
                            <zfin2:searchResult result="${result}"/>
                        </c:forEach>
                    </div>
                </c:if>

                <div class="row" style="margin-top: 1em;">
                    <div class="span3 result-actions">
                        <div class="result-actions">
                            <a href="${downloadUrl}" class="btn btn-default">
                                <i class="fa fa-download"></i> Download
                            </a>
                        </div>
                    </div>
                    <div class="span2 result-count">
                        <fmt:formatNumber value="${numFound}" pattern="##,###"/>
                        results
                    </div>
                    <div class="span7 sort-controls pull-right">

                        <authz:authorize ifAnyGranted="root">
                            <div class="btn-group">
                                <button id="boxy-result-button" class="btn result-action-tooltip" title="Detailed Results">
                                    <i class="fa fa-newspaper-o fa-flip-horizontal"></i>
                                </button>
                                <button id="table-result-button" class="btn result-action-tooltip" title="Tabular Results">
                                    <i class="fa fa-table"></i>
                                </button>
                            </div>

                            <div class="btn-group">
                                <a href="${baseUrlWithoutRows}${rowsUrlSeparator}rows=20" class="btn <c:if test="${rows eq 20}">disabled</c:if>">20</a>
                                <a href="${baseUrlWithoutRows}${rowsUrlSeparator}rows=50" class="btn <c:if test="${rows eq 50}">disabled</c:if>">50</a>
                                <a href="${baseUrlWithoutRows}${rowsUrlSeparator}rows=200" class="btn <c:if test="${rows eq 200}">disabled</c:if>">200</a>
                            </div>
                        </authz:authorize>

                        <div class="btn-group">
                            <a class="btn dropdown-toggle sort-button" data-toggle="dropdown" href="#">
                                Sorted ${sortDisplay}
                                <span class="caret"></span>
                            </a>
                            <ul class="dropdown-menu">
                                <li><a href="${baseUrlWithoutSort}">Relevance</a></li>
                                <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=A+to+Z">A to Z</a></li>
                                <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Z+to+A">Z to A</a></li>
                                <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Newest">Newest</a></li>
                                <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Oldest">Oldest</a></li>
                                <%--                                <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Most+Attributed">Most
                                                                    Attributed</a></li>
                                                                <li><a href="${baseUrlWithoutSort}${sortUrlSeparator}sort=Least+Attributed">Least
                                                                    Attributed</a></li>--%>


                            </ul>
                        </div>

                    </div>
                </div>

                <c:forEach var="result" items="${results}">
                    <zfin2:searchResult result="${result}"/>
                </c:forEach>

                <table class="table-results searchresults" style="display: none;">
                    <th>Name</th> <th>ID</th> <th>Category</th>
                    <c:forEach var="result" items="${results}" varStatus="loop">
                        <zfin:alternating-tr loopName="loop" groupBeanCollection="${results}" groupByBean="result.id">
                            <td>${result.link}</td>
                            <td style="white-space: nowrap"> <c:if test="${!empty result.displayedID}">${result.id}</c:if> </td>
                            <td>${result.category}</td>
                        </zfin:alternating-tr>
                    </c:forEach>
                </table>


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

function scrollToAnchor(aid) {
    aid = aid.replace("#", "");
    var aTag = $("a[name='" + aid + "']");
    $('html,body').animate({scrollTop: aTag.offset().top}, 'fast');
}

function toggleLocalStorage(field) {
    if (localStorage.getItem(field) == null || localStorage.getItem(field) == "closed") {
        localStorage.setItem(field, "open");
    } else {
        localStorage.setItem(field, "closed");
    }
}

$(document).ready(function () {
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

    $('#primary-query-input').bind("typeahead:selected", function() {
        $('#query-form').submit();
    });
})

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

    if (!(localStorage.getItem("draft-software-warning-dismissed") == "true"))
        $('#draft-software-warning').modal('show');

    function showBoxyResults() {
        $('.boxy-search-result').show();
        $('.table-results').hide();
        $('#boxy-result-button').prop('disabled', true);
        $('#boxy-result-button').tipsy('disable');
        $('#boxy-result-button').tipsy('hide');
        $('#table-result-button').prop('disabled', false);
        $('#table-result-button').tipsy('enable');
        localStorage.setItem("results-type","boxy");
    }
    function showTabularResults() {
        $('.boxy-search-result').hide();
        $('.table-results').show();
        $('#boxy-result-button').prop('disabled', false);
        $('#boxy-result-button').tipsy('enable');
        $('#table-result-button').prop('disabled', true);
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


    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        var value = localStorage.getItem(localStorage.key(i));
        if (value == "open") {
            $('.' + key + "-toggle").toggle();
        }

    }


});
;


</script>

</body>
</html>
