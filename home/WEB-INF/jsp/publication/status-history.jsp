<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<div class="container-fluid">
    <p class="lead">
        <a href="/${publication.zdbID}">${publication.title}</a>
        <c:if test="${!empty publication.fileName}"> <a
                href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i
                class="far fa-file-pdf"></i></a></c:if>
    </p>

    <c:forEach items="${events}" var="event">
        <div class="media">
            <div class="mr-3">
                <div class="thumb-container">
                    <zfin2:profileImage className="thumb-image" value="${event.performedBy}"/>
                </div>
            </div>
            <div class="media-body">
                <h5>
                    ${event.performedBy.firstName}&nbsp;${event.performedBy.lastName}
                    <small class="text-muted">
                        <fmt:formatDate value="${event.date.time}" pattern="yyyy-MM-dd h:mm a"/>
                    </small>
                </h5>
                <p>
                    ${event.display}
                </p>
            </div>
        </div>
    </c:forEach>
</div>
