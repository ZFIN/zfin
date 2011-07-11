<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-delete_record.apg&OID=${formBean.marker.zdbID}&rtype=marker</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   mergeURL="${deleteURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:markerHead marker="${formBean.marker}" previousNames="${formBean.previousNames}"/>

<%--MARKER RELATIONSHIPTS--%>
<c:if test="${formBean.marker.type ne 'RAPD'}">
    <zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}" marker="${formBean.marker}"
                                    title="${fn:toUpperCase('MARKER RELATIONSHIPS')}" />
</c:if>

<%--SEQUENCE INFORMATION--%>
<c:if test="${formBean.marker.type ne 'RAPD'}">
    <zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="SEQUENCE INFORMATION" showAllSequences="false"/>
</c:if>

<%--OTHER GENE/Marker Pages--%>
<c:if test="${formBean.marker.type eq 'BAC_END'}">
    <zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />
</c:if>

<%--MAPPING INFORMATION--%>
<zfin2:mappingInformation mappedMarker="${formBean.mappedMarkerBean}"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

