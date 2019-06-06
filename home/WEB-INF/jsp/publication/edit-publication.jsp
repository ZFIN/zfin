<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("bootstrap.js")}"></script>
<script src="${zfn:getAssetPath("angular.js")}"></script>

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
            <div class="__react-root"
                 id="ProcessorApproval"
                 data-pub-id="${pubID}"
                 data-task="ADD_PDF"
            >
            </div>
        </div>
        <div role="tabpanel" class="tab-pane figure-edit-panel" id="figures">
            <div figure-edit pub-id="${pubID}"></div>
            <div class="__react-root"
                 id="ProcessorApproval"
                 data-pub-id="${pubID}"
                 data-task="ADD_FIGURES"
            >
            </div>
        </div>
    </div>

</div>

<script>
    $('#fig-edit-tabs').stickyTabs();
</script>

<script src="${zfn:getAssetPath("react.js")}"></script>