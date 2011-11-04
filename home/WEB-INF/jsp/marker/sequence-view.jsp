<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="editURL">/<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString() %>?MIval=aa-sequence.apg&UPDATE=1&OID=${formBean.marker.zdbID}&rtype=marker</c:set>
<c:set var="deleteURL">/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString() %>?MIval=aa-delete_record.apg&OID=${formBean.marker.zdbID}&rtype=marker</c:set>
<zfin2:dataManager
        zdbID="${formBean.marker.zdbID}"
        editURL="${editURL}"
        deleteURL="${deleteURL}"
        latestUpdate="${lastUpdated}"
        rtype="marker"
        />

<%--&lt;%&ndash;latestUpdate="${formBean.latestUpdate}"&ndash;%&gt;--%>
<%--editURL="${editURL}"--%>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insertTemplate>
</div>

    <%--<jsp:useBean id="formBean"  class="org.zfin.marker.presentation.SequencePageInfoBean"/>--%>

<zfin2:sequenceHead gene="${formBean.marker}"/>


<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationFull marker="${formBean.marker}"
                                     dbLinks="${formBean.dbLinkList}"
                                     title="${fn:toUpperCase('Sequence Information')}" />


<zfin2:markerSequenceInfoRelated marker="${formBean.marker}"
                                     dbLinks="${formBean.firstRelatedMarkerDBLink}" title="ENCODES"
                                      />

<zfin2:markerSequenceInfoRelated marker="${formBean.marker}"
                                      dbLinks="${formBean.secondRelatedMarkerDBLink}" title="CONTAINED IN"
        />

