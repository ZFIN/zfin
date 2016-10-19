<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publication" type="org.zfin.publication.Publication" required="true" %>
<%@ attribute name="error" type="java.lang.String" %>

<c:if test="${!empty error}">
    <div class="alert alert-danger">${error}</div>
</c:if>

<link rel=stylesheet type="text/css" href="/css/datepicker3.css">
<script src="/javascript/bootstrap-datepicker.js"></script>

<form:form method="POST" commandName="publication" cssClass="form-horizontal">
    <div class="form-group">
        <label for="title" class="col-sm-3 control-label">Title</label>
        <div class="col-sm-8">
            <form:textarea path="title" cssClass="form-control"/>
            <form:errors path="title" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label for="status" class="col-sm-3 control-label">Status</label>
        <div class="col-sm-8">
            <form:select path="status" items="${statusList}" itemLabel="display" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="accessionNumber" class="col-sm-3 control-label">PubMed ID</label>
        <div class="col-sm-8">
            <form:input path="accessionNumber" cssClass="form-control"/>
            <form:errors path="accessionNumber" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label for="doi" class="col-sm-3 control-label">DOI</label>
        <div class="col-sm-8">
            <form:input path="doi" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="authors" class="col-sm-3 control-label">Authors</label>
        <div class="col-sm-8">
            <form:textarea path="authors" cssClass="form-control"/>
            <form:errors path="authors" cssClass="text-danger" />
            <span class="help-block">Standard format: Dole, J.P., Nixon, R.M., and Gingrinch, N.
                <a data-toggle="collapse" href="#authors-more-help"><i class="fa fa-question-circle"></i></a>
            </span>
            <div class="collapse" id="authors-more-help">
                <div class="well">
                    <button type="button" class="close" data-dismiss="alert">
                        <span aria-hidden="true">&times;</span>
                    </button>
                    Use the following formatting guidelines:
                    <ul>
                        <li>Last name, comma, space, initials, then comma to separate from next name</li>
                        <li>No commas or spaces between multiple initials</li>
                        <li>The last name in the listing is preceeded by the word 'and'</li>
                    </ul>
                    For instance, a paper by John Pat Dole, Richard M. Nixon and Newt Gingrinch would be "Dole, J.P., Nixon, R.M., and Gingrinch, N."
                </div>
            </div>
        </div>
    </div>

    <div class="form-group">
        <label for="publicationDate" class="col-sm-3 control-label">Date</label>
        <div class="col-sm-8">
            <form:input path="publicationDate" cssClass="form-control datepicker" data-provide="datepicker"/>
            <form:errors path="publicationDate" cssClass="text-danger" />
            <span class="help-block">(MM/DD/YYYY)</span>
        </div>
    </div>

    <div id="journal" class="form-group">
        <label for="journal" class="col-sm-3 control-label">Journal Abbreviation</label>
        <div class="col-sm-8">
            <div class="scrollable-dropdown-menu">
                <form:input path="journal" cssClass="form-control" id="journal-autocomplete"/>
            </div>
            <form:errors path="journal" cssClass="text-danger" htmlEscape="false" />
        </div>
    </div>

    <div class="form-group">
        <label for="volume" class="col-sm-3 control-label">Journal Volume</label>
        <div class="col-sm-8">
            <form:input path="volume" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="pages" class="col-sm-3 control-label">Pages</label>
        <div class="col-sm-8">
            <form:input path="pages" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="status" class="col-sm-3 control-label">Type</label>
        <div class="col-sm-8">
            <form:select path="type" items="${typeList}" itemLabel="display" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="keywords" class="col-sm-3 control-label">Keywords</label>
        <div class="col-sm-8">
            <form:textarea path="keywords" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="abstractText" class="col-sm-3 control-label">Abstract</label>
        <div class="col-sm-8">
            <form:textarea path="abstractText" cssClass="form-control" rows="8"/>
        </div>
    </div>

    <div class="form-group">
        <label for="errataAndNotes" class="col-sm-3 control-label">Errata & Notes</label>
        <div class="col-sm-8">
            <form:textarea path="errataAndNotes" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="canShowImages" class="col-sm-3 control-label">Has Image Permissions</label>
        <div class="col-sm-8">
            <div class="checkbox">
                <label><form:checkbox path="canShowImages"/></label>
            </div>
            <form:errors path="canShowImages" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-3 col-sm-8">
            <button type="submit" class="btn btn-primary">Submit</button>
            <a class="btn btn-default" href="/${publication.zdbID}">Cancel</a>
        </div>
    </div>
</form:form>

<script>
    $(function () {
        $("#journal-autocomplete").autocompletify(
                "/action/quicksearch/autocomplete?q=%QUERY&category=Autocomplete&type=Journal&rows=10000", {
                    templates: {
                        suggestion: function (item) {
                            return '<div><p class="journal-abbrev">' + item.value + '</p>' +
                                    '<p class="journal-name text-muted">' + item.name + '</p></div>';
                        },
                        empty: "<p class=\"tt-no-results text-danger\">" +
                                "Oof. I couldn't find any journals like that.<br>" +
                                "Perhaps a new one needs to be added." +
                                "</p>"
                    },
                    limit: 10000
                });
        $('#authors-more-help .close').click(function() {
            $('#authors-more-help').collapse('hide');
        });
    });
</script>
