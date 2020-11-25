<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.zebrashare.presentation.LineEditBean" scope="request"/>

<z:page bootstrap="true">
    <div class="container-fluid">
        <c:if test="${!empty publication}">
            <span class="float-right">
                Edit other line associated with ${publication.shortAuthorList}:
                <select id="other-lines">
                    <c:forEach items="${otherFeatures}" var="other">
                        <option value="${other.zdbID}" ${feature.zdbID == other.zdbID ? "selected" : ""}>${other.name}</option>
                    </c:forEach>
                </select>
            </span>
        </c:if>

        <a href="/action/zebrashare/dashboard">View Your ZebraShare Submissions</a>

        <h1>${feature.name}</h1>

        <c:if test="${!empty error}">
            <div class="alert alert-danger" role="alert">${error}</div>
        </c:if>

        <c:if test="${!empty success}">
            <div class="alert alert-success" role="alert">${success}</div>
        </c:if>

        <form:form method="POST" cssClass="form-horizontal" commandName="formBean">
            <div class="form-group row">
                <form:label cssClass="col-md-3 col-form-label" path="functionalConsequence">Functional Consequence</form:label>
                <div class="col-md-3">
                    <form:select path="functionalConsequence" cssClass="form-control">
                        <form:option value="" />
                        <form:options items="${functionalConsequenceList}" itemLabel="display" />
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="adultViable" cssClass="col-md-3 col-form-label">Adult Viable</form:label>
                <div class="col-md-3">
                    <form:select path="adultViable" cssClass="form-control">
                        <form:option value="">Unknown</form:option>
                        <form:option value="true">Yes</form:option>
                        <form:option value="false">No</form:option>
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="maternalZygosityExamined" cssClass="col-md-3 col-form-label">Maternal Zygocity Examined</form:label>
                <div class="col-md-3">
                    <form:select path="maternalZygosityExamined" cssClass="form-control">
                        <form:option value="">Unknown</form:option>
                        <form:option value="true">Yes</form:option>
                        <form:option value="false">No</form:option>
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label cssClass="col-md-3 col-form-label" path="nmdApparent">NMD Apparent</form:label>
                <div class="col-md-3">
                    <form:select path="nmdApparent" cssClass="form-control">
                        <form:option value="" />
                        <form:options items="${nmdApparentList}" itemLabel="display" />
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="currentlyAvailable" cssClass="col-md-3 col-form-label">Currently Available</form:label>
                <div class="col-md-3">
                    <form:select path="currentlyAvailable" cssClass="form-control">
                        <form:option value="">Unknown</form:option>
                        <form:option value="true">Yes</form:option>
                        <form:option value="false">No</form:option>
                    </form:select>
                </div>
            </div>

            <div class="form-group row">
                <form:label path="otherLineInformation" cssClass="col-md-3 col-form-label">Other Line Information</form:label>
                <div class="col-md-9">
                    <form:textarea path="otherLineInformation" cssClass="form-control" rows="5"/>
                </div>
            </div>

            <div class="form-group row">
                <div class="offset-md-3 col-md-8">
                    <button type="submit" class="btn btn-primary">Save</button>
                </div>
            </div>
        </form:form>
    </div>

    <script>
        $(function () {
            var formChanged = false;
            $('#other-lines').on('change', function () {
                window.location.href = '/action/zebrashare/line-edit/' + $(this).val();
            });
            $('#formBean').on('change', function () {
                formChanged = true;
            });
            $('#formBean').on('submit', function () {
                formChanged = false;
            })
            $(window).on('beforeunload', function () {
                if (formChanged) {
                    return 'Unsaved changes';
                }
            })
        });
    </script>
</z:page>