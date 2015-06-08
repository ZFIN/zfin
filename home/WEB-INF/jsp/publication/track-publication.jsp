<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" type="text/css" href="/css/bootstrap3/css/bootstrap.css">
<link rel="stylesheet" type="text/css" href="/css/zfin-bootstrap-overrides.css">
<script type="text/javascript" src="/css/bootstrap3/js/bootstrap.js"></script>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/angular-route.min.js"></script>
<script src="/javascript/pub-tracking.js"></script>

<base href="/">

<div class="container-fluid" ng-app="pubTrackingApp">
  <div class="page-header">
    <h1>
      Track ${publication.zdbID}<br>
      <small>${publication.title}</small>
    </h1>
  </div>

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title">File</h3>
    </div>
    <div class="panel-body">
      <c:if test="${hasFile}">
        <a href="<%=ZfinPropertiesEnum.PDF_LOAD.value()%>/${publication.fileName}"><i class="fa fa-file-pdf-o"></i> PDF</a>
      </c:if>
      <form action="/cgi-bin/upload.cgi" method="post" class="form-inline">
        <div class="form-group">
          <label for="pdfUploadFileInput">
            ${hasFile ? "Replace File" : "Upload File"}
          </label>
          <input type="file" accept="application/pdf" name="upload" id="pdfUploadFileInput">
        </div>
        <input type="hidden" name="redirect_url" value="/action/publication/${publication.zdbID}/track">
        <input type="hidden" name="OID" value="${publication.zdbID}">
        <button type="submit" class="btn btn-default">Upload</button>
      </form>
    </div>
  </div>

  <div ng-controller="PubTrackingTopicsController as topicsCtrl">
    <div class="panel panel-default">
      <div class="panel-heading">
        <h3 class="panel-title">Status</h3>
      </div>
      <div class="panel-body" ng-cloak ng-show="topicsCtrl.status">
        <div class="row bottom-buffer">
          <div class="col-xs-4" >
            <div ng-if="topicsCtrl.status.indexed">
              <strong>Indexed on {{topicsCtrl.status.indexedDate | date:'yyyy-MM-dd'}}</strong>
              <button class="btn btn-default btn-block" ng-click="topicsCtrl.unindexPub()">Un-index</button>
            </div>
            <div ng-if="!topicsCtrl.status.indexed">
              <strong>Not indexed yet</strong>
              <button class="btn btn-primary btn-block" ng-click="topicsCtrl.indexPub()">Index</button>
            </div>
          </div>
        </div>
        <div class="row bottom-buffer">
          <div class="col-xs-4">
            <div ng-if="!topicsCtrl.status.closedDate && !topicsCtrl.hasTopics()">
              <strong>Not closed yet</strong>
              <button class="btn btn-primary btn-block" ng-click="topicsCtrl.closePub()">Close with no data found</button>
            </div>
            <div ng-if="!topicsCtrl.status.closedDate && topicsCtrl.hasTopics()">
              <strong>Not closed yet</strong>
              <button class="btn btn-primary btn-block" ng-click="topicsCtrl.closePub()">Close</button>
            </div>
            <div ng-if="topicsCtrl.status.closedDate">
              <strong>Closed on {{topicsCtrl.status.closedDate | date:"yyyy-MM-dd"}}</strong>
              <button class="btn btn-default btn-block">Re-open</button>
            </div>
          </div>
        </div>
        <div class="alert alert-warning" role="alert" ng-show="topicsCtrl.warnings.length > 0">
          <h4>Heads up!</h4>
          <p class="bottom-buffer-sm">You might not want to close this publication yet. Are you sure you want to close it?</p>
          <ul class="bottom-buffer">
            <li ng-repeat="warning in topicsCtrl.warnings">
              {{warning}}
            </li>
          </ul>
          <p>
            <button class="btn btn-warning" ng-click="topicsCtrl.closeWithoutWarning()">Yes, close it</button>
            <button class="btn btn-default" ng-click="topicsCtrl.hideWarnings()">Cancel</button>
          </p>
        </div>
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
          <tr ng-repeat="topic in topicsCtrl.topics" ng-cloak>
            <td>
              <input type="checkbox" ng-checked="topic.dataFound" ng-click="topicsCtrl.toggleDataFound(topic, $index)"/> {{topic.topic}}
            </td>
            <td>
              {{topicsCtrl.getStatus(topic)}}
            </td>
            <td>{{(!topicsCtrl.isNew(topic)) ? topic.curator.name : ""}}</td>
            <td>
              <button class="btn btn-default" ng-show="topicsCtrl.isNew(topic)" ng-click="topicsCtrl.open(topic, $index)">Open</button>
              <!-- Split button -->
              <div class="btn-group" ng-show="topicsCtrl.isOpen(topic)">
                <button type="button" class="btn btn-default" ng-click="topicsCtrl.close(topic, $index)">Close</button>
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                  <span class="caret"></span>
                  <span class="sr-only">Toggle Dropdown</span>
                </button>

                <ul class="dropdown-menu" role="menu">
                  <li><a href ng-click="topicsCtrl.unopen(topic, $index)">Back to New</a></li>
                </ul>
              </div>
              <%--<button class="btn btn-danger" ng-show="{{topic.openedDate && !topic.closedDate}}" ng-click="topicsCtrl.close(topic, $index)">Close</button>--%>
              <%--<button class="btn btn-default" ng-show="{{topic.openedDate && !topic.closedDate}}">Un-open</button>--%>
              <button class="btn btn-default" ng-show="topicsCtrl.isClosed(topic)" ng-click="topicsCtrl.open(topic, $index)">Re-open</button>
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
            <textarea ng-model="topicsCtrl.newNote" class="form-control" rows="3" id="new-note-text"></textarea>
          </div>
          <button ng-click="topicsCtrl.addNote()" type="submit" class="btn btn-primary">Post</button>
        </form>
        <hr>
        <div class="media" ng-repeat="note in topicsCtrl.notes" ng-cloak>
          <div class="media-left">
            <div style="width: 64px; height: 64px; text-align: center;">
              <img style="max-width: 100%; max-height: 100%" ng-src="{{note.curator.imageURL}}">
            </div>
          </div>
          <div class="media-body">
            <h4 class="media-heading">
              {{note.curator.name}}
              <small>{{note.date | date:'yyyy-MM-dd'}}</small>
            </h4>
            <ul class="list-inline" ng-show="note.editable">
              <li><small><a href ng-click="topicsCtrl.beginEditing(note)">Edit</a></small></li>
              <li><small><a href ng-click="topicsCtrl.deleteNote(note)">Delete</a></small></li>
            </ul>
            <p ng-hide="note.editing">{{note.text}}</p>
            <div ng-show="note.editing">
              <div class="form-group">
                <textarea class="form-control" ng-model="note.text"></textarea>
              </div>
              <button ng-click="topicsCtrl.editNote(note)" type="submit" class="btn btn-primary">Done Editing</button>
              <button ng-click="topicsCtrl.cancelEditing(note)" type="submit" class="btn btn-default">Cancel</button>
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
        <p class="text-danger error hidden">No contacts selected</p>
        <button class="btn btn-primary" type="submit">Edit Notification</button>
      </form>
    </div>
  </div>
</div>

<script>
  $(function() {
    $("#edit-notification").submit(function (evt) {
      evt.preventDefault();
      var contactList = $(":checked", this).map(function () { return $(this).val(); }).get().join("|");
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
              "&sender_id=${loggedInUser}",
              "editwindow","resizable=yes,toolbar=yes,scrollbars=yes,width=700,height=900");
    });
  });
</script>