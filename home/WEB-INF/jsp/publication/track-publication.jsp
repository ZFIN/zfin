<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<c:set var="viewURL">/${publication.zdbID}</c:set>
<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>
<c:set var="linkURL">/action/publication/${publication.zdbID}/link</c:set>
<c:if test="${allowCuration}">
  <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
</c:if>
<c:if test="${hasCorrespondence}">
  <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
</c:if>

<div class="container-fluid" ng-app="app">
  <zfin2:dataManager zdbID="${publication.zdbID}"
                     viewURL="${viewURL}"
                     editURL="${editURL}"
                     linkURL="${linkURL}"
                     correspondenceURL="${correspondenceURL}"
                     curateURL="${curateURL}"/>

  <p class="lead">
    <a href="/${publication.zdbID}">${publication.title}</a>
    <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="far fa-file-pdf"></i></a></c:if>
  </p>

  <div class="__react-root"
       id="PubTracker"
       data-pub-id="${publication.zdbID}"
       data-user-id="${loggedInUser.zdbID}"
       data-user-name="${loggedInUser.display}"
       data-user-email="${loggedInUser.email}"
  >
  </div>

</div>

<script src="${zfn:getAssetPath("react.js")}"></script>