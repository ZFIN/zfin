<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<script src="${zfn:getAssetPath("bootstrap.js")}"></script>
<script src="${zfn:getAssetPath("angular.js")}"></script>

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


    <div ng-controller="AuthorLinkingController as alCtrl" data-pub-zdb-id="${publication.zdbID}" ng-cloak>

        <div class="alert alert-danger" ng-show="alCtrl.errorString">
            {{alCtrl.errorString}}
        </div>

        <div class="row">
            <div class="col-4">
                <h4>Listed Authors</h4>
                <form class="form-inline">
                    <input class="form-control" ng-model="alCtrl.filterValue" placeholder="Filter authors">
                </form>
                <ul class="list-unstyled">
                    <li class="author-item"
                        ng-repeat="author in alCtrl.authors | filter:alCtrl.filterValue"
                        ng-class="{'author-selected': author === alCtrl.selectedAuthor}">
                        <a href="#" ng-click="alCtrl.selectAuthor(author)">{{author.label}}</a>
                    </li>
                </ul>
            </div>
            <div class="col-4">
                <h4>
                    Suggested Authors
                    <span ng-show="alCtrl.selectedAuthor">
                        for {{alCtrl.selectedAuthor.label}}
                    </span>
                </h4>
                <div class="text-muted" ng-show="!alCtrl.selectedAuthor">
                    <i>Select a listed author to view suggestions.</i>
                </div>
                <div class="alert alert-warning" ng-show="alCtrl.selectedAuthor.loadedSuggestions && alCtrl.selectedAuthor.suggestions.length == 0">
                    <strong>Bummer.</strong> No suggestions found.
                </div>
                <ul class="list-unstyled">
                    <li class="author-item" ng-repeat="person in alCtrl.selectedAuthor.suggestions">
                        <a href="#" title="Link this author" ng-click="alCtrl.addAuthor(person)" ng-show="!alCtrl.isLinked(person)"><i class="fas fa-fw fa-plus-circle"></i></a>
                        <span ng-show="alCtrl.isLinked(person)" title="Author already linked">
                            <i class="fas fa-fw fa-check"></i>
                        </span>
                        <a ng-href="/{{person.zdbID}}" target="_blank">{{person.display}}</a>
                    </li>
                </ul>
            </div>
            <div class="col-4">
                <h4>Linked Authors</h4>
                <div class="form-inline">
                    <input ng-model="alCtrl.authorZdbID" class="form-control" placeholder="Link by ZDB ID..."/>
                    <button class="btn btn-success" ng-click="alCtrl.addAuthorByID()">Link</button>
                </div>
                <ul class="list-unstyled">
                    <li class="author-item" ng-repeat="author in alCtrl.registeredAuthors">
                        <a href="#" title="Unlink this author" ng-click="alCtrl.removeAuthor(author)"><i class="fas fa-trash fa-fw"></i></a>
                        <a ng-href="/{{author.zdbID}}" target="_blank">{{author.display}}</a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>

<script src="${zfn:getAssetPath("react.js")}"></script>