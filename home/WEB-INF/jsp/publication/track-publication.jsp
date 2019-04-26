<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("bootstrap.js")}"></script>
<script src="${zfn:getAssetPath("angular.js")}"></script>

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

  <div>
    <ul id="pub-track-tabs" class="nav nav-tabs nav-justified nav-padded" role="tablist">
      <li role="presentation" class="active"><a href="#status" aria-controls="status" role="tab" data-toggle="tab">Status</a></li>
      <li role="presentation"><a href="#correspondence" aria-controls="correspondence" role="tab" data-toggle="tab">Correspondence</a></li>
    </ul>

    <div class="tab-content">
      <div role="tabpanel" class="tab-pane active" id="status">
        <div publication-tracker
             pub-id="${publication.zdbID}"
             curator-id="${loggedInUser.zdbID}"
             curator-first="${loggedInUser.firstName}"
             curator-last="${loggedInUser.lastName}"
             curator-email="${loggedInUser.email}">
        </div>
      </div>
      <div role="tabpanel" class="tab-pane" id="correspondence">
        <div publication-correspondence pub-id="${publication.zdbID}" curator-id="${loggedInUser.zdbID}" curator-email="${loggedInUser.email}"></div>
      </div>
    </div>
  </div>

</div>

<script>
  $('#pub-track-tabs').stickyTabs();
</script>