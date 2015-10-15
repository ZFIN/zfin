<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/ortho-edit.js"></script>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${gene.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${gene.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${gene.zdbID}</c:set>

<zfin2:dataManager zdbID="${gene.zdbID}"
                   editURL="${editURL}"
                   deleteURL="none"
                   mergeURL="${mergeURL}"
                   rtype="marker"/>

<a href="${editURL}"><< Back to old edit page</a>

<div ng-app="app">
    <h1 id="orthology">Orthology</h1>
    <div ortho-edit gene="${gene.zdbID}"></div>
</div>
