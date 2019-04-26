<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("bootstrap.js")}"></script>
<script src="${zfn:getAssetPath("angular.js")}"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid" ng-app="app">
    <div curating-bin
         user-id="${currentUser.zdbID}"
         current-status="${currentStatus.id}"
         next-status="${nextStatus.id}">
    </div>
</div>