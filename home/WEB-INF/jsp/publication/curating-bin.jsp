<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<link rel="stylesheet" type="text/css" href="/css/faceted-search.css">

<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/dist/angular.bundle.js"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid" ng-app="app">
    <div curating-bin
         user-id="${currentUser.zdbID}"
         current-status="${currentStatus.id}"
         next-status="${nextStatus.id}">
    </div>
</div>