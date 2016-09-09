<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<link rel="stylesheet" type="text/css" href="/css/faceted-search.css">

<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>
<script src="/javascript/figure-gallery-resize.jquery.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>

<script src="/javascript/publication.service.js"></script>
<script src="/javascript/zfinutils.service.js"></script>

<script src="/javascript/timeago.filter.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>

<script src="/javascript/indexing-bin.directive.js"></script>

<div class="container-fluid" ng-app="app">
    <div indexing-bin
         user-id="${currentUser.zdbID}"
         current-status="${currentStatus.id}"
         next-status="${nextStatus.id}">
    </div>
</div>