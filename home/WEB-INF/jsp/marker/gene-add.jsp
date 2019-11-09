<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.GeneAddFormBean" scope="request"/>

<div class="container-fluid">
  <h2 class="page-header">New Gene</h2>
  <form:form id="gene-add" cssClass="form-horizontal" commandName="formBean" action="gene-add">
    <div class="form-group row">
      <form:label path="type" class="col-md-2 col-form-label">Type</form:label>
      <div class="col-md-4">
        <form:select path="type" class="form-control">
          <form:option value="" label="Select..." disabled="true" selected="true"/>
          <form:options items="${formBean.allTypes}"/>
        </form:select>
        <form:errors path="type" class="error"/>
      </div>
    </div>
    <div class="form-group row">
      <form:label path="publicationId" class="col-md-2 col-form-label">Reference</form:label>
      <div class="col-md-4">
        <form:input path="publicationId" class="form-control" placeholder="ZDB-PUB-123456-7"/>
        <form:errors path="publicationId" cssClass="error"/>
      </div>
    </div>
    <div class="form-group row">
      <form:label path="name" class="col-md-2 col-form-label">Name</form:label>
      <div class="col-md-4">
        <form:input path="name" class="form-control"/>
        <form:errors path="name" class="error"/>
      </div>
    </div>
    <div class="form-group row" id="abbrev-group">
      <form:label path="abbreviation" class="col-md-2 col-form-label">Abbreviation</form:label>
      <div class="col-md-4">
        <form:input path="abbreviation" class="form-control"/>
        <form:errors path="abbreviation" class="error"/>
      </div>
    </div>
    <div class="form-group row">
      <form:label path="alias" class="col-md-2 col-form-label">Alias</form:label>
      <div class="col-md-4">
        <form:input path="alias" class="form-control"/>
      </div>
    </div>
    <div class="form-group row">
      <form:label path="publicNote" class="col-md-2 col-form-label">Public Note</form:label>
      <div class="col-md-6">
        <form:textarea path="publicNote" class="form-control" rows="3"/>
      </div>
    </div>
    <div class="form-group row">
      <form:label path="curatorNote" class="col-md-2 col-form-label">Curator Note</form:label>
      <div class="col-md-6">
        <form:textarea path="curatorNote" class="form-control" rows="3"/>
      </div>
    </div>
    <div class="form-group row">
      <div class="offset-md-2 col-md-10">
        <button type="submit" class="btn btn-primary">Submit</button>
      </div>
    </div>
  </form:form>
</div>

<script>
  $(function() {
    var $type = $('#type');

    function showHideAbbrevField() {
      var $abbrev = $('#abbrev-group');
      $abbrev.toggle($type.val() != 'EFG');
    }

    $type.on('change', showHideAbbrevField);

    showHideAbbrevField();
  })
</script>