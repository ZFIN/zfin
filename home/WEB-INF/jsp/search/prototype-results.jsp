<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>

<link rel=stylesheet type="text/css" href="/css/bootstrap/css/bootstrap.css">
<script type="text/javascript" src="/css/bootstrap/js/bootstrap.js"></script>


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
        <form id="query-form" method="get" action="/search" style="margin: 0px;">
            <div class="row">
                <div class="search-input-container offset1 span11">

                    <a data-remote="/action/quicksearch/message" data-target="#beta-message" data-toggle="modal"
                       class="clickable">
                        <span class="badge alert-success">Beta</span>
                    </a>


                    <select <%--class="input-medium" --%> name="category">
                        <option>Any</option>
                        <c:forEach items="${categories}" var="cat">
                            <option <c:if test="${cat eq category}">selected="selected"</c:if>>${cat}</option>
                        </c:forEach>
                    </select>
                    <input class="search-form-input input" style="width: 25em;" name="q" id="primary-query-input"
                           autocomplete="off" type="text" value="${q}"/>
                    <a href="#" id="search-box-clear-link" onclick="jQuery('#primary-query-input').val('');">&times;</a>

                    <div class="btn-group search-box-buttons">

                        <button type="submit" class="btn btn-primary">Go</button>

                        <a class="btn" href="/search?q=" onclick="localStorage.clear();">New</a>

                            <a  class="btn" href="http://wiki.zfin.org/display/general/ZFIN+Single+Box+Search+Help" target="newWindow">
                                &nbsp;<img src="/images/help.gif"/>&nbsp;</a>
                        <a class="btn  feedback-link" href="#"
                           onclick="jQuery('.feedback-box').slideToggle(50);">
                            Feedback</a>

                        <div id="helpContent" class="modal fade" style="max-width: 800px; width: 800px">
                            <div class="modal-body" style="max-width: 780px; width: 780px;">
                                <!-- remote content will be inserted here via jQuery load() -->
                            </div>
                        </div>
                    </div>
                </div>

            </div>
            <script>
                function replaceQuery(query) {
                    jQuery('#primary-query-input').val(query);
                    jQuery('#query-form').submit();
                }
            </script>
        </form>

        <form style="display:none;">  <%-- packing these away here to be used by javascript --%>
            <input type="hidden" name="queryString" id="query-string" value="${queryString}"/>
            <input type="hidden" name="baseUrl" id="base-url" value="${baseUrl}"/>
            <input type="hidden" name="baseUrlWithoutQ" id="base-url-without-q" value="${baseUrlWithoutQ}"/>
            <input type="hidden" name="baseUrlWithoutPage" id="base-url-without-page" value="${baseUrlWithoutPage}"/>
            <input type="hidden" name="defaultAction" id="default-action" value="/prototype"/>
        </form>

        <script>


            jQuery('#primary-query-input').autocomplete({
                source: function (request, response) {
                    jQuery.ajax({
                        url: '/action/quicksearch/autocomplete',
                        dataType: "json",
                        data: {
                            q: request.term /* ,
                             category : 'Gene'*/
                        },
                        success: function (data) {
                            response(data);
                        }
                    });
                },
                select: function (event, ui) {
                    var val = ui.item.value;

                    jQuery('#primary-query-input').val(val);
                    var form = jQuery(this).parents('form:first');
                    jQuery("#query-form").submit();

                },
                minLength: 1, delay: 50,
                open: function (event, ui) {
                    jQuery("ul.ui-autocomplete li a").each(function () {
                        var htmlString = jQuery(this).html().replace(/&lt;/g, '<');
                        htmlString = htmlString.replace(/&gt;/g, '>');
                        jQuery(this).html(htmlString);
                    });
                }
            }, {});


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
                    <div class="span8 result-count">
                        <fmt:formatNumber value="${numFound}" pattern="##,###"/>
                        results

                    </div>
                    <div class="span4 sort-controls">
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

                <div style="clear: both ; width: 80%">
                    <zfin2:pagination paginationBean="${paginationBean}"/>
                </div>


            </div>


        </div>
    </div>

    <div style="clear:both; width:100%; display:none;">
        <a style="clear:both; font-size: smaller;" href="#" onclick="jQuery('.debug-output' ).slideToggle();">don't look
            at my debug output!</a>
    </div>
    <div class="debug-output"
         style="display:none; clear: both; background-color: pink ; border:5px solid magenta; display: none">
        ${debug}
    </div>


</div>


<zfin-search:allFacetsModal/>


<script>

function resetYourInputWelcome() {
    jQuery(".your-input-welcome-form input[type=text]").val('');
    jQuery(".your-input-welcome-form textarea").val('');
}

function scrollToAnchor(aid) {
    aid = aid.replace("#", "");
    var aTag = jQuery("a[name='" + aid + "']");
    jQuery('html,body').animate({scrollTop: aTag.offset().top}, 'fast');
}

function toggleLocalStorage(field) {
    if (localStorage.getItem(field) == null || localStorage.getItem(field) == "closed") {
        localStorage.setItem(field, "open");
    } else {
        localStorage.setItem(field, "closed");
    }
}

jQuery(document).ready(function () {

    if (!${numFound}) {
        ga('send', 'event', 'Search', 'Zero Results', '${q}', {'nonInteraction': 1});
    }

    // add GA click handlers for sort options
    jQuery('.sort-controls .dropdown-menu a').click(function () {
        var category = '${empty category ? 'Any' : category}',
            label = jQuery(this).text();
        ga('send', 'event', 'Search', 'Sort By', category + " : " + label);
    });

    jQuery('.search-result-related-links a').click(function () {
        // send a Related Link event with the category and link text minus the number in parenthesis
        var category = jQuery(this).closest(".search-result").find(".search-result-category").text().trim(),
            label = jQuery(this).text().replace(/\s+\(\d+\)\s+$/g, "");
        ga('send', 'event', 'Search', 'Related Link', category + " : " + label);
    });

    //if this gets converted from tipsy to bootstrap, need to handle the jquery-ui collision:
    //http://stackoverflow.com/questions/13731400/jqueryui-tooltips-are-competing-with-twitter-bootstrap
    jQuery('.facet-value-hover').tipsy({gravity: 'w'});
    jQuery('.facet-include').tipsy({gravity: 'nw'});
    jQuery('.facet-exclude').tipsy({gravity: 'sw'});

    jQuery('.result-explain-link').on('click', function () {
        jQuery(this).closest(".search-result").find(".result-explain-container").slideToggle(50);
    });


    /* this is to get the background to not scroll behind the modals */
    jQuery(".modal").on("show",function () {
        jQuery("body").addClass("modal-open");
    }).on("hidden", function () {
        jQuery("body").removeClass("modal-open")
        jQuery('.modal-backdrop').remove();
    });


    /* this provides event handling for results elements that need a show/hide behavior because they're too long */
    jQuery(".collapsible-attribute").click(function () {
        if (jQuery(this).hasClass("collapsed-attribute")) {
            jQuery(this).removeClass("collapsed-attribute");
        }
        else {
            jQuery(this).addClass("collapsed-attribute");
        }
    });


    jQuery('#feedback-form').validate({
        submitHandler: function (form) {
            jQuery.post('/cgi-bin/input_welcome_generic.cgi',
                            jQuery('.your-input-welcome-form').serialize())
                    .done(function () {

                        jQuery('.secondary-action-box').hide();
                        jQuery('#feedback-thanks-message').show();
                        resetYourInputWelcome();

                    })
                    .error(function () {
                        jQuery('#feedback-error-message').show();
                    });
            return true;
        }

    });

    jQuery('#help-contents').validate({
        submitHandler: function (form) {
            jQuery.get('/webapp/wiki/view/general/ZFIN+Single+Box+Search+Help')
                    .done(function () {

                        jQuery('.secondary-action-box').hide();
                        jQuery('#feedback-thanks-message').show();
                        resetYourInputWelcome();

                    })
                    .error(function () {
                        jQuery('#feedback-error-message').show();
                    });
            return true;
        }

    });


    jQuery('.cross-reference-button').click(function () {
        // Only call notifications when opening the dropdown
        if (!jQuery(this).hasClass('open')) {
            var list = jQuery(this).siblings('ul');
            var query = jQuery(this).attr('query');
            var category = jQuery(this).attr('category');
            list.html('');
            list.append('<li><img src="/images/ajax-loader.gif"></li>');
            jQuery.getJSON('/action/quicksearch/cross-reference',
                    {
                        term: query,
                        category: category
                    },
                    function (data) {
                        list.html('<li style="color: #666; margin-left:1em;margin-right:1em;">This entry has references in...</li>');


                        jQuery.each(data, function () {
                            var url = "/prototype?" + "fq=" + this.fq + "&fq=xref%3A%22" + query + "%22";
                            var link = '<li><a href="' + url + '">' + this.label + '</a></li>';
                            list.append(link);
                        });

                        if (data.length == 0)
                            list.append('<li style="opacity: .5; padding-left: 1em ; padding-right: 1em">None Found</li>');
                    }
            );

        }
    });

    if (!(localStorage.getItem("draft-software-warning-dismissed") == "true"))
        jQuery('#draft-software-warning').modal('show');


    for (var i = 0; i < localStorage.length; i++) {
        var key = localStorage.key(i);
        var value = localStorage.getItem(localStorage.key(i));
        if (value == "open") {
            jQuery('.' + key + "-toggle").toggle();
        }

    }

    jQuery('.facet-filter-form').each(
            function () {
                var fieldName = jQuery(this).attr('facetfield');
                jQuery(this).html('<input placeholder="starts with..." autocomplete="off" type="text" id="' + fieldName + '-facet-filter-form-input" class="facet-filter-form-input search-query" name="' + fieldName + '"/>');

                //we don't want to submit this...
                jQuery(this).submit(function (e) {
                            /*if (!$('#search').val()) {*/
                            e.preventDefault();
                            /*}*/
                        }
                );
            });


    jQuery('.facet-filter-form-input').keyup(function () {
        var fieldName = jQuery(this).attr('name');
        jQuery.getJSON('/action/quicksearch/facet-autocomplete?' + jQuery('#query-string').val(),
                {
                    field: fieldName,
                    term: jQuery(this).val()
                },
                function (data) {
                    var outputDiv = "#" + fieldName + "-facet-value-container";
                    jQuery(outputDiv).html('');
                    jQuery(outputDiv).append('<ol>');

                    //todo: this should remove page=N from the url, but it doesn't yet
                    jQuery.each(data, function () {
                        /*
                         <li style="min-height:10px" class="facet-value row-fluid"><span style="min-height:10px" class="span9 selectable-facet-value"><a title="require in results" style="min-height:10px" class=" " href="/prototype?category=Gene&amp;fq=expression_anatomy_tf%3A%22brain%22"><img class="checkbox-icon" src="/images/icon-check-empty.png">brain</a></span><ul style="min-height:10px" class="facet-count-container span3 unstyled">
                         <li class="dropdown">
                         <a class="facet-count dropdown-toggle" data-toggle="dropdown" href="#">
                         (1612)        <b class="caret"></b>
                         </a>
                         <ul class="dropdown-menu">
                         <li><a href="/prototype?category=Gene&amp;fq=expression_anatomy_tf%3A%22brain%22">Require</a></li>
                         <li><a href="/prototype?category=Gene&amp;fq=-expression_anatomy_tf%3A%22brain%22">Exclude</a></li>
                         <li class="divider"></li>
                         <li><a target="_blank" href="/prototype?q=brain">Search for <strong>brain</strong> in New Window</a></li>
                         </ul>
                         </li>
                         </ul></li>


                         var link = '<li class="facet-value"><a href="'
                         + jQuery('#base-url').val() + '&fq='
                         + this.fq + '">' + this.name + '</a>'
                         + '<span class="facet-count">' + this.count + '</span>';

                         */
                        //this.fq  this.name  this.count  jQuery('#base-url').val() + '&fq='
                        var link = ' <li style="min-height:10px" class="facet-value row-fluid"><span style="min-height:10px" class="span9 selectable-facet-value"><a title="require in results" style="min-height:10px" class=" " '
                                + 'href="' + jQuery('#base-url').val() + '&fq=' + this.fq
                                + '"><img class="checkbox-icon" src="/images/icon-check-empty.png">' + this.name + '</a></span><ul style="min-height:10px" class="facet-count-container span3 unstyled">'
                                + '<li class="dropdown">'
                                + ' <a class="facet-count dropdown-toggle" data-toggle="dropdown" href="#">'
                                + '(' + this.count + ')'
                                + ' <b class="caret"></b>'
                                + '   </a>'
                                + ' <ul class="dropdown-menu">'
                                + '   <li><a href="' + jQuery('#base-url').val() + '&fq=' + this.fq + '">Require</a></li>'
                                + '    <li><a href="' + jQuery('#base-url').val() + '&fq=-' + this.fq + '">Exclude</a></li>'
                                + '    <li class="divider"></li>'
                                + '    <li><a target="_blank" href="/prototype?q=' + this.name + '">Search for <strong>' + this.name + '</strong> in New Window</a></li>'
                                + '  </ul>'
                                + '</li>'
                                + '</ul></li>';
                        jQuery(outputDiv).append(link);
                    });

                    jQuery(outputDiv).append('</ol>');

                }
        );
    });


});
;


</script>

</body>
</html>
