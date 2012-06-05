<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<h3>Download Archive:</h3>


<authz:authorize ifAnyGranted="root">
    Back to <a href="/action/dev-tools/home">Dev Tools Home Page</a>


    <table class="primary-entity-attributes summary">
        <tr>
            <th width="200px">Index File</th>
            <td>???</td>
        </tr>
        <tr>
            <th>Downloads Directory</th>
            <td>${formBean.downloadFileService.downloadDirectory}
            </td>
        </tr>
        <tr>
            <th>Number of Archives</th>
            <td>${fn:length(formBean.downloadFileService.downloadFileDirectories)}
            </td>
        </tr>
        <tr>
            <th style="text-align: right">First Archive</th>
            <td><zfin2:download-date-link date="${formBean.downloadFileService.firstDownloadDate}"/>
            </td>
        </tr>
        <tr>
            <th style="text-align: right">Current Files</th>
            <td><zfin2:download-date-link date="${formBean.downloadFileService.latestDownloadDate}"/>
            </td>
        </tr>
        <tr>
            <th style="text-align: right">Update Cache</th>
            <td><a href="update-cache"> Update </a>
            </td>
        </tr>
    </table>

    <p/>
</authz:authorize>

<form:form method="Get" modelAttribute="formBean" name="Date History" onsubmit="return false;">
    Show archive for <form:select path="date" items="${formBean.downloadDateList}"/>
    <input value="Show" onclick="goToArchive();" type="button" id="date">
</form:form>

<script type="text/javascript ">
    function goToArchive() {
        var date = document.getElementById('date').value;
        window.location = "archive/" + date;
    }
</script>



