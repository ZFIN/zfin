<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>
<%--<script type="text/javascript" language="javascript"
        src="org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>

  <script type="text/javascript">
  var curationProperties = {
    zdbID: "ZDB-PUB-020723-1",
    moduleType: "feature",
    debug: "false"
  }
</script>--%>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.RegionAddFormBean" scope="request"/>


<div class="container-fluid">
  <h2 class="page-header">New NTR</h2>
  <form:form id="nonTranscribedRegion-add" class="form-horizontal" commandName="formBean" action="nonTranscribedRegion-add">
    <div class="form-group">
      <form:label path="type" class="col-sm-2 control-label">Type</form:label><a href="http://www.sequenceontology.org/browser/current_svn/term/">Link to the SO (for reference)</a>
      <div class="col-sm-4">
        <form:select path="type" class="form-control">
          <form:option value="" label="Select..." disabled="true" selected="true"/>
          <form:options items="${formBean.allTypes}"/>
        </form:select>
        <form:errors path="type" class="error"/>
      </div>
    </div>
    <%--<div id="construct-relationship"></div>--%>
    <div class="form-group">
      <form:label path="publicationId" class="col-sm-2 control-label">Reference</form:label>
      <div class="col-sm-4">
        <form:input path="publicationId" class="form-control" placeholder="ZDB-PUB-123456-7"/>
        <form:errors path="publicationId" cssClass="error"/>
      </div>
    </div>
    <div class="form-group">
      <form:label path="name" class="col-sm-2 control-label">Name</form:label>
      <div class="col-sm-4">
        <form:input path="name" class="form-control"/>
        <form:errors path="name" class="error"/>
      </div>
    </div>
    <div class="form-group" id="abbrev-group">
      <form:label path="abbreviation" class="col-sm-2 control-label">Abbreviation</form:label>
      <div class="col-sm-4">
        <form:input path="abbreviation" class="form-control"/>
        <form:errors path="abbreviation" class="error"/>
      </div>
    </div>
    <div class="form-group">
      <form:label path="alias" class="col-sm-2 control-label">Alias</form:label>
      <div class="col-sm-4">
        <form:input path="alias" class="form-control"/>
      </div>
    </div>
    <div class="form-group">
      <form:label path="publicNote" class="col-sm-2 control-label">Public Note</form:label>
      <div class="col-sm-6">
        <form:textarea path="publicNote" class="form-control" rows="3"/>
      </div>
    </div>
    <div class="form-group">
      <form:label path="curatorNote" class="col-sm-2 control-label">Curator Note</form:label>
      <div class="col-sm-6">
        <form:textarea path="curatorNote" class="form-control" rows="3"/>
      </div>
    </div>
    <div class="form-group">
      <div class="col-sm-offset-2 col-sm-10">
        <button type="submit" class="btn btn-primary">Submit</button>
      </div>
    </div>
  </form:form>
</div>

<script>
  $(function() {
    var $type = $('#type');
    var selectedTerm = $type.val();




    $type.on('change', updateTermInfoBox(selectedTerm, selectedOption, tabName));


  })
</script>

