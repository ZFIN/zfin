<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MorpholinoBean" scope="request"/>

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

<zfin2:morpholinoHead marker="${formBean.marker}" markerBean="${formBean}" typeName="Morpholino" previousNames="${formBean.previousNames}"/>

<%--// PHENOTYPE --%>
<zfin2:phenotype bean="${formBean.phenotypeOnMarkerBeans}" marker="${formBean.marker}" webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

