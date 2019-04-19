<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">

<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/dist/angular.bundle.js"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid" ng-app="app">
    <div pub-dashboard user-id="${currentUser.zdbID}"></div>
</div>