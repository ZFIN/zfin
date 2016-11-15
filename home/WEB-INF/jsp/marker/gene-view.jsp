<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/table-collapse.js"></script>
<script src="/javascript/field-error.service.js"></script>
<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/editMarker.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>
<script src="/javascript/curator-notes.directive.js"></script>
<script src="/javascript/public-note.directive.js"></script>
<script src="/javascript/field-error.service.js"></script>
<script src="/javascript/gene-marker-relationship.directive.js"></script>
<script src="/javascript/other-markers.directive.js"></script>
<script src="/javascript/marker.service.js"></script>
<script src="/javascript/autocompletify.directive.js"></script>

<script src="/javascript/ortho-edit.js"></script>
<script src="/javascript/inline-edit-textarea.directive.js"></script>

<script src="/javascript/sequence-information.directive.js"></script>

<authz:authorize access="hasRole('root')">
    <div ng-app="app" ng-controller="EditController as eControl">
</authz:authorize>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<c:set var="markerID">${formBean.marker.zdbID}</c:set>
<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${markerID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${markerID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${markerID}</c:set>

<zfin2:dataManager zdbID="${markerID}"
                   editURL="${editURL}"
                   deleteURL="none"
                   mergeURL="${mergeURL}"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:geneHead gene="${formBean.marker}" previousNames="${formBean.previousNames}" userID="${formBean.user.zdbID}" />


<zfin2:uninformativeGeneName name="${formBean.marker.abbreviation}" fromChimericClone="${formBean.hasChimericClone}"/>

<%--// EXPRESSION SECTION--%>
<zfin2:markerExpression marker="${formBean.marker}" markerExpression="${formBean.markerExpression}" webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%--// MUTANTS AND TARGETED KNOCKDOWNS--%>
<div id="mutant-info">
    <zfin2:mutantsInGene mutantsOnMarkerBean="${formBean.mutantOnMarkerBeans}" marker="${formBean.marker}"/>
</div>

<%--// PHENOTYPE --%>

<zfin2:phenotype phenotypeOnMarkerBean="${formBean.phenotypeOnMarkerBeans}" marker="${formBean.marker}" webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%--// DISEASE --%>
<div id="disease">
    <zfin2:humanDiseaseOnGene gene="${formBean.marker}" diseases="${formBean.diseaseDisplays}"/>
</div>

<%-- gene ontology--%>
<zfin2:geneOntology geneOntologyOnMarker="${formBean.geneOntologyOnMarkerBeans}" marker="${formBean.marker}"/>

<%--protein families, domains, and sites--%>
<zfin2:proteinProductsLight referenceDBs="${formBean.proteinProductDBLinkDisplay}"/>

<%--Transcripts--%>
<zfin2:markerTranscriptSummary relatedTranscriptDisplay="${formBean.relatedTranscriptDisplay}"
                               title="TRANSCRIPTS" showAllTranscripts="true" />

<zfin2:geneProductsDescription geneBean="${formBean}"/>


<zfin2:subsection title="INTERACTIONS AND PATHWAYS" anchor="pathway_links"
                  test="${!empty formBean.pathwayDBLinks}" showNoData="true" noDataText="No data available">
    <table class="summary">
        <c:forEach var="link" items="${formBean.pathwayDBLinks}" varStatus="loop">
            <tr>
                <td><a href="${link.link}">${link.referenceDatabaseName}</a></td>
            </tr>
        </c:forEach>
    </table>
</zfin2:subsection>

<%--Antibodies--%>
<zfin2:markerRelationshipsLightSingleType relationships="${formBean.relatedAntibodies}" marker="${formBean.marker}" title="ANTIBODIES" maxNumber="5"/>

<%--Plasmid Links--%>
<zfin2:subsection title="PLASMIDS" anchor="plasmid_links"
                  test="${!empty formBean.plasmidDBLinks}" showNoData="true" noDataText="No data available">
    <table class="summary">
        <c:forEach var="link" items="${formBean.plasmidDBLinks}" varStatus="loop">
            <tr>
                <td><a href="${link.link}">${link.referenceDatabaseName}</a></td>
            </tr>
        </c:forEach>
    </table>
</zfin2:subsection>



<%--Constructs--%>
<zfin2:constructsWithSequences formBean="${formBean}"/>

<%--SEGMENT (CLONE AND PROBE) RELATIONSHIPS--%>

<zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}" marker="${formBean.marker}" title="SEGMENT (CLONE AND PROBE) RELATIONSHIPS"/>
<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>


<%--other GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />


<%--ORTHOLOGY--%>
<zfin2:orthology
        orthologyPresentationBean="${formBean.orthologyPresentationBean}"
        marker="${formBean.marker}"
        showTitle="true"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<authz:authorize access="hasRole('root')">
    </div>
</authz:authorize>

<script>
    jQuery(function() {
        jQuery("#mutant-info").find(".alleles").tableCollapse({label: "alleles"});
        jQuery("#disease").find(".marker-go-table").tableCollapse({label: "records"});
    });
</script>

