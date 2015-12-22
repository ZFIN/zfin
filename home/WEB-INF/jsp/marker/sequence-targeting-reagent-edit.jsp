<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="str" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>

<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script>
    ;(function() {
        angular.module('app', []);
    }());
</script>

<script src="/javascript/field-error.service.js"></script>
<script src="/javascript/str.service.js"></script>
<script src="/javascript/marker.service.js"></script>

<script src="/javascript/pub-lookup.directive.js"></script>
<script src="/javascript/bootstrap-modal.directive.js"></script>
<script src="/javascript/reference-editor.directive.js"></script>
<script src="/javascript/marker-suppliers.directive.js"></script>
<script src="/javascript/str-details.directive.js"></script>
<script src="/javascript/str-sequence.directive.js"></script>
<script src="/javascript/marker-aliases.directive.js"></script>
<script src="/javascript/marker-relationships.directive.js"></script>
<script src="/javascript/marker-notes.directive.js"></script>
<script src="/javascript/marker-links.directive.js"></script>

<c:set var="viewURL">/${str.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${str.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${str.zdbID}</c:set>

<zfin2:dataManager zdbID="${str.zdbID}"
                   viewURL="${viewURL}"
                   deleteURL="${deleteURL}"
                   mergeURL="${mergeURL}"
                   rtype="marker"/>

<div class="container-fluid" ng-app="app">
    <h2 class="page-header">Editing ${str.zdbID}</h2>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">STR Details</h3>
        </div>
        <div class="panel-body">
            <div str-details marker-id="${str.zdbID}" type="${str.type}"></div>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Aliases</h3>
        </div>
        <div class="panel-body">
            <div marker-aliases marker-id="${str.zdbID}" name="${str.abbreviation}"></div>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Target Genes</h3>
        </div>
        <div class="panel-body">
            <div marker-relationships
                 marker-id="${str.zdbID}"
                 relationship="knockdown reagent targets gene"
                 relative-name="target gene"></div>
        </div>
    </div>

    <c:if test="${showSupplier}">
        <div class="panel panel-default">
            <div class="panel-heading">
                <h3 class="panel-title">Suppliers</h3>
            </div>
            <div class="panel-body">
                <div marker-suppliers marker-id="${str.zdbID}"></div>
            </div>
        </div>
    </c:if>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Notes</h3>
        </div>
        <div class="panel-body">
            <div marker-notes marker-id="${str.zdbID}" user-id="${user.zdbID}"></div>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h3 class="panel-title">Other ${str.name} Pages</h3>
        </div>
        <div class="panel-body">
            <div marker-links marker-id="${str.zdbID}" group="summary page"></div>
        </div>
    </div>
</div>
