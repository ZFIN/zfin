<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>

    <h3>New Analytics Report</h3>
    <p>Generate new analytics report.</p>

    <form:form action="" method="POST" cssClass="form-horizontal" modelAttribute="analyticsReportRequestForm">

    <div class="form-group row">
        <form:label path="credentials" cssClass="col-md-2 col-form-label required">Credentials</form:label>
        <div class="col-md-4">
            <form:textarea path="credentials" cols="60" rows="8"></form:textarea>
        </div>
    </div>

    <div class="form-group row">
        <form:label path="reportName" cssClass="col-md-2 col-form-label required">Report Name</form:label>
        <div class="col-md-4">
            <form:select path="reportName" items="${reportNames}"></form:select>
        </div>
    </div>

    <div class="form-group row">
        <form:label path="start" cssClass="col-md-2 col-form-label required">Begin Date</form:label>
        <div class="col-md-4">
            <form:input type="date" path="start"></form:input>
        </div>
    </div>

    <div class="form-group row">
        <form:label path="end" cssClass="col-md-2 col-form-label required">End Date</form:label>
        <div class="col-md-4">
            <form:input type="date" path="end"></form:input>
        </div>
    </div>


        <%--    Submit button --%>
        <div class="form-group row">
            <div class="col-md-4 offset-md-2">
                <button type="submit" class="btn btn-primary">Submit</button>
            </div>
        </div>

    </form:form>

</z:page>
