<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h1><tiles:getAsString name="headerText"/></h1>

<h3>Nomenclature Resources</h3>
<p>
    Before proposing and reserving a name, review the current guidelines and search existing records in ZFIN for
    possible naming conflicts.
</p>
<ul>
    <tiles:insertAttribute name="resources-list"/>
</ul>

<hr>

<form:form action="" method="POST" commandName="submission" cssClass="nomenclature">

    <h3>Contact Information</h3>

    <div>
        <form:label path="name" cssClass="required">Name</form:label>
        <form:input path="name"/>
    </div>

    <%--hidden email field, used for spam prevention--%>
    <div class="alternate-email">
        <form:label path="email">Please leave blank</form:label>
        <form:input path="email"/>
    </div>

    <div>
        <form:label path="email2" cssClass="required">Email</form:label>
        <form:input path="email2"/>
    </div>

    <div>
        <form:label path="laboratory" cssClass="required">Laboratory</form:label>
        <form:input path="laboratory"/>
    </div>

    <tiles:insertAttribute name="submission-form"/>

    <h3>Publication Status</h3>

    <div>
        <form:radiobuttons path="pubStatus" items="${pubStatusOptions}" cssClass="pub-status"/>
        <%--get the validation message to show up in the right place--%>
        <form:label path="pubStatus" cssClass="error"></form:label>
    </div>

    <div class="citations-group">
        <form:label path="citations">Citations</form:label>
        <form:input path="citations"/>
    </div>

    <div class="reserve-group">
        <span class="required">Reserve gene in ZFIN: </span>
        <form:radiobuttons path="reserveType" items="${reserveTypeOptions}"/>
        <%--get the validation message to show up in the right place--%>
        <form:label path="reserveType" cssClass="error"></form:label>
    </div>

    <h3>Additional Comments</h3>

    <form:textarea path="comments" cols="80" rows="5"/>

    <div>
        <input type="submit" value="Submit" />
    </div>

</form:form>

<script src="/javascript/jquery.validate.min.js"></script>
<script>
    function makeDynamicTable(selector, rowCount, namePrefix, properties) {

        function addRow() {
            var row = jQuery("<tr>").appendTo(selector);
            properties.forEach(function (prop) {
                jQuery("<td><input name=\"" + namePrefix + "[" + rowCount + "]." + prop + "\" type=\"text\" /></td>")
                        .appendTo(row);
            });
            rowCount += 1;
        }

        addRow();
        jQuery(selector).after(
                jQuery("<div>").append(
                        jQuery("<a class=\"add-row\">").click(function () { addRow() })
                                .append("<i class=\"fa fa-plus-circle fa-lg\"></i>")
                )
        );
    }

    jQuery(function() {

        jQuery(".citations-group").hide();
        jQuery(".reserve-group").hide();
        jQuery("input[type=radio][name=pubStatus]").change(function () {
            if (this.value == 'Published') {
                jQuery(".citations-group label").addClass("required");
                jQuery(".citations-group").show();
                jQuery(".reserve-group").hide();
            } else {
                jQuery(".citations-group label").removeClass("required");
                jQuery(".citations-group").show();
                jQuery(".reserve-group").show();
            }
        });

        jQuery(".nomenclature").validate({
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
                            return jQuery('input[name=pubStatus]:checked').val() === 'Published';
                        }
                    }
                },
                reserveType: {
                    required: {
                        depends: function () {
                            return jQuery('input[name=pubStatus]:checked').val() !== 'Published';
                        }
                    }
                }
            },
            messages: {
                pubStatus: "Please select an option",
                reserveType: "Please select an option"
            }
        });

        <%-- hide as a spam prevention tactic --%>
        jQuery(".alternate-email").hide();
    });
</script>
