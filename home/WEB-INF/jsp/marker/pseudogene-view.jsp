<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/table-collapse.js"></script>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>

<script src="/javascript/editMarker.js"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>
<script src="/javascript/curator-notes.directive.js"></script>
<script src="/javascript/public-note.directive.js"></script>
<script src="/javascript/gene-marker-relationship.directive.js"></script>
<script src="/javascript/other-markers.directive.js"></script>
<script src="/javascript/marker.service.js"></script>

<authz:authorize access="hasRole('root')">
    <div ng-app="app" ng-controller="EditController as eControl">
</authz:authorize>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="none"
                   mergeURL="${mergeURL}"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:geneHead gene="${formBean.marker}" previousNames="${formBean.previousNames}"/>

<%--// EXPRESSION SECTION--%>
<zfin2:markerExpression marker="${formBean.marker}" markerExpression="${formBean.markerExpression}" webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<zfin2:mutantsInGene mutantsOnMarkerBean="${formBean.mutantOnMarkerBeans}" marker="${formBean.marker}"/>
<%--Transcripts--%>
<zfin2:markerTranscriptSummary relatedTranscriptDisplay="${formBean.relatedTranscriptDisplay}"
                               title="TRANSCRIPTS" showAllTranscripts="true" />


<%--SEGMENT (CLONE AND PROBE) RELATIONSHIPS--%>
<zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}" marker="${formBean.marker}"
                                title="SEGMENT (CLONE AND PROBE) RELATIONSHIPS" />


<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>


<%--OTHER GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />


<%--ORTHOLOGY--%>
<c:set var="geneSymbol">
    <zfin:abbrev entity="${formBean.marker}"/>
</c:set>
<zfin2:orthology
        orthologyPresentationBean="${formBean.orthologyPresentationBean}"
        marker="${formBean.marker}"
        title="ORTHOLOGY for ${geneSymbol}"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

