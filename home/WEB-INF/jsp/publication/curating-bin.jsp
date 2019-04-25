<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="/javascript/dist/bootstrap.bundle.css">

<script src="/javascript/dist/bootstrap.bundle.js"></script>
<script src="/javascript/dist/angular.bundle.js"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid" ng-app="app">
    <div curating-bin
         user-id="${currentUser.zdbID}"
         current-status="${currentStatus.id}"
         next-status="${nextStatus.id}">
    </div>
</div>