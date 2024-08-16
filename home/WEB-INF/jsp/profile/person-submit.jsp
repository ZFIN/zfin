<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">

    <div class="container">
        <h1>Register for ZFIN Account</h1>

        <p>Please fill out the following information and we will get back to you as soon as we have reviewed it
            and approved the request.</p>

        <form:form action="" method="POST" modelAttribute="submission" id="nomenclature">

            <div class="form-group row">
                <form:label path="firstName" cssClass="col-md-3 col-form-label required">First Name</form:label>
                <div class="col-md-5">
                    <form:input path="firstName" cssClass="form-control" required="true"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="lastName" cssClass="col-md-3 col-form-label required">Last Name</form:label>
                <div class="col-md-5">
                    <form:input path="lastName" cssClass="form-control"  required="true"/>
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
                <form:label path="email2" cssClass="col-md-3 col-form-label required">Email</form:label>
                <div class="col-md-5">
                    <form:input path="email2" cssClass="form-control" required="true"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="address" cssClass="col-md-3 col-form-label">Address</form:label>
                <div class="col-md-5">
                    <form:input path="address" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="country" cssClass="col-md-3 col-form-label">Country</form:label>
                <div class="col-md-5">
                    <form:select path="country" cssClass="form-control">
                        <form:option value="" />
                        <form:options items="${countryList}" />
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="phone" cssClass="col-md-3 col-form-label">Phone</form:label>
                <div class="col-md-5">
                    <form:input path="phone" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="lab" cssClass="col-md-3 col-form-label">Lab</form:label>
                <div class="col-md-5">
                    <form:input path="lab" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="role" cssClass="col-md-3 col-form-label">Role/Position</form:label>
                <div class="col-md-5">
                    <form:select path="role" cssClass="form-control">
                        <form:option value="" label=""/>
                        <c:forEach var="roleOption" items="${roleOptions}">
                            <form:option value="${roleOption.name}" label="${roleOption.name}"/>
                        </c:forEach>
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="url" cssClass="col-md-3 col-form-label">Website</form:label>
                <div class="col-md-5">
                    <form:input path="url" cssClass="form-control"/>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="orcid" cssClass="col-md-3 col-form-label">ORCID</form:label>
                <div class="col-md-5">
                    <form:input path="orcid" cssClass="form-control"/>
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

    <script>
    //if the Lab field is not empty, make Role/Position field required
    document.addEventListener("DOMContentLoaded", function() {
        const labField = document.getElementById("lab");
        const roleField = document.getElementById("role");
        const roleLabel = document.querySelector("label[for='role']");

        if (labField && roleField) {
            labField.addEventListener("change", function() {
                if (labField.value) {
                    roleField.required = true;
                    roleLabel.classList.add("required");
                } else {
                    roleField.required = false;
                    roleLabel.classList.remove("required");
                }
            });
        }
    });
    </script>

</z:page>