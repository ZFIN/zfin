<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="str" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>

<script src="/javascript/angular/angular.min.js"></script>
<script>
    ;(function() {
        angular.module('app', []);
    }());
</script>

<script src="/javascript/str.service.js"></script>
<script src="/javascript/str-details.directive.js"></script>
<script src="/javascript/str-sequence.directive.js"></script>

<c:set var="viewURL">/${str.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${str.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${str.zdbID}</c:set>

<zfin2:dataManager zdbID="${str.zdbID}"
                   viewURL="${viewURL}"
                   deleteURL="${deleteURL}"
                   mergeURL="${mergeURL}"
                   rtype="marker"/>

<div class="container-fluid" ng-app="app">
    <h1 class="page-header">Editing ${str.zdbID}</h1>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">STR Details</h3>
        </div>
        <div class="panel-body">
            <div str-details id="${str.zdbID}" type="${str.type}"></div>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Previous Names</h3>
        </div>
        <div class="panel-body">Oak is strong and also gives shade</div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Target Genes</h3>
        </div>
        <div class="panel-body">Cats and dogs each hate the other</div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Suppliers</h3>
        </div>
        <div class="panel-body">The pipe began to rust while new</div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Notes</h3>
        </div>
        <div class="panel-body">Open the crate but don't break the glass</div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Other ${str.name} Pages</h3>
        </div>
        <div class="panel-body">Add the sum to the product of these three</div>
    </div>
</div>
