<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="formBean" type="org.zfin.zebrashare.presentation.SubmissionFormBean" required="true" %>

<form:form method="POST" commandName="formBean" class="form-horizontal" id="zebrashareForm" enctype="multipart/form-data">
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
            <form:errors path="abstractText" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Submitter Name</label>
        <div class="col-sm-8">
            <form:input path="submitterName" cssClass="form-control"/>
            <form:errors path="submitterName" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Submitter Email</label>
        <div class="col-sm-8">
            <form:input path="submitterEmail" cssClass="form-control"/>
            <form:errors path="submitterEmail" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Lab of Origin</label>
        <div class="col-sm-8">
        <c:choose>
            <c:when test="${!empty labOptions}">
                <form:select path="labZdbId" items="${labOptions}" itemLabel="name" itemValue="zdbID" cssClass="form-control"/>
                <span class="help-block">
                    Don't see your lab listed here? Please
                    <zfin2:mailTo subject="Lab update request">contact us</zfin2:mailTo> to update your information.
                </span>
            </c:when>
            <c:otherwise>
                <p class="text-danger form-control-static">
                    <b>Uh oh!</b> Your profile is not associated with any labs. Please
                    <zfin2:mailTo subject="Lab update request">contact us</zfin2:mailTo> to update your information
                    before proceeding.
                </p>
            </c:otherwise>
        </c:choose>
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Who Can Edit Alleles Associated With This Publication?</label>
        <div class="col-sm-8">
            <input id="userLookup" class="form-control" />
            <div id="selectedUsers"></div>
        </div>
    </div>

    <div class="form-group">
        <label class="col-sm-3 control-label">Data File</label>
        <div class="col-sm-8">
            <input type="file" name="dataFile" cssClass="form-control"
                   accept=".xls,.xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"/>
            <form:errors path="dataFile" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-8">
            <button type="submit" class="btn btn-primary">Submit</button>
            <a class="btn btn-default" href="/">Cancel</a>
        </div>
    </div>
</form:form>

<script>
    $(function () {
        var form = $('#zebrashareForm');
        form.on('keyup keypress', function(e) {
            var keyCode = e.keyCode || e.which;
            if (keyCode === 13) {
                e.preventDefault();
                return false;
            }
        });
        $('#userLookup')
            .autocompletify('/action/profile/find-member?term=%QUERY')
            .on('typeahead:select', function(event, item) {
                $(this).typeahead('val', '');
                var hiddenInput = $('<input hidden name="editors" value="' + item.id  + '"/>');
                var userDisplay = $('<p class="form-control-static">' + item.value + '</p>');
                var removeButton = $('<button class="btn btn-link"><i class="fas fa-times"></i></button>');
                removeButton.on('click', function () {
                    hiddenInput.remove();
                    userDisplay.remove();
                });
                userDisplay.append(removeButton);
                form.append(hiddenInput);
                $('#selectedUsers').append(userDisplay);
            });
    });
</script>