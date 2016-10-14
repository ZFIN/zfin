<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel=stylesheet type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/author-linking.js"></script>


<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>
<c:set var="trackURL">/action/publication/${publication.zdbID}/track</c:set>
<c:if test="${allowCuration}">
  <c:set var="curateURL">/action/curation/${publication.zdbID}</c:set>
</c:if>

<style>
    .btn-marginalized { margin-top: .2em; margin-bottom: .2em;}
</style>


<div class="container-fluid" ng-app="authorLinkingApp">
  <zfin2:dataManager zdbID="${publication.zdbID}"
                     editURL="${editURL}"
                     trackURL="${trackURL}"
                     curateURL="${curateURL}"
                     rtype="publication"/>

    <div class="row">
    <div class="col-xs-12">

      <zfin-figure:publicationInfo publication="${publication}" showThisseInSituLink="false" showErrataAndNotes="false"/>
      <h2>Linking ${publication.zdbID}</h2>
    </div>
    </div>


    <div ng-controller="AuthorLinkingController as alCtrl" data-pub-zdb-id="${publication.zdbID}" ng-cloak>

        <div class="alert alert-danger"
                ng-show="alCtrl.errorString">{{alCtrl.errorString}}</div>

        <div class="row">

            <div class="col-md-6">
                <h3>Potential Authors</h3>
                <!-- tabs left -->
                <div>
                    <div>
                        <input class="form-control" style="width:200px;" ng-model="alCtrl.filterValue" placeholder="filter">
                    </div>

                    <ul class="nav nav-pills nav-stacked col-md-3">
                        <li ng-repeat="author in alCtrl.authors | filter:alCtrl.filterValue">
                            <a href="#" ng-click="alCtrl.selectAuthor(author)">{{author.label}}</a>
                        </li>
                    </ul>
                    <div class="col-md-9">

                        <div>
                            <h2>{{alCtrl.selectedAuthor.label}}</h2>

                            <div ng-repeat="person in alCtrl.selectedAuthor.suggestions">
                                <button class="btn btn-success btn-sm btn-marginalized"
                                        ng-click="alCtrl.addAuthor(person)">
                                    {{person.display}} <i class="fa fa-chevron-right"></i>
                                </button>
                            </div>
                            <div class="alert alert-warning" ng-show="alCtrl.selectedAuthor.loadedSuggestions && alCtrl.selectedAuthor.suggestions.length == 0">
                                <strong>Bummer.</strong> No suggestions found.
                            </div>
                        </div>

                    </div>
                </div>
                <!-- /tabs -->
            </div>
            <div class="col-md-4 col-md-offset-2">
                <div class="form-inline">
                    <input ng-model="alCtrl.authorZdbID" class="form-control" placeholder="Add by ZDB ID..."/>
                    <button class="btn btn-success" ng-click="alCtrl.addAuthorByID()">Link</button>
                </div>


                <h3>Registered Authors</h3>
                <ul class="list-unstyled">
                    <li ng-repeat="author in alCtrl.registeredAuthors">
                        <a ng-href="/{{author.zdbID}}">{{author.display}}</a>
                        <button class="btn btn-link" ng-click="alCtrl.removeAuthor(author)">
                            <i class="fa fa-trash-o"></i>
                        </button>
                    </li>
                </ul>
            </div>

        </div>
        <!-- /row -->
    </div>

</div>