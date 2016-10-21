<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css">

<div class="container-fluid">
    <h3 class="page-header">Updates for ${zdbID}</h3>
    <c:if test="${empty updates}">
        <div class="text-muted">
            <i>No updates yet</i>
        </div>
    </c:if>
    <c:forEach items="${updates}" var="update">
        <div class="media">
            <div class="media-left">
                <div class="thumb-container">
                    <c:choose>
                        <c:when test="${!empty update.submitter}">
                            <zfin2:viewSnapshot className="thumb-image" value="${update.submitter}"/>
                        </c:when>
                    </c:choose>
                </div>
            </div>
            <div class="media-body">
                <h4 class="media-heading">
                    <c:choose>
                        <c:when test="${!empty update.submitter}">
                            ${update.submitter.display}
                        </c:when>
                        <c:otherwise>
                            ${update.submitterName}
                        </c:otherwise>
                    </c:choose>
                    &nbsp;<small><fmt:formatDate value="${update.whenUpdated}" pattern="yyyy-MM-dd"/></small>
                </h4>
                <dl class="dl-horizontal">
                    <dt>Field</dt>
                    <dd>${update.fieldName}</dd>

                    <dt>Previous value</dt>
                    <dd>${update.oldValue}</dd>

                    <dt>New value</dt>
                    <dd>${update.newValue}</dd>

                    <dt>Comments</dt>
                    <dd>${update.comments}</dd>
                </dl>
            </div>
        </div>
    </c:forEach>
</div>