<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>

<script src="/javascript/angular/angular.min.js"></script>
<script>
    ;(function() {
        angular.module('app', []);
    }());
</script>


<jsp:useBean id="formBean" class="org.zfin.publication.presentation.JournalAddBean" scope="request"/>

<div class="container-fluid">
    <h2 class="page-header">New Journal</h2>

    <form:form id="journal-form"
               class="form-horizontal"
               action="journal-add"
               commandName="formBean"
               ng-app="app"
               method="post">
        <div class="form-group">
            <form:label path="name" class="col-sm-2 control-label">Name</form:label>
            <div class="col-sm-4">
                <form:input path="name" class="form-control"/>
                <form:errors path="name" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="abbreviation" class="col-sm-2 control-label">Abbreviation</form:label>
            <div class="col-sm-4">
                <form:input path="abbreviation" class="form-control" />
                <form:errors path="abbreviation" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="publisher" class="col-sm-2 control-label">Publisher</form:label>
            <div class="col-sm-4">
                <form:input path="publisher" class="form-control"/>
                <form:errors path="publisher" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="alias" class="col-sm-2 control-label">Synonyms</form:label>
            <div class="col-sm-4">
                <form:input path="alias" class="form-control"/>
                <form:errors path="alias" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="printIssn" class="col-sm-2 control-label">Print ISSN</form:label>
            <div class="col-sm-4">
                <form:input path="printIssn" class="form-control"/>
                <form:errors path="printIssn" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="eIssn" class="col-sm-2 control-label">Online ISSN</form:label>
            <div class="col-sm-4">
                <form:input path="eIssn" class="form-control"/>
                <form:errors path="eIssn" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="nlmID" class="col-sm-2 control-label">NLMID</form:label>
            <div class="col-sm-4">
                <form:input path="nlmID" class="form-control"/>
                <form:errors path="nlmID" cssClass="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="reproduceImages" class="col-sm-2 control-label">Can Reproduce Images?</form:label>
            <div class="col-sm-4">
                <form:checkbox size="50" path="reproduceImages"/>

            </div>
        </div>

        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </form:form>
</div>



