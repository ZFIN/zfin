<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="editURL">/action/marker/sequence/edit/${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">none</c:set>
<zfin2:dataManager
        zdbID="${formBean.marker.zdbID}"
        editURL="${editURL}"
        deleteURL="${deleteURL}"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<%--<jsp:useBean id="formBean"  class="org.zfin.marker.presentation.SequencePageInfoBean"/>--%>

<zfin2:sequenceHead gene="${formBean.marker}"/>


<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationFull marker="${formBean.marker}"
                                     dbLinks="${formBean.dbLinkList}"
                                     geneProducts="${formBean.geneProductsBean}"
                                     title="${fn:toUpperCase('Sequence Information')}" />


<zfin2:markerSequenceInfoRelated marker="${formBean.marker}"
                                 dbLinks="${formBean.relatedMarkerDBLinks}"
        />


