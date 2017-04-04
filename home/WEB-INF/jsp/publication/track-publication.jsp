<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>

<script src="/javascript/intertab-event.service.js"></script>
<script src="/javascript/publication.service.js"></script>
<script src="/javascript/zfinutils.service.js"></script>

<script src="/javascript/publication-author-notif.directive.js"></script>
<script src="/javascript/publication-correspondence.directive.js"></script>
<script src="/javascript/publication-notes.directive.js"></script>
<script src="/javascript/publication-status.directive.js"></script>
<script src="/javascript/publication-topics.directive.js"></script>
<script src="/javascript/publication-tracker.directive.js"></script>
<script src="/javascript/select-all-list.directive.js"></script>

<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>

<c:set var="linkURL">/action/publication/${publication.zdbID}/link</c:set>

<c:if test="${allowCuration}">
  <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
</c:if>

<div class="container-fluid" ng-app="app">
  <zfin2:dataManager zdbID="${publication.zdbID}"
                     editURL="${editURL}"
                     linkURL="${linkURL}"
                     curateURL="${curateURL}"/>

  <p class="lead">
    <a href="/${publication.zdbID}">${publication.title}</a>
    <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="fa fa-file-pdf-o"></i></a></c:if>
  </p>

  <div>
    <ul class="nav nav-tabs nav-justified nav-padded" role="tablist">
      <li role="presentation" class="active"><a href="#status" aria-controls="status" role="tab" data-toggle="tab">Status</a></li>
      <li role="presentation"><a href="#correspondence" aria-controls="correspondence" role="tab" data-toggle="tab">Correspondence</a></li>
    </ul>

    <div class="tab-content">
      <div role="tabpanel" class="tab-pane active" id="status">
        <div publication-tracker
             pub-id="${publication.zdbID}"
             curator-id="${loggedInUser.zdbID}"
             curator-first="${loggedInUser.firstName}"
             curator-last="${loggedInUser.lastName}"
             curator-email="${loggedInUser.email}">
        </div>
      </div>
      <div role="tabpanel" class="tab-pane" id="correspondence">
        <div publication-correspondence pub-id="${publication.zdbID}" curator-id="${loggedInUser.zdbID}" curator-email="${loggedInUser.email}"></div>
      </div>
    </div>
  </div>

</div>
