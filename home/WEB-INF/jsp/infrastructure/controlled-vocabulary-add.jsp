<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.infrastructure.presentation.ControlledVocabularyAddBean" scope="request"/>

<z:page bootstrap="true">
    <authz:authorize access="hasRole('root')">
    <div class="container-fluid">
        <h2 class="page-header">New Species As Construct Component</h2>

        <form:form id="cv-form"
                   cssClass="form-horizontal"
                   action="controlled-vocabulary-add"
                   modelAttribute="formBean"
                   method="post">
            <div class="form-group row">
                <form:label path="termName" class="col-md-2 col-form-label">Term Name</form:label>
                <div class="col-md-1">
                    <form:input path="termName" class="form-control" placeholder="Hsa."/>
                    <form:errors path="termName" class="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="foreignSpecies" class="col-md-2 col-form-label">Foreign Species</form:label>
                <div class="col-md-2">
                    <form:input path="foreignSpecies" class="form-control" placeholder="Human"/>
                    <form:errors path="foreignSpecies" class="error"/>
                </div>
            </div>
            <div class="form-group row">
                <form:label path="nameDefinition" class="col-md-2 col-form-label">Name Definition</form:label>
                <div class="col-md-2">
                    <form:input path="nameDefinition" class="form-control" placeholder="Homo sapiens"/>
                    <form:errors path="nameDefinition" class="error"/>
                </div>
            </div>
            <div class="form-group row">
                <div class="offset-md-2 col-md-10">
                    <button type="submit" class="btn btn-primary">Submit</button>
                </div>
            </div>
        </form:form>
    </div>
    </authz:authorize>
</z:page>
