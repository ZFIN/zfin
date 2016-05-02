<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<c:set var="service" value="${formBean.downloadFileService}"/>

<h3>Download Archive Error:</h3>

<c:choose>
    <c:when test="${!service.downloadArchiveExists}">
        No Root Download archive found. Login for more detail. <p/>
        <authz:authorize access="hasRole('root')">

            No Root Download archive exists: ${service.downloadDirectory}
            <p/>
            Run the following script to generate the first archive: $TARGETROOT/server_apps/data_transfer_Downloads/DownloadFiles.pl
        </authz:authorize>
    </c:when>
    <c:otherwise>
        <c:if test="${!service.validArchiveFound}">
            No download archive found that matches the data. <p/>

            The time stamp of the data (unload) is:
            <fmt:formatDate value="${service.unloadInfo.date}" pattern="dd-MM-yyyy"/>
            <br/>
            but no archive before this date is found.
            <authz:authorize access="hasRole('root')">
                <c:if test="${service.futureArchivesAvailable}">
                    <p/>
                    Future download archive available:
                    <a href="downloads/archive/${service.futureArchive}"> ${service.futureArchive}
                </c:if>
            </authz:authorize>
        </c:if>
    </c:otherwise>
</c:choose>





