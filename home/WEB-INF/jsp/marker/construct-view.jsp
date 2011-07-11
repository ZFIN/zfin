<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.ConstructBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-delete_record.apg&OID=${formBean.marker.zdbID}&rtype=marker</c:set>

<%--Currently, not possible to merge these (not provided as an option on the merge page--%>
<%--mergeURL="${deleteURL}"--%>
<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   latestUpdate="${formBean.latestUpdate}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
        <tiles:putAttribute name="subjectID" value="${formBean.marker.zdbID}"/>
    </tiles:insertTemplate>
</div>

<zfin2:markerHead marker="${formBean.marker}" previousNames="${formBean.previousNames}"/>

<%--Construct Features--%>
<%--&lt;%&ndash;SEGMENT (CLONE AND PROBE) RELATIONSHIPS&ndash;%&gt;--%>
<zfin2:constructFeatures relationships="${formBean.markerRelationshipPresentationList}"
        marker="${formBean.marker}"
        title="CONSTRUCT FEATURES" />


<zfin2:subsectionMarker title="TRANSGENIC LINES"
                        test="${!empty formBean.transgenicLineLinks}" showNoData="true">
    <table class="summary horizontal-solidblock">
        <tr>
            <td>
                <zfin2:toggledHyperlinkStrings collection="${formBean.transgenicLineLinks}" maxNumber="5" suffix="<br>"/>
            </td>
        </tr>
    </table>
</zfin2:subsectionMarker>


<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

