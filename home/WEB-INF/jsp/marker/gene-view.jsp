<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/table-collapse.js"></script>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>
<c:set var="mergeURL">/action/marker/merge?zdbIDToDelete=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="none"
                   mergeURL="${mergeURL}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:geneHead gene="${formBean.marker}" previousNames="${formBean.previousNames}"/>


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
        title="ORTHOLOGY for ${geneSymbol}"
        webdriverPathFromRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<script>
    jQuery(function() {
        jQuery("#mutant-info").find(".alleles").tableCollapse({label: "alleles"});
        jQuery("#disease").find(".marker-go-table").tableCollapse({label: "records"});
    });
</script>