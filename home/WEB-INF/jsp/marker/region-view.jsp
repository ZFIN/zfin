<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/table-collapse.js"></script>
<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/editMarker.js"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>
<script src="/javascript/curator-notes.directive.js"></script>
<script src="/javascript/public-note.directive.js"></script>
<script src="/javascript/field-error.service.js"></script>
<script src="/javascript/other-markers.directive.js"></script>
<script src="/javascript/gene-marker-relationship.directive.js"></script>
<script src="/javascript/marker.service.js"></script>
<script src="/javascript/autocompletify.directive.js"></script>

<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/ortho-edit.js"></script>
<script src="/javascript/inline-edit-textarea.directive.js"></script>


<div ng-app="app" ng-controller="EditController as eControl">
<authz:authorize access="hasRole('root')">
    <div ng-app="app" ng-controller="EditController as eControl" ng-init="init('${gene.name}','${gene.abbreviation}')">
</authz:authorize>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   mergeURL="${mergeURL}"
                   editMarker="true"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:geneHead gene="${formBean.marker}" previousNames="${formBean.previousNames}" userID="${formBean.user.zdbID}" soTerm="${formBean.zfinSoTerm}" />




<div id="mutant-info">
    <zfin2:mutantsInGene mutantsOnMarkerBean="${formBean.mutantOnMarkerBeans}" marker="${formBean.marker}"/>
</div>
<%--Antibodies
<zfin2:markerRelationshipsLightSingleType relationships="${formBean.relatedAntibodies}" marker="${formBean.marker}" title="ANTIBODIES" maxNumber="5"/>
--%>
<zfin2:phenotype phenotypeOnMarkerBean="${formBean.phenotypeOnMarkerBeans}" marker="${formBean.marker}"
                 webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>
<%--Constructs--%>
<zfin2:geneOntology geneOntologyOnMarker="${formBean.geneOntologyOnMarkerBeans}" marker="${formBean.marker}"/>
<%--Transcripts--%>
<zfin2:markerTranscriptSummary relatedTranscriptDisplay="${formBean.relatedTranscriptDisplay}"
                               title="TRANSCRIPTS" showAllTranscripts="true"/>

<zfin2:constructsWithSequences formBean="${formBean}"/>
<%--SEGMENT (CLONE AND PROBE) RELATIONSHIPS--%>

<zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}"
                                marker="${formBean.marker}" title="SEGMENT (CLONE AND PROBE) RELATIONSHIPS"/>
<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}"
                                        title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>


<%--other GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>
<%--SEQUENCE INFORMATION
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>
--%>
    <%--ORTHOLOGY--%>
    <zfin2:orthology marker="${formBean.marker}" showTitle="true"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<authz:authorize access="hasRole('root')">
    </div>
</authz:authorize>



