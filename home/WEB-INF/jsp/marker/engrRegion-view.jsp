<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="${zfn:getAssetPath("angular.js")}"></script>

<authz:authorize access="hasRole('root')">
    <div ng-app="app" ng-controller="EditController as eControl" ng-init="init('${gene.name}','${gene.abbreviation}')">
</authz:authorize>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   deleteURL="${deleteURL}"
                   editMarker="true"/>

<zfin2:markerHead marker="${formBean.marker}" previousNames="${formBean.previousNames}" showEditControls="true" userID="${formBean.user.zdbID}" />

<%--// EXPRESSION SECTION
<zfin2:markerExpression marker="${formBean.marker}" markerExpression="${formBean.markerExpression}"/>
--%>

<%--Antibodies
<zfin2:markerRelationshipsLightSingleType relationships="${formBean.relatedAntibodies}" marker="${formBean.marker}" title="ANTIBODIES" maxNumber="5"/>
--%>

<%--Constructs--%>
<zfin2:constructsWithSequences formBean="${formBean}"/>

<%--SEQUENCE INFORMATION
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>
--%>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<authz:authorize access="hasRole('root')">
    </div>
</authz:authorize>



