<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">

    <c:set var="publication" value="${publicationBean.publication}" scope="page"/>
    <c:set var="viewURL">/${publication.zdbID}</c:set>
    <c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>
    <c:set var="trackURL">/action/publication/${publication.zdbID}/track</c:set>
    <c:if test="${allowCuration}">
      <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
    </c:if>
    <c:if test="${hasCorrespondence}">
        <c:set var="correspondenceURL">/action/publication/${publication.zdbID}/track#correspondence</c:set>
    </c:if>

    <style>
        .author-item {
            padding: 0.25em;
        }
        .author-selected {
            background-color: #eee;
        }
    </style>


    <div class="container-fluid" ng-app="app">
      <zfin2:dataManager zdbID="${publication.zdbID}"
                         viewURL="${viewURL}"
                         editURL="${editURL}"
                         trackURL="${trackURL}"
                         correspondenceURL="${correspondenceURL}"
                         curateURL="${curateURL}"/>

        <div class="row">
            <div class="col-12">
                <zfin-figure:publicationInfo publication="${publication}" showThisseInSituLink="false" showErrataAndNotes="false"/>
            </div>
        </div>
        <div class="row">
            <div class="col-8">
                <h2>Linking ${publication.zdbID}</h2>
            </div>
            <div class="col-4">
                <div class="__react-root"
                     id="ProcessorApproval"
                     data-pub-id="${publication.zdbID}"
                     data-task="LINK_AUTHORS"
                >
                </div>
            </div>
        </div>
        <div class="author-linking-container">
            <div class="__react-root"
                 id="LinkAuthors"
                 data-pub-id="${publication.zdbID}"
            ></div>
        </div>
    </div>
    </div>

    <script src="${zfn:getAssetPath("react.js")}"></script>
</z:page>