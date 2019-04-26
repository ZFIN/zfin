<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("bootstrap.js")}"></script>
<script src="${zfn:getAssetPath("angular.js")}"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid" ng-app="app">
    <div pub-dashboard user-id="${currentUser.zdbID}"></div>
</div>