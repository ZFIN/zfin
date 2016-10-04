<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:set var="editURL">/action/marker/marker-edit?zdbID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">none</c:set>

<script src="/javascript/gbrowse-image.js"></script>
<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/editMarker.js"></script>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"
                   rtype="marker"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<div ng-app="editMarker" ng-controller="EditController as eControl">
    <zfin2:cloneHead cloneBean="${formBean}"/>

    <zfin2:uninformativeCloneName name="${formBean.marker.abbreviation}" chimericClone="${formBean.marker.chimeric}"/>

    <div class="summary">
        <div id="clone_gbrowse_thumbnail_box">
            <table class="summary solidblock">
                <caption>GBROWSE</caption>
                <tr>
                    <td style="text-align: center">
                        <div class="gbrowse-image"/>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <script>
        jQuery("#clone_gbrowse_thumbnail_box").gbrowseImage({
            width: 600,
            imageTarget: ".gbrowse-image",
            imageUrl: "${formBean.image.imageUrl}",
            linkUrl: "${formBean.image.linkUrl}"
        });
    </script>

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
    <zfin2:markerSummaryReport marker="${formBean.marker}" links="${formBean.otherMarkerPages}"/>

    <zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.clone}"/>
</div>
