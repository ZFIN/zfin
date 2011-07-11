<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.AntibodyMarkerBean" scope="request"/>

<script type="text/javascript" src="/javascript/prototype.js"></script>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   mergeURL="${formBean.mergeURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:antibodyMarkerHeader antibodyBean="${formBean}" />

<zfin2:externalNotes notes="${formBean.externalNotes}" />

<zfin2:antibodyLabeling formBean="${formBean}" webdriverPath="<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>"/>

<div class="summary">
    <a href="/action/antibody/publication-list?antibody.zdbID=${formBean.marker.zdbID}&orderBy=author">CITATIONS</a>  (${formBean.numPubs})
</div>


