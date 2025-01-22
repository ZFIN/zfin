<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">

    <div class="container">
        <h1>Register for ZFIN Account</h1>

        <p>Please fill out the following information and we will get back to you as soon as we have reviewed it
            and approved the request.</p>

        <form:form action="" method="POST" modelAttribute="submission" id="nomenclature">

            <div class="form-group row">
                <form:label path="name" cssClass="col-md-3 col-form-label required">Lab Name</form:label>
                <div class="col-md-5">
                    <form:input path="name" cssClass="form-control" required="true"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="type" cssClass="col-md-3 col-form-label required">Type</form:label>
                <div class="col-md-5">
                    <form:select path="type" cssClass="form-control" required="true">
                        <form:option value="" label=""/>
                        <form:option value="Lab" label="Lab"/>
                        <form:option value="Company" label="Company"/>
                    </form:select>
                </div>
            </div>

            <%--hidden email field, used for spam prevention--%>
            <div class="form-group row alternate-email" id="alternate-email">
                <form:label path="email" cssClass="col-md-3 col-form-label">Please leave blank</form:label>
                <div class="col-md-5">
                    <form:input path="email" cssClass="form-control" />
                </div>
            </div>

            <div class="form-group row">
                <form:label path="contactPerson" cssClass="col-md-3 col-form-label required">Contact Name</form:label>
                <div class="col-md-5">
                    <form:input path="contactPerson" cssClass="form-control" required="true"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="email2" cssClass="col-md-3 col-form-label required">Contact Email</form:label>
                <div class="col-md-5">
                    <form:input path="email2" cssClass="form-control" required="true"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="phone" cssClass="col-md-3 col-form-label">Phone</form:label>
                <div class="col-md-5">
                    <form:input path="phone" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="fax" cssClass="col-md-3 col-form-label">Fax</form:label>
                <div class="col-md-5">
                    <form:input path="fax" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="url" cssClass="col-md-3 col-form-label">Website</form:label>
                <div class="col-md-5">
                    <form:input path="url" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="comments" cssClass="col-md-3 col-form-label">Additional Comments</form:label>
                <div class="col-md-5">
                    <form:textarea class="form-control" path="comments" rows="3"></form:textarea>
                </div>
            </div>

            <div class="form-group row">
                <div class="col-md-5">
                    <form:button class="btn btn-primary" value="Submit">Submit</form:button>
                </div>
            </div>
        </form:form>
    </div>

    <script>
    <%-- hide as a spam prevention tactic --%>
    const altEmailElement = document.getElementById("alternate-email");
    if (altEmailElement) {
        altEmailElement.style.display = "none";
    }
    </script>

</z:page>