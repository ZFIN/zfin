<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<div class="container-fluid">
    <h3 class="page-header">Updates for ${zdbID}</h3>
    <c:if test="${empty updates}">
        <div class="text-muted">
            <i>No updates yet</i>
        </div>
    </c:if>
    <c:forEach items="${updates}" var="update">
        <div class="media">
            <div class="mr-3">
                <div class="thumb-container">
                    <c:choose>
                        <c:when test="${!empty update.submitter}">
                            <zfin2:profileImage className="thumb-image" value="${update.submitter}"/>
                        </c:when>
                    </c:choose>
                </div>
            </div>
            <div class="media-body">
                <h5>
                    <c:choose>
                        <c:when test="${!empty update.submitter}">
                            ${update.submitter.display}
                        </c:when>
                        <c:otherwise>
                            ${update.submitterName}
                        </c:otherwise>
                    </c:choose>
                    &nbsp;<small class="text-muted"><fmt:formatDate value="${update.whenUpdated}" pattern="yyyy-MM-dd"/></small>
                </h5>
                <dl class="row">
                    <dt class="col-md-2 text-md-right">Field</dt>
                    <dd class="col-md-10 mb-md-0">${update.fieldName}</dd>

                    <dt class="col-md-2 text-md-right">Previous value</dt>
                    <dd class="col-md-10 mb-md-0">${update.oldValue}</dd>

                    <dt class="col-md-2 text-md-right">New value</dt>
                    <dd class="col-md-10 mb-md-0">${update.newValue}</dd>

                    <dt class="col-md-2 text-md-right">Comments</dt>
                    <dd class="col-md-10 mb-md-0">${update.comments}</dd>
                </dl>
            </div>
        </div>
    </c:forEach>
</div>