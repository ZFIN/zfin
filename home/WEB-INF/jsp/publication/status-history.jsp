<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">

<div class="container-fluid">
  <p class="lead">
    <a href="/${publication.zdbID}">${publication.title}</a>
    <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="fa fa-file-pdf-o"></i></a></c:if>
  </p>

  <c:forEach items="${statusUpdates}" var="item">
    <div class="media">
      <div class="media-left">
        <div class="thumb-container">
          <c:choose>
            <c:when test="${!empty item.updater.snapshot}">
              <img class="thumb-image" src="/action/profile/image/view/${item.updater.zdbID}.jpg">
            </c:when>
            <c:otherwise>
              <img class="thumb-image" src="/images/LOCAL/smallogo.gif">
            </c:otherwise>
          </c:choose>
        </div>
      </div>
      <div class="media-body">
        <h4 class="media-heading">
            ${item.updater.firstName}&nbsp;${item.updater.lastName}
          <small>
            <fmt:formatDate value="${item.date.time}" pattern="yyyy-MM-dd h:mm a"/>
          </small>
        </h4>
        <p>
          Status changed to <b>${item.status.name}</b>
          <c:if test="${!empty item.owner}">
            <br>Owner changed to <b>${item.owner.firstName}&nbsp;${item.owner.lastName}</b>
          </c:if>
          <c:if test="${!empty item.location}">
            <br>Location changed to <b>${item.location.name}</b>
          </c:if>
        </p>
      </div>
    </div>
  </c:forEach>
</div>
