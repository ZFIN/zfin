<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">


<jsp:useBean id="formBean" class="org.zfin.publication.presentation.JournalAddBean" scope="request"/>

<div class="container-fluid">
    <h2 class="page-header">New Journal</h2>

    <form:form id="journal-form"
               action="journal-add"
               commandName="formBean"
               ng-app="app"
               method="post">
        <div class="form-group row">
            <form:label path="name" class="col-md-2 col-form-label">Name</form:label>
            <div class="col-md-4">
                <form:input path="name" class="form-control"/>
                <form:errors path="name" class="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="abbreviation" class="col-md-2 col-form-label">Abbreviation</form:label>
            <div class="col-md-4">
                <form:input path="abbreviation" class="form-control" />
                <form:errors path="abbreviation" cssClass="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="publisher" class="col-md-2 col-form-label">Publisher</form:label>
            <div class="col-md-4">
                <form:input path="publisher" class="form-control"/>
                <form:errors path="publisher" class="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="alias" class="col-md-2 col-form-label">Synonyms</form:label>
            <div class="col-md-4">
                <form:input path="alias" class="form-control"/>
                <form:errors path="alias" class="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="printIssn" class="col-md-2 col-form-label">Print ISSN</form:label>
            <div class="col-md-4">
                <form:input path="printIssn" class="form-control"/>
                <form:errors path="printIssn" cssClass="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="eIssn" class="col-md-2 col-form-label">Online ISSN</form:label>
            <div class="col-md-4">
                <form:input path="eIssn" class="form-control"/>
                <form:errors path="eIssn" cssClass="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="nlmID" class="col-md-2 col-form-label">NLMID</form:label>
            <div class="col-md-4">
                <form:input path="nlmID" class="form-control"/>
                <form:errors path="nlmID" cssClass="error"/>
            </div>
        </div>
        <div class="form-group row">
            <form:label path="reproduceImages" class="col-md-2 col-form-label">Can Reproduce Images?</form:label>
            <div class="col-md-4">
                <form:checkbox size="50" path="reproduceImages"/>
            </div>
        </div>

        <div class="form-group row">
            <div class="offset-md-2 col-md-10">
                <button type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </form:form>
</div>



