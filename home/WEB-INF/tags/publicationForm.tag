<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publicationBean" type="org.zfin.publication.presentation.PublicationBean" required="true" %>
<%@ attribute name="error" type="java.lang.String" %>

<c:if test="${!empty error}">
    <div class="alert alert-danger">${error}</div>
</c:if>

<link rel=stylesheet type="text/css" href="/css/datepicker3.css">
<script src="/javascript/bootstrap-datepicker.js"></script>

<form:form method="POST" commandName="publicationBean" cssClass="form-horizontal">
    <div class="form-group">
        <label for="publication.title" class="col-sm-3 control-label">Title</label>
        <div class="col-sm-8">
            <form:textarea path="publication.title" cssClass="form-control"/>
            <form:errors path="publication.title" cssClass="text-danger" />
        </div>
    </div>

    <div class="form-group">
        <label for="publication.status" class="col-sm-3 control-label">Status</label>
        <div class="col-sm-8">
            <form:select path="publication.status" items="${statusList}" itemLabel="display" cssClass="form-control"/>
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
        <label for="publication.doi" class="col-sm-3 control-label">DOI</label>
        <div class="col-sm-8">
            <form:input path="publication.doi" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.authors" class="col-sm-3 control-label">Authors</label>
        <div class="col-sm-8">
            <form:textarea path="publication.authors" cssClass="form-control"/>
            <form:errors path="publication.authors" cssClass="text-danger" />
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
        <label for="publication.publicationDate" class="col-sm-3 control-label">Date</label>
        <div class="col-sm-8">
            <form:input path="publication.publicationDate" cssClass="form-control datepicker" data-provide="datepicker"/>
            <form:errors path="publication.publicationDate" cssClass="text-danger" />
            <span class="help-block">(MM/DD/YYYY)</span>
        </div>
    </div>

    <div id="journal" class="form-group">
        <label for="publication.journal" class="col-sm-3 control-label">Journal Abbreviation</label>
        <div class="col-sm-8">
            <div class="scrollable-dropdown-menu">
                <form:input path="publication.journal" cssClass="form-control" id="journal-autocomplete"/>
            </div>
            <form:errors path="publication.journal" cssClass="text-danger" htmlEscape="false" />
        </div>
    </div>

    <div class="form-group">
        <label for="publication.volume" class="col-sm-3 control-label">Journal Volume</label>
        <div class="col-sm-8">
            <form:input path="publication.volume" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.pages" class="col-sm-3 control-label">Pages</label>
        <div class="col-sm-8">
            <form:input path="publication.pages" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.type" class="col-sm-3 control-label">Type</label>
        <div class="col-sm-8">
            <form:select path="publication.type" items="${typeList}" itemLabel="display" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.keywords" class="col-sm-3 control-label">Keywords</label>
        <div class="col-sm-8">
            <form:textarea path="publication.keywords" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.abstractText" class="col-sm-3 control-label">Abstract</label>
        <div class="col-sm-8">
            <form:textarea path="publication.abstractText" cssClass="form-control" rows="8"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.errataAndNotes" class="col-sm-3 control-label">Errata & Notes</label>
        <div class="col-sm-8">
            <form:textarea path="publication.errataAndNotes" cssClass="form-control"/>
        </div>
    </div>

    <div class="form-group">
        <label for="publication.canShowImages" class="col-sm-3 control-label">Has Image Permissions</label>
        <div class="col-sm-8">
            <div class="checkbox">
                <label><form:checkbox path="publication.canShowImages"/></label>
            </div>
            <form:errors path="publication.canShowImages" cssClass="text-danger" />
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
                "/action/quicksearch/autocomplete?q=%QUERY&category=Journal&rows=10000", {
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
