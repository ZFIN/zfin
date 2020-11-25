<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="str" class="org.zfin.mutant.SequenceTargetingReagent" scope="request"/>

<z:page bootstrap="true">
    <script src="${zfn:getAssetPath("angular.js")}"></script>

    <c:set var="viewURL">/${str.zdbID}</c:set>
    <c:set var="deleteURL">/action/infrastructure/deleteRecord/${str.zdbID}</c:set>
    <c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${str.zdbID}</c:set>

    <zfin2:dataManager zdbID="${str.zdbID}"
                       viewURL="${viewURL}"
                       deleteURL="${deleteURL}"
                       mergeURL="${mergeURL}"/>

    <div class="container-fluid" ng-app="app">
        <h2 class="page-header">Editing ${str.zdbID}</h2>

        <div class="card mb-3">
            <h5 class="card-header">STR Details</h5>
            <div class="card-body">
                <div str-details marker-id="${str.zdbID}" type="${str.type}"></div>
            </div>
        </div>

        <div class="card mb-3">
            <h5 class="card-header">Aliases</h5>
            <div class="card-body">
                <div marker-aliases marker-id="${str.zdbID}" name="${str.abbreviation}"></div>
            </div>
        </div>

        <div class="card mb-3">
            <h5 class="card-header">Target Genes</h5>
            <div class="card-body">
                <div marker-relationships
                     marker-id="${str.zdbID}"
                     relationship="knockdown reagent targets gene"
                     relative-name="target gene"></div>
            </div>
        </div>

        <c:if test="${showSupplier}">
            <div class="card mb-3">
                <h5 class="card-header">Suppliers</h5>
                <div class="card-body">
                    <div marker-suppliers marker-id="${str.zdbID}"></div>
                </div>
            </div>
        </c:if>

        <div class="card mb-3">
            <h5 class="card-header">Notes</h5>
            <div class="card-body">
                <div marker-notes marker-id="${str.zdbID}" user-id="${user.zdbID}"></div>
            </div>
        </div>

        <div class="card mb-3">
            <h5 class="card-header">Other ${str.name} Pages</h5>
            <div class="card-body">
                <div marker-links marker-id="${str.zdbID}" group="summary page"></div>
            </div>
        </div>
    </div>
</z:page>