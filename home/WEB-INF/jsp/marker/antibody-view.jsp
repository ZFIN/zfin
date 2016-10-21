<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.AntibodyMarkerBean" scope="request"/>

<script src="/javascript/table-collapse.js"></script>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${formBean.editURL}"
                   deleteURL="${formBean.deleteURL}"
                   mergeURL="${formBean.mergeURL}"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:antibodyMarkerHeader antibodyBean="${formBean}" />

<zfin2:externalNotes notes="${formBean.externalNotes}" />

<div id="antibody-labeling">
    <zfin2:antibodyLabeling formBean="${formBean}" webdriverPath="<%= ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>"/>
</div>

<div class="summary">
    <a href="/action/antibody/antibody-publication-list?antibodyID=${formBean.marker.zdbID}&orderBy=author">CITATIONS</a>  (${formBean.numPubs})
</div>

<script>
    jQuery(function() {
        jQuery("#antibody-labeling").tableCollapse({label: "labeled structures"});
    });
</script>