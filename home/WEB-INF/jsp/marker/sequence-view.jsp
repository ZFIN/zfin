<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="editURL">/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString() %>?MIval=aa-sequence.apg&UPDATE=1&OID=${formBean.marker.zdbID}&rtype=marker</c:set>
<c:set var="deleteURL">none</c:set>
<zfin2:dataManager
        zdbID="${formBean.marker.zdbID}"
        editURL="${editURL}"
        deleteURL="${deleteURL}"/>

<%--&lt;%&ndash;latestUpdate="${formBean.latestUpdate}"&ndash;%&gt;--%>
<%--editURL="${editURL}"--%>

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
                                     title="${fn:toUpperCase('Sequence Information')}" />


<zfin2:markerSequenceInfoRelated marker="${formBean.marker}"
                                 dbLinks="${formBean.relatedMarkerDBLinks}"
        />


