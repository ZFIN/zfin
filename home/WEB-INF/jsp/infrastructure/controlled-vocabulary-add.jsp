<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>


<jsp:useBean id="formBean" class="org.zfin.infrastructure.presentation.ControlledVocabularyAddBean" scope="request"/>

<authz:authorize access="hasRole('root')">
<div class="container-fluid">
    <h2 class="page-header">New Species</h2>

    <form:form id="cv-form"
               class="form-horizontal"
               action="controlled-vocabulary-add"
               commandName="formBean"
               method="post">
        <div class="form-group">
            <form:label path="termName" class="col-sm-2 control-label">Term Name</form:label>
            <div class="col-sm-1">
                <form:input path="termName" class="form-control"/>
                <form:errors path="termName" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="foreignSpecies" class="col-sm-2 control-label">Foreign Species</form:label>
            <div class="col-sm-2">
                <form:input path="foreignSpecies" class="form-control"/>
                <form:errors path="foreignSpecies" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <form:label path="nameDefinition" class="col-sm-2 control-label">Name Definition</form:label>
            <div class="col-sm-2">
                <form:input path="nameDefinition" class="form-control"/>
                <form:errors path="nameDefinition" class="error"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>
    </form:form>
</div>
</authz:authorize>



