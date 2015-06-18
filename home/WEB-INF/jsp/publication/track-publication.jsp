<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/angular-route.min.js"></script>
<script src="/javascript/pub-tracking.js"></script>

<c:set var="editURL">/action/publication/${publication.zdbID}/edit</c:set>

<c:set var="linkURL">/cgi-bin/webdriver?MIval=aa-link_authors.apg&OID=${publication.zdbID}&anon1=zdb_id&anon1text=${publication.zdbID}</c:set>

<c:if test="${allowCuration}">
  <c:set var="curateURL">/cgi-bin/webdriver?MIval=aa-curation.apg&OID=${publication.zdbID}</c:set>
</c:if>

<div class="container-fluid" ng-app="pubTrackingApp">
  <zfin2:dataManager zdbID="${publication.zdbID}"
                     editURL="${editURL}"
                     linkURL="${linkURL}"
                     curateURL="${curateURL}"
                     rtype="publication"/>

  <%--<h2>Track ${publication.zdbID}</h2>--%>
  <p class="lead">
    <a href="/${publication.zdbID}">${publication.title}</a>
    <c:if test="${!empty publication.fileName}"> <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}" target="_blank"><i class="fa fa-file-pdf-o"></i></a></c:if>
  </p>

  <div id="pub-tracking-main" ng-controller="PubTrackingController as trackCtrl" data-zdb-id="${publication.zdbID}">
    <div ng-cloak ng-show="trackCtrl.status">
      <div class="row bottom-buffer">
        <div class="col-xs-4 col-xs-offset-1" >
          <div ng-if="trackCtrl.status.indexed">
            <h4>Indexed on {{trackCtrl.status.indexedDate | date:'yyyy-MM-dd'}}</h4>
            <button class="btn btn-default btn-block" ng-click="trackCtrl.unindexPub()">Un-index</button>
          </div>
          <div ng-if="!trackCtrl.status.indexed">
            <h4>Not indexed yet</h4>
            <button class="btn btn-primary btn-block" ng-click="trackCtrl.indexPub()">Index</button>
          </div>
        </div>
        <div class="col-xs-4 col-xs-offset-2">
          <div ng-if="!trackCtrl.status.closedDate && !trackCtrl.hasTopics()">
            <h4>Not closed yet</h4>
            <button class="btn btn-primary btn-block" ng-click="trackCtrl.validateForClose()">Close with no data found</button>
          </div>
          <div ng-if="!trackCtrl.status.closedDate && trackCtrl.hasTopics()">
            <h4>Not closed yet</h4>
            <button class="btn btn-primary btn-block" ng-click="trackCtrl.validateForClose()">Close</button>
          </div>
          <div ng-if="trackCtrl.status.closedDate">
            <h4>Closed on {{trackCtrl.status.closedDate | date:"yyyy-MM-dd"}}</h4>
            <button class="btn btn-default btn-block" ng-click="trackCtrl.reopenPub()">Reopen</button>
          </div>
        </div>
      </div>
      <div class="alert alert-warning" role="alert" ng-show="trackCtrl.warnings.length > 0">
        <h4>Heads up!</h4>
        <p class="bottom-buffer-sm">You might not want to close this publication yet. Are you sure you want to close it?</p>
        <ul class="bottom-buffer">
          <li ng-repeat="warning in trackCtrl.warnings">
            {{warning}}
          </li>
        </ul>
        <p>
          <button class="btn btn-warning" ng-click="trackCtrl.closePub()">Yes, close it</button>
          <button class="btn btn-default" ng-click="trackCtrl.hideWarnings()">Cancel</button>
        </p>
      </div>
    </div>

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Topics</h3>
      </div>
      <table class="table table-hover">
        <thead>
          <tr>
            <th>Topic</th>
            <th>Status</th>
            <th>Curator</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          <tr ng-repeat="topic in trackCtrl.topics" ng-cloak>
            <td>
              <input type="checkbox" ng-checked="topic.dataFound" ng-click="trackCtrl.toggleTopicDataFound(topic, $index)"/> {{topic.topic}}
            </td>
            <td>
              {{trackCtrl.getTopicStatus(topic)}}
            </td>
            <td>{{(!trackCtrl.isNewTopic(topic)) ? topic.curator.name : ""}}</td>
            <td>
              <button class="btn btn-default btn-dense" ng-show="trackCtrl.isNewTopic(topic)" ng-click="trackCtrl.openTopic(topic, $index)">Open</button>
              <!-- Split button -->
              <div class="btn-group" ng-show="trackCtrl.isOpenTopic(topic)">
                <button type="button" class="btn btn-default btn-dense" ng-click="trackCtrl.closeTopic(topic, $index)">Close</button>
                <button type="button" class="btn btn-default dropdown-toggle btn-dense" data-toggle="dropdown">
                  <span class="caret"></span>
                  <span class="sr-only">Toggle Dropdown</span>
                </button>
                <ul class="dropdown-menu" role="menu">
                  <li><a href ng-click="trackCtrl.unopenTopic(topic, $index)">Back to New</a></li>
                </ul>
              </div>
              <button class="btn btn-default btn-dense" ng-show="trackCtrl.isClosedTopic(topic)" ng-click="trackCtrl.openTopic(topic, $index)">Re-open</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Correspondence</h3>
      </div>
      <table class="table table-hover">
        <thead>
          <tr>
            <th class="col-xs-6">Contacted</th>
            <th class="col-xs-6">Correspondence completed</th>
          </tr>
        </thead>
        <tbody>
          <tr ng-if="trackCtrl.allCorrespondencesClosed()">
            <td>
              <button class="btn btn-primary" ng-click="trackCtrl.newCorrespondence()">Contacted author</button>
            </td>
            <td></td>
          </tr>
          <tr ng-repeat="corr in trackCtrl.correspondences" ng-cloak>
            <td class="hover-trigger">
              {{ corr.openedDate | date:'yyyy-MM-dd' }}
              <a href class="hover-reveal" style="display: none;" ng-click="trackCtrl.deleteCorrespondence(corr, $index)">
                <i class="fa fa-times-circle"></i>
              </a>
            </td>
            <td ng-if="corr.closedDate" class="hover-trigger">
              <i class="fa fa-fw" ng-class="{'fa-check': corr.replyReceived, 'fa-times': !corr.replyReceived}"></i>
              {{ corr.closedDate | date:'yyyy-MM-dd' }}
              <a href class="hover-reveal" style="display: none;" ng-click="trackCtrl.reopenCorrespondence(corr, $index)">
                <i class="fa fa-times-circle"></i>
              </a>
            </td>
            <td ng-if="!corr.closedDate" class="hover-trigger">
              <div class="btn-group">
                <button class="btn btn-primary" ng-click="trackCtrl.closeCorrespondence(true, corr, $index)">
                  <i class="fa fa-check"></i> Received reply
                </button>
                <button type="button" class="btn btn-primary dropdown-toggle" data-toggle="dropdown">
                  <span class="caret"></span>
                  <span class="sr-only">Toggle Dropdown</span>
                </button>
                <ul class="dropdown-menu" role="menu">
                  <li>
                    <a href ng-click="trackCtrl.closeCorrespondence(false, corr, $index)">
                      <i class="fa fa-times"></i> Reply not expected
                    </a>
                  </li>
                </ul>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Notes</h3>
      </div>
      <div class="panel-body">
        <form role="form">
          <div class="form-group">
            <label for="new-note-text">New Note</label>
            <textarea ng-model="trackCtrl.newNote" class="form-control" rows="3" id="new-note-text"></textarea>
          </div>
          <button ng-click="trackCtrl.addNote()" type="submit" class="btn btn-primary">Post</button>
        </form>
        <hr>
        <p ng-if="!trackCtrl.notes.length" class="text-muted text-center">No notes yet</p>
        <div class="media" ng-repeat="note in trackCtrl.notes" ng-cloak>
          <div class="media-left">
            <div class="thumb-container">
              <img class="thumb-image" ng-src="{{note.curator.imageURL}}">
            </div>
          </div>
          <div class="media-body">
            <h4 class="media-heading">
              {{note.curator.name}}
              <small>{{note.date | date:'yyyy-MM-dd'}}</small>
            </h4>
            <ul class="list-inline" ng-show="note.editable">
              <li><small><a href ng-click="trackCtrl.beginEditingNote(note)">Edit</a></small></li>
              <li><small><a href ng-click="trackCtrl.deleteNote(note)">Delete</a></small></li>
            </ul>
            <p ng-hide="note.editing" class="keep-breaks">{{note.text}}</p>
            <div ng-show="note.editing">
              <div class="form-group">
                <textarea class="form-control" ng-model="note.text"></textarea>
              </div>
              <button ng-click="trackCtrl.editNote(note)" type="submit" class="btn btn-primary">Done Editing</button>
              <button ng-click="trackCtrl.cancelEditingNote(note)" type="submit" class="btn btn-default">Cancel</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title">Contact Authors</h3>
    </div>
    <div class="panel-body">
      <form id="edit-notification">
        <ul class="list-unstyled">
          <c:forEach var="author" items="${publication.people}">
            <c:if test="${!empty author.email}">
              <li><input class="author-email" type="checkbox" checked value="${author.fullName}=${author.email}"/> ${author.fullName}</li>
            </c:if>
          </c:forEach>
        </ul>
        <div class="form-group">
          <label for="additional-emails">Additional recipients</label>
          <input class="form-control" id="additional-emails" type="text" placeholder="alice@example.com, bob@example.net"/>
        </div>
        <p class="text-danger error">No contacts selected</p>
        <button class="btn btn-primary" type="submit">Edit Notification</button>
      </form>
    </div>
  </div>
</div>

<script>
  $(function() {
    $("#edit-notification .error").hide();
    $("#edit-notification").submit(function (evt) {
      evt.preventDefault();
      var contactList = $(":checked", this).map(function () { return $(this).val(); }).get().join("|") + "|";
      var additionalEaddr = $("#additional-emails").val();
      if (!contactList && !additionalEaddr) {
        $(".error", this).show();
        return;
      }
      $(".error", this).hide();
      window.open("/<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.value()%>?MIval=aa-notif_frame.apg" +
              "&status=edit" +
              "&OID=${publication.zdbID}" +
              "&contact_list=" + contactList +
              "&additional_eaddr=" + additionalEaddr +
              "&sender_id=${loggedInUser.zdbID}",
              "editwindow","resizable=yes,toolbar=yes,scrollbars=yes,width=700,height=900");
    });

    $("#pub-tracking-main")
            .on("mouseenter", ".hover-trigger", function () {
              $(".hover-reveal", this).show();
            })
            .on("mouseleave", ".hover-trigger", function () {
              $(".hover-reveal", this).hide();
            });
  });
</script>