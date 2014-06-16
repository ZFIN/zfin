<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneBean" scope="request"/>

<table class="summary">
    <tr>
        <th>Orthology Details
            <div style="float: right;">
                <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                    <tiles:putAttribute name="subjectName" value="Orthology Detail Page"/>
                    <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
                </tiles:insertTemplate>
            </div>
        </th>
    </tr>
</table>
<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Gene&nbsp;Name:</span></th>
        <td><span class="name-value"><zfin:name entity="${formBean.marker}"/></span></td>
    </tr>
    <tr>
        <th><span class="name-label">Gene&nbsp;Symbol:</span></th>
        <td><span class="name-value"><zfin:link entity="${formBean.marker}"/></span></td>
    </tr>
</table>


<zfin2:orthology orthologyPresentationBean="${formBean.orthologyPresentationBean}"
                 marker="${formBean.marker}" showTitle="true" title="SUMMARY" hideEvidence="true"
                 webdriverPathFromRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%-- Evidence Codes --%>
<zfin2:orthologyDetailEvidenceCodes marker="${formBean.marker}" title="ORTHOLOGY BY EVIDENCE CODE"/>

<%-- Orthology By Publication --%>
<zfin2:orthologyDetailPublications marker="${formBean.marker}" title="ORTHOLOGY BY PUBLICATION"/>
