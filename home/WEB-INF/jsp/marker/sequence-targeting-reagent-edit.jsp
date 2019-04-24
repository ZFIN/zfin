<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="str" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>

<link rel="stylesheet" href="/javascript/dist/bootstrap.bundle.css">

<script src="/javascript/dist/bootstrap.bundle.js"></script>
<script src="/javascript/dist/angular.bundle.js"></script>

<c:set var="viewURL">/${str.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${str.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${str.zdbID}</c:set>

<zfin2:dataManager zdbID="${str.zdbID}"
                   viewURL="${viewURL}"
                   deleteURL="${deleteURL}"
                   mergeURL="${mergeURL}"/>

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
