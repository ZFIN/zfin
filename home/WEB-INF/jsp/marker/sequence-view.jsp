<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/editMarker.js"></script>
<script src="/javascript/marker.service.js"></script>
<script src="/javascript/sequence-information.directive.js"></script>

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

<authz:authorize access="hasRole('root')">
    <div ng-app="app">
        <sequence-info-edit-link marker-id="${formBean.marker.zdbID}" edit="true"></sequence-info-edit-link>
    </div>
</authz:authorize>

<%--<jsp:useBean id="formBean"  class="org.zfin.marker.presentation.SequencePageInfoBean"/>--%>

<zfin2:sequenceHead gene="${formBean.marker}"/>


<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationFull marker="${formBean.marker}"
                                     dbLinks="${formBean.dbLinkList}"
                                     title="${fn:toUpperCase('Sequence Information')}" />


<zfin2:markerSequenceInfoRelated marker="${formBean.marker}"
                                 dbLinks="${formBean.relatedMarkerDBLinks}"
        />


