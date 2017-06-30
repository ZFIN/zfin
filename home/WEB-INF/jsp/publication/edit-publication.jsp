<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>
<script type="text/javascript" src="/javascript/jquery.stickytabs.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>
<script src="/javascript/figure.service.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/inline-edit-textarea.directive.js"></script>
<script src="/javascript/figure-edit.directive.js"></script>
<script src="/javascript/file-input.directive.js"></script>
<script src="/javascript/figure-upload.directive.js"></script>
<script src="/javascript/figure-update.directive.js"></script>
<script src="/javascript/pub-file-edit.directive.js"></script>
<script src="/javascript/pub-file-upload.directive.js"></script>
<script src="/javascript/publication.service.js"></script>
<script src="/javascript/zfinutils.service.js"></script>

<c:set var="pubID">${publicationBean.publication.zdbID}</c:set>
<c:set var="linkURL">/action/publication/${pubID}/link</c:set>
<c:set var="trackURL">/action/publication/${pubID}/track</c:set>
<c:if test="${allowCuration}">
    <c:set var="curateURL">/action/curation/${pubID}</c:set>
</c:if>
<c:if test="${hasCorrespondence}">
    <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
</c:if>

<div class="container-fluid">
    <zfin2:dataManager zdbID="${pubID}"
                       linkURL="${linkURL}"
                       trackURL="${trackURL}"
                       curateURL="${curateURL}"
                       correspondenceURL="${correspondenceURL}"
                       viewURL="/${pubID}"/>

    <div class="row">
        <div class="col-xs-12">
            <h2>Editing ${pubID}</h2>
        </div>
    </div>

    <ul id="fig-edit-tabs" class="nav nav-tabs nav-justified nav-padded" role="tablist">
        <li role="presentation" class="active"><a href="#details" aria-controls="details" role="tab" data-toggle="tab">Details</a></li>
        <li role="presentation"><a href="#files" aria-controls="files" role="tab" data-toggle="tab">Files</a></li>
        <li role="presentation"><a href="#figures" aria-controls="figures" role="tab" data-toggle="tab">Figures</a></li>
    </ul>

    <div class="tab-content" ng-app="app">
        <div role="tabpanel" class="tab-pane active" id="details">
            <zfin2:publicationForm publicationBean="${publication}" error="${error}"/>
        </div>
        <div role="tabpanel" class="tab-pane" id="files">
            <div pub-file-edit pub-id="${pubID}"></div>
        </div>
        <div role="tabpanel" class="tab-pane figure-edit-panel" id="figures">
            <div figure-edit pub-id="${pubID}"></div>
        </div>
    </div>

</div>

<script>
    $('#fig-edit-tabs').stickyTabs();
</script>

