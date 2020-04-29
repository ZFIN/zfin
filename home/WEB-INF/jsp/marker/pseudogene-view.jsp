<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="${zfn:getAssetPath("angular.js")}"></script>

<div ng-app="app" ng-controller="EditController as eControl">

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

    <zfin2:dataManager zdbID="${markerID}"
                       deleteURL="none"
                       mergeURL="${mergeURL}"
                       editURL="${editURL}"
                       prototypeURL="/action/marker/pseudogene/prototype-view/${formBean.marker.zdbID}"
    />

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
