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
<script src="/javascript/timeago.filter.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/pub-dashboard.directive.js"></script>

<zfin2:pub-navigator pages="${page}" currentPage="${currentPage}"/>

<div class="container-fluid" ng-app="app">
    <div pub-dashboard user-id="${currentUser.zdbID}"></div>
</div>