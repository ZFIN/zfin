<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.marker.Marker/org.zfin.gwt.marker.Marker.nocache.js"></script>
<script src="/javascript/angular/angular.min.js" type="text/javascript"></script>
<script src="/javascript/angular/angular-sanitize.js"></script>
<script src="/javascript/editMarker.js"></script>
<script src="/javascript/marker.service.js"></script>
<script src="/javascript/sequence-information.directive.js"></script>
<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.min.css">
<link rel="stylesheet" type="text/css" href="/css/Marker.css">

<authz:authorize access="hasRole('root')">
    <div ng-app="app">

        <c:set var="deleteURL">none</c:set>
        <zfin2:dataManager
                zdbID="${formBean.marker.zdbID}"
                editURL=""
                deleteURL="${deleteURL}"/>

        <div style="float: right">
            <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                <tiles:putAttribute name="subjectName" value="${formBean.marker.name}"/>
            </tiles:insertTemplate>
        </div>

        <div style="font-size: large; text-align: center;">
            <a href="/action/marker/sequence/view/${formBean.marker.zdbID}">[View Sequences]</a>
        </div>

        <zfin2:sequenceHead gene="${formBean.marker}"/>

        <script type="text/javascript">
            var MarkerProperties = {
                zdbID: "${formBean.marker.zdbID}"
            };
        </script>

        <sequence-information id="seqenceInfo" marker-id="${formBean.marker.zdbID}">
            <script>
                function refreshSequenceInformation() {
                    alert("refresh sequence info")
                    si.init()
                }
            </script>
        </sequence-information>

    </div>


</authz:authorize>



