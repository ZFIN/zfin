<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:set var="editURL">/action/marker/marker-edit?zdbID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-delete_record.apg&OID=${formBean.marker.zdbID}&rtype=marker</c:set>

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

<zfin2:cloneHead cloneBean="${formBean}"/>

<zfin2:uninformativeName name="${formBean.marker.abbreviation}"/>

<div class="summary">
    <div id="clone_gbrowse_thumbnail_box" style="display: none;">
        <table class="summary solidblock" id="clone_gbrowse_thumbnail_box">
            <caption>GBROWSE</caption>
            <tr>
                <td style="text-align: center">
                    <div style="margin: .5em; border: 1px solid black; background: white">

                        <a href="/<%=ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT.value()%>?name=${formBean.marker.abbreviation}">
                            <img
                                    onload="document.getElementById('clone_gbrowse_thumbnail_box').style.display = 'block';"
                                    style="padding-bottom:10px; border: 0 "
                                    src="/<%=ZfinPropertiesEnum.GBROWSE_IMG_PATH_FROM_ROOT.value()%>?grid=0&width=600&options=fullclone+0+mRNA+0+genes+0&type=fullclone&type=genes&type=mRNA&name=${formBean.marker.zdbID}&h_feat=${formBean.marker.abbreviation}">

                        </a>

                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>

<c:if test="${formBean.clone.rnaClone}">
    <zfin2:markerExpression markerExpression="${formBean.markerExpression}" marker="${formBean.marker}"
                            webdriverRoot="<%=ZfinProperties.getWebDriver()%>"/>
</c:if>

<zfin2:markerRelationshipsLight marker="${formBean.clone}"
                                relationships="${formBean.markerRelationshipPresentationList}"
                                title="MARKER RELATIONSHIPS"
        />

<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}"
                                        title="SEQUENCE INFORMATION" showAllSequences="false"/>


<%--OTHER GENE/Marker Pages--%>
<%--old page--%>
<%--<zfin2:markerSummaryPages  marker="${formBean.marker}" links="${formBean.summaryDBLinkDisplay}"/>--%>
<zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

<zfin2:mappingInformation mappedMarker="${formBean.mappedMarkerBean}"/>

<!--http://zfin.org/cgi-bin/webdriver?MIval=aa-showpubs.apg&OID=ZDB-EST-000426-1181&rtype=marker&title=EST+Name&name=fb73a06&abbrev=fb73a06&total_count=2-->

<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.clone}"/>

