<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/table-collapse.js"></script>

<script src="/javascript/dist/angular.bundle.js"></script>

<div ng-app="app" ng-controller="EditController as eControl">

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   deleteURL="none"
                   mergeURL="${mergeURL}"
                   editMarker="true"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:geneHead gene="${formBean.marker}" previousNames="${formBean.previousNames}" userID="${formBean.user.zdbID}" />

<%--// EXPRESSION SECTION--%>
<zfin2:markerExpression marker="${formBean.marker}" markerExpression="${formBean.markerExpression}"/>

<zfin2:mutantsInGene mutantsOnMarkerBean="${formBean.mutantOnMarkerBeans}" marker="${formBean.marker}"/>
<%--Transcripts--%>
<zfin2:markerTranscriptSummary relatedTranscriptDisplay="${formBean.relatedTranscriptDisplay}"
                               title="TRANSCRIPTS" showAllTranscripts="true" />
    <zfin2:subsection title="INTERACTIONS AND PATHWAYS" anchor="pathway_links">
        <c:if test="${!empty formBean.pathwayDBLinks}">
            <table class="summary">
                <c:forEach var="link" items="${formBean.pathwayDBLinks}" varStatus="loop">
                    <tr>
                        <td><a href="${link.link}">${link.referenceDatabaseName}</a></td>
                    </tr>
                </c:forEach>
            </table>
        </c:if>
        <zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}"
                                        marker="${formBean.marker}" title="" interactsWith="yes"/>
    </zfin2:subsection>

<%--SEGMENT (CLONE AND PROBE) RELATIONSHIPS--%>
    <zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}"
                                    marker="${formBean.marker}" title="MARKER RELATIONSHIPS" interactsWith="no"/>


<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>


<%--OTHER GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />

<%--ORTHOLOGY--%>
<zfin2:orthology marker="${formBean.marker}" showTitle="true"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

</div>
