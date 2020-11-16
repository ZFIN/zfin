<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bodyClass="data-page">
  <link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
  <script src="${zfn:getAssetPath("bootstrap.js")}"></script>

  <c:set var="viewURL">/${publication.zdbID}</c:set>
  <c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>
  <c:set var="linkURL">/action/publication/${publication.zdbID}/link</c:set>
  <c:if test="${allowCuration}">
    <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
  </c:if>
  <c:if test="${hasCorrespondence}">
    <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
  </c:if>

  <zfin2:dataManager zdbID="${publication.zdbID}"
                     viewURL="${viewURL}"
                     editURL="${editURL}"
                     linkURL="${linkURL}"
                     correspondenceURL="${correspondenceURL}"
                     curateURL="${curateURL}"/>

  <div class="__react-root"
       id="PubTracker"
       data-pub-id="${publication.zdbID}"
       data-user-id="${loggedInUser.zdbID}"
       data-user-name="${loggedInUser.display}"
       data-user-email="${loggedInUser.email}"
  >
  </div>

  <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>