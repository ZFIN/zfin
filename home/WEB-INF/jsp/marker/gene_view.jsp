<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<zfin2:inputWelcome marker="${formBean.marker}"/>

<zfin2:geneHead gene="${formBean.marker}"/>

<%--nomenclature history--%>

<zfin2:markerExpression markerExpression="${formBean.markerExpression}" />

<%--mutants and targeted knockdowns--%>

<%--GENE PRODUCTS--%>
<%--gene ontology--%>
<%--protein families, domains, and sites--%>
<zfin2:markerSummaryPages marker="${formBean.marker}" links="${formBean.proteinProductDBLinkDisplay}" title="${fn:toUpperCase('Protein Products')}" />


<%--SEGMENT (CLONE AND PROBE) RELATIONSHIPS--%>
<zfin2:markerRelationships relationships="${formBean.markerRelationships}" marker="${formBean.marker}"
                           title="${fn:toUpperCase('Segment (Clone and Probe) Relationships')}" />


<%--SEQUENCE INFORMATION--%>
<zfin2:transcriptSequenceInformation sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>


<%--OTHER GENE/Marker Pages--%>


<%--MAPPING INFORMATION--%>
<zfin2:mappingInformation mappedMarker="${formBean.mappedMarkerBean}"/>


<%--ORTHOLOGY--%>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>


<%--<zfin2:markerSummaryPages links="${formBean.otherLinks}" marker="${formBean.marker}"/>--%>