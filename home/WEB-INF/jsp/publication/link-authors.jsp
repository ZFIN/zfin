<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>
<script src="/javascript/zfinutils.service.js"></script>
<script src="/javascript/author-linking.js"></script>

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
    <div class="col-xs-12">

      <zfin-figure:publicationInfo publication="${publication}" showThisseInSituLink="false" showErrataAndNotes="false"/>
      <h2>Linking ${publication.zdbID}</h2>
    </div>
    </div>


    <div ng-controller="AuthorLinkingController as alCtrl" data-pub-zdb-id="${publication.zdbID}" ng-cloak>

        <div class="alert alert-danger" ng-show="alCtrl.errorString">
            {{alCtrl.errorString}}
        </div>

        <div class="row">
            <div class="col-xs-4">
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
            <div class="col-xs-4">
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
                        <a href="#" title="Link this author" ng-click="alCtrl.addAuthor(person)" ng-show="!alCtrl.isLinked(person)"><i class="fa fa-fw fa-plus"></i></a>
                        <span ng-show="alCtrl.isLinked(person)" title="Author already linked">
                            <i class="fa fa-fw fa-check"></i>
                        </span>
                        <a ng-href="/{{person.zdbID}}" target="_blank">{{person.display}}</a>
                    </li>
                </ul>
            </div>
            <div class="col-xs-4">
                <h4>Linked Authors</h4>
                <div class="form-inline">
                    <input ng-model="alCtrl.authorZdbID" class="form-control" placeholder="Link by ZDB ID..."/>
                    <button class="btn btn-success" ng-click="alCtrl.addAuthorByID()">Link</button>
                </div>
                <ul class="list-unstyled">
                    <li class="author-item" ng-repeat="author in alCtrl.registeredAuthors">
                        <a href="#" title="Unlink this author" ng-click="alCtrl.removeAuthor(author)"><i class="fa fa-fw fa-trash-o"></i></a>
                        <a ng-href="/{{author.zdbID}}" target="_blank">{{author.display}}</a>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</div>