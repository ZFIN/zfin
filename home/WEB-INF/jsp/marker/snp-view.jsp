<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SnpMarkerBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:snpHead markerBean="${formBean}"/>


<%--MARKER RELATIONSHIPTS--%>
<zfin2:markerRelationshipsLight relationships="${formBean.markerRelationshipPresentationList}" marker="${formBean.marker}"
                                title="${fn:toUpperCase('MARKER RELATIONSHIPS')}" />

<%--OTHER GENE/Marker Pages--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}" />


<%--MAPPING INFORMATION--%>
<zfin2:mappingInformation mappedMarker="${formBean.mappedMarkerBean}"/>


<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>
