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

  <div class="panel panel-default">
    <div class="panel-heading">
      <h3 class="panel-title">Topics</h3>
    </div>
    <table class="table table-hover">
      <thead>
        <tr>
          <th>Topic</th>
          <th>Open</th>
          <th>Close</th>
          <th>Curator</th>
        </tr>
      </thead>
      <tbody>

      </tbody>
    </table>
  </div>

  <div class="panel panel-default"
       ng-controller="PubTrackingNotesController as notesCtrl"
       data-logged-in-user="${loggedInUser}" data-pub-zdb-id="${publication.zdbID}">
    <div class="panel-heading">
      <h3 class="panel-title">Notes</h3>
    </div>
    <div class="panel-body">
      <form role="form">
        <div class="form-group">
          <label for="new-note-text">New Note</label>
          <textarea ng-model="notesCtrl.newNote" class="form-control" rows="3" id="new-note-text"></textarea>
        </div>
        <button ng-click="notesCtrl.addNote()" type="submit" class="btn btn-primary">Post</button>
      </form>
      <hr>
      <div class="media" ng-repeat="note in notesCtrl.notes">
        <div class="media-left">
          <div style="width: 64px; height: 64px; text-align: center;">
            <img style="max-width: 100%; max-height: 100%" ng-src="{{note.curator.imageURL}}">
          </div>
        </div>
        <div class="media-body">
          <h4 class="media-heading">
            {{note.curator.name}}
            <small>{{note.date}}</small>
          </h4>
          <ul class="list-inline" ng-show="note.curator.zdbID === notesCtrl.user">
            <li><small><a href ng-click="note.editing = true;">Edit</a></small></li>
            <li><small><a href ng-click="notesCtrl.deleteNote(note)">Delete</a></small></li>
          </ul>
          <p ng-hide="note.editing">{{note.text}}</p>
          <div ng-show="note.editing">
            <textarea ng-model="note.text"></textarea>
            <button ng-click="notesCtrl.editNote(note)" type="submit" class="btn btn-default">Save</button>
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