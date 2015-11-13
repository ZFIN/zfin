<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<h3>Download Archive:</h3>


<authz:authorize access="hasRole('root')">
    Back to <a href="/action/devtool/home">Dev Tools Home Page</a>


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
            <td>${fn:length(formBean.downloadFileService.archiveDirectories)}
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

    Database Info:
    <table class="summary groupstripes" width="80%">
        <tr>
            <th width="200" nowrap="nowrap">
                Last Unload Date
            </th>
            <th>
                Version
            </th>
        </tr>
        <tr>
            <td>
                <fmt:formatDate pattern="d MMM yyyy" value="${formBean.downloadFileService.unloadInfo.date}"/><br/>
                <fmt:formatDate pattern="HH:mm" value="${formBean.downloadFileService.unloadInfo.date}"/>
            </td>
            <td>
                    ${formBean.downloadFileService.unloadInfo.version}
            </td>
        </tr>
    </table>
    <p/>
</authz:authorize>

<form:form method="Get" modelAttribute="formBean" name="Date History" onsubmit="return false;">
    Archive Date: <form:select path="date" items="${formBean.downloadDateList}"/>
    <input value="Show" onclick="goToArchive();" type="button" id="date">
</form:form>

<script type="text/javascript ">
    function goToArchive() {
        var date = document.getElementById('date').value;
        window.location = "archive/" + date;
    }
</script>



