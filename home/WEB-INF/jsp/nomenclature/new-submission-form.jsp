<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>

<div class="container-fluid">
    <h1><tiles:getAsString name="headerText"/></h1>

    <h3>Nomenclature Resources</h3>
    <p>
        Before proposing and reserving a name, review the current guidelines and search existing records in ZFIN for
        possible naming conflicts.
    </p>
    <ul>
        <tiles:insertAttribute name="resources-list"/>
    </ul>
    <p>
        <b>Need additional assistance? <a href="mailto:<%= ZfinPropertiesEnum.NOMEN_COORDINATOR.value()%>">Contact the
        Nomenclature Coordinator</a></b>
    </p>

    <hr>

    <form:form action="" method="POST" commandName="submission" cssClass="form-horizontal" id="nomenclature">

        <h3>Contact Information</h3>

        <div class="form-group">
            <form:label path="name" cssClass="col-sm-2 control-label required">Name</form:label>
            <div class="col-sm-4">
                <form:input path="name" cssClass="form-control"/>
            </div>
        </div>

        <%--hidden email field, used for spam prevention--%>
        <div class="alternate-email">
            <form:label path="email" cssClass="col-sm-2 control-label">Please leave blank</form:label>
            <div class="col-sm-4">
                <form:input path="email" cssClass="form-control"/>
            </div>
        </div>

        <div class="form-group">
            <form:label path="email2" cssClass="col-sm-2 control-label required">Email</form:label>
            <div class="col-sm-4">
                <form:input path="email2" cssClass="form-control"/>
            </div>
        </div>

        <div class="form-group">
            <form:label path="laboratory" cssClass="col-sm-2 control-label required">Laboratory</form:label>
            <div class="col-sm-4">
                <form:input path="laboratory" cssClass="form-control"/>
            </div>
        </div>

        <tiles:insertAttribute name="submission-form"/>

        <h3>Publication Status</h3>

        <div class="form-group">
            <div class="col-sm-4">
                <c:forEach items="${pubStatusOptions}" var="option">
                    <div class="radio">
                        <label>
                            <form:radiobutton path="pubStatus" value="${option}"/>
                            ${option}
                        </label>
                    </div>
                </c:forEach>
            </div>
        </div>

        <div class="form-group citations-group">
            <form:label path="citations" cssClass="col-sm-2 control-label">Citations</form:label>
            <div class="col-sm-6">
                <form:input path="citations" cssClass="form-control"/>
            </div>
        </div>

        <div class="form-group reserve-group">
            <label class="col-sm-2 control-label required">Reserve name</label>
            <div class="col-sm-6">
                <c:forEach items="${reserveTypeOptions}" var="option">
                    <label class="radio-inline">
                        <form:radiobutton path="reserveType" value="${option}"/> ${option}
                    </label>
                </c:forEach>
            </div>
        </div>

        <h3>Additional Comments</h3>

        <div class="form-group">
            <div class="col-sm-8">
                <form:textarea path="comments" cols="80" rows="5" cssClass="form-control"/>
            </div>
        </div>

        <button type="submit" class="btn btn-default">Submit</button>
    </form:form>
</div>

<script src="/css/bootstrap3/js/bootstrap.min.js"></script>
<script src="/javascript/jquery.validate.min.js"></script>
<script src="/javascript/multirow-table.js"></script>
<script>
    $(function() {

        $(".citations-group").hide();
        $(".reserve-group").hide();
        $("input[type=radio][name=pubStatus]").change(function () {
            if (this.value == 'Published') {
                $(".citations-group label").addClass("required");
                $(".citations-group").show();
                $(".reserve-group").hide();
            } else {
                $(".citations-group label").removeClass("required");
                $(".citations-group").show();
                $(".reserve-group").show();
            }
        });

        $("#nomenclature").validate({
            rules: {
                name: { required: true },
                email2: {
                    required: true,
                    email: true
                },
                laboratory: { required: true },
                geneName: { required: true },
                geneSymbol: { required: true },
                pubStatus: { required: true },
                citations: {
                    required: {
                        depends: function () {
                            return $('input[name=pubStatus]:checked').val() === 'Published';
                        }
                    }
                },
                reserveType: {
                    required: {
                        depends: function () {
                            return $('input[name=pubStatus]:checked').val() !== 'Published';
                        }
                    }
                }
            },
            messages: {
                pubStatus: "Please select an option",
                reserveType: "Please select an option"
            },
            highlight: function(el) {
                $(el).closest('.form-group').addClass('has-error');
            },
            unhighlight: function(el) {
                $(el).closest('.form-group').removeClass('has-error');
            },
            errorElement: 'span',
            errorClass: 'help-block',
            errorPlacement: function (error, element) {
                if (element.is(':radio')) {
                    error.prependTo(element.closest('.radio').parent());
                } else {
                    error.insertAfter(element);
                }
            }
        });

        <%-- hide as a spam prevention tactic --%>
        $(".alternate-email").hide();
    });
</script>
