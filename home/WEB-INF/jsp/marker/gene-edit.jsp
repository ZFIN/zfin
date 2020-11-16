<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<z:page>
    <script src="${zfn:getAssetPath("angular.js")}"></script>

    <div ng-app="app" ng-controller="EditController as eControl" ng-init="eControl.editMarker()">

        <jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

        <c:set var="markerID">${formBean.marker.zdbID}</c:set>
        <c:set var="deleteURL">/action/infrastructure/deleteRecord/${markerID}</c:set>
        <c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${markerID}</c:set>
        <c:set var="viewURL">/${markerID}</c:set>

        <zfin2:dataManager zdbID="${markerID}"
                           deleteURL="none"
                           mergeURL="${mergeURL}"
                           viewURL="${viewURL}"
        />

        <zfin2:geneHead gene="${formBean.marker}" previousNames="${formBean.previousNames}"
                        soTerm="${formBean.zfinSoTerm}" geneDesc="${formBean.allianceGeneDesc}" userID="${formBean.user.zdbID}"/>

        <%-- gene ontology--%>
        <zfin2:geneOntology geneOntologyOnMarker="${formBean.geneOntologyOnMarkerBeans}" marker="${formBean.marker}"/>

        <%--SEGMENT (CLONE AND PROBE) RELATIONSHIPS--%>
        <zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}"
                                        marker="${formBean.marker}" title="MARKER RELATIONSHIPS" interactsWith="no"/>

        <%--SEQUENCE INFORMATION--%>
        <zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}"
                                                title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>

        <%--ORTHOLOGY--%>
        <zfin2:orthology marker="${formBean.marker}" showTitle="true"/>
    </div>
</z:page>
