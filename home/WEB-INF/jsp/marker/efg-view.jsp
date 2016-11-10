<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/trusted-html.filter.js"></script>

<script src="/javascript/editMarker.js"></script>
<script src="/javascript/nomenclature.js" type="text/javascript"></script>
<script src="/javascript/curator-notes.directive.js"></script>
<script src="/javascript/public-note.directive.js"></script>
<script src="/javascript/marker.service.js"></script>

<authz:authorize access="hasRole('root')">
    <div ng-app="app" ng-controller="EditController as eControl" ng-init="init('${gene.name}','${gene.abbreviation}')">
</authz:authorize>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerBean" scope="request"/>

<c:set var="editURL">/<%=ZfinProperties.getWebDriver()%>?MIval=aa-markerview.apg&UPDATE=1&orgOID=&OID=${formBean.marker.zdbID}</c:set>
<c:set var="deleteURL">/action/infrastructure/deleteRecord/${formBean.marker.zdbID}</c:set>

<zfin2:dataManager zdbID="${formBean.marker.zdbID}"
                   editURL="${editURL}"
                   deleteURL="${deleteURL}"/>


<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
    </tiles:insertTemplate>
</div>

<zfin2:markerHead marker="${formBean.marker}" previousNames="${formBean.previousNames}" showEditControls="true" userID="${formBean.user.zdbID}" />

</div>

<%--// EXPRESSION SECTION--%>
<zfin2:markerExpression marker="${formBean.marker}" markerExpression="${formBean.markerExpression}" webdriverRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

<%--Antibodies
<zfin2:markerRelationshipsLightSingleType relationships="${formBean.relatedAntibodies}" marker="${formBean.marker}" title="ANTIBODIES" maxNumber="5"/>
--%>
<%--Constructs--%>
<zfin2:constructsWithSequences formBean="${formBean}"/>

<%--SEQUENCE INFORMATION--%>
<zfin2:markerSequenceInformationSummary marker="${formBean.marker}" sequenceInfo="${formBean.sequenceInfo}" title="${fn:toUpperCase('Sequence Information')}" showAllSequences="false"/>

<%--CITATIONS--%>
<zfin2:citationFooter numPubs="${formBean.numPubs}" marker="${formBean.marker}"/>

<authz:authorize access="hasRole('root')">
    </div>
</authz:authorize>


