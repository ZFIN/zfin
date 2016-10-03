<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>
<script src="/javascript/figure.service.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/inline-edit-textarea.directive.js"></script>
<script src="/javascript/figure-edit.directive.js"></script>
<script src="/javascript/file-input.driective.js"></script>
<script src="/javascript/figure-upload.directive.js"></script>
<script src="/javascript/figure-update.directive.js"></script>
<script src="/javascript/pub-file-edit.directive.js"></script>
<script src="/javascript/pub-file-upload.directive.js"></script>
<script src="/javascript/publication.service.js"></script>
<script src="/javascript/zfinutils.service.js"></script>

<c:set var="linkURL">/cgi-bin/webdriver?MIval=aa-link_authors.apg&OID=${publication.zdbID}&anon1=zdb_id&anon1text=${publication.zdbID}</c:set>
<c:set var="trackURL">/action/publication/${publication.zdbID}/track</c:set>
<c:if test="${allowCuration}">
    <c:set var="curateURL">/cgi-bin/webdriver?MIval=aa-curation.apg&OID=${publication.zdbID}</c:set>
</c:if>


<div class="container-fluid">
    <zfin2:dataManager zdbID="${publication.zdbID}"
                       linkURL="${linkURL}"
                       trackURL="${trackURL}"
                       curateURL="${curateURL}"
                       rtype="publication"/>

    <div class="row">
        <div class="col-xs-12">
            <h2>Editing ${publication.zdbID}</h2>
        </div>
    </div>

    <ul id="fig-edit-tabs" class="nav nav-tabs nav-justified" role="tablist">
        <li role="presentation" class="active"><a href="#details" aria-controls="details" role="tab" data-toggle="tab">Details</a></li>
        <li role="presentation"><a href="#files" aria-controls="files" role="tab" data-toggle="tab">Files</a></li>
        <li role="presentation"><a href="#figures" aria-controls="figures" role="tab" data-toggle="tab">Figures</a></li>
    </ul>

    <div class="tab-content edit-form-content" ng-app="app">
        <div role="tabpanel" class="tab-pane active" id="details">
            <zfin2:publicationForm publication="${publication}" error="${error}"/>
        </div>
        <div role="tabpanel" class="tab-pane" id="files">
            <div pub-file-edit pub-id="${publication.zdbID}"></div>
        </div>
        <div role="tabpanel" class="tab-pane figure-edit-panel" id="figures">
            <div figure-edit pub-id="${publication.zdbID}"></div>
        </div>
    </div>

</div>

<script>
    $(function () {

        function goToTab(hash) {
            $('#fig-edit-tabs a[href=' + hash + ']').tab('show');
        }

        var hash = window.location.hash;
        if (hash) {
            goToTab(hash);
        }

        $('#fig-edit-tabs a[data-toggle="tab"]').on('shown.bs.tab', function(e) {
            var href = $(e.target).attr('href');
            if (history.pushState) {
                history.pushState(null, null, href);
            } else {
                location.hash = href;
            }
        });

        $('.edit-form-content').on('click', "a[href^='#']", function () {
            var hash = $(this).attr('href');
            goToTab(hash);
        });
    });
</script>

