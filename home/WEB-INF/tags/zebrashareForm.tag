<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="formBean" type="org.zfin.zebrashare.presentation.SubmissionFormBean" required="true" %>

<form:form method="POST" commandName="formBean" class="form-horizontal">
    <div class="form-group">
        <label class="col-sm-3 control-label">Authors</label>
        <div class="col-sm-8">
            <form:textarea path="authors" cssClass="form-control"/>
            <form:errors path="authors" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Title</label>
        <div class="col-sm-8">
            <form:textarea path="title" cssClass="form-control"/>
            <form:errors path="title" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Abstract</label>
        <div class="col-sm-8">
            <form:textarea path="abstractText" cssClass="form-control" rows="8"/>
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-8">
            <button type="submit" class="btn btn-primary">Submit</button>
            <a class="btn btn-default" href="/">Cancel</a>
        </div>
    </div>
</form:form>