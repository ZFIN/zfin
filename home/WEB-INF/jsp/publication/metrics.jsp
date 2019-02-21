<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.publication.presentation.PublicationMetricsFormBean" scope="request"/>

<form:form method="GET" commandName="formBean">
    <table class="primary-entity-attributes metrics-table">
        <tr>
            <th>Dates</th>
            <td>
                <form:select path="queryType" items="${queryTypes}" itemLabel="display" />
                <form:label path="fromDate">From</form:label>
                <form:input path="fromDate" cssClass="date-mask" />
                <form:label path="toDate">To</form:label>
                <form:input path="toDate" cssClass="date-mask" />
                <form:label path="groupBy">By</form:label>
                <form:select path="groupBy" items="${intervals}" itemLabel="display" />
            </td>
        </tr>

        <tr style="display: none;">
            <th>Show</th>
            <td>
                <div class="metrics-checkboxes">
                    <form:checkboxes path="statistics" items="${statistics}" itemLabel="display" />
                </div>
            </td>
        </tr>

        <tr>
            <th>Group By</th>
            <td>
                <form:select path="groupType" items="${groupTypes}" itemLabel="display" />
                <div id="group-by-checkboxes">
                    <div style="display: ${formBean.groupType == 'ACTIVE' ? 'block' : 'none'};" class="metrics-checkboxes ACTIVE">
                        <form:checkboxes path="activationStatuses" items="${activationStatuses}" itemLabel="display" />
                    </div>
                    <div style="display: ${formBean.groupType == 'STATUS' ? 'block' : 'none'};" class="metrics-checkboxes STATUS">
                        <form:checkboxes path="statuses" items="${statuses}" itemLabel="display" />
                    </div>
                </div>
            </td>
        </tr>

        <tr>
            <td colspan="2"><form:checkbox path="currentStatusOnly" label="Current Status Only" /></td>
        </tr>

        <tr>
            <td colspan="2"><button type="submit">Submit</button></td>
        </tr>
    </table>
</form:form>

<table class="metrics-results">
    <c:forEach items="${resultsTable}" var="row" varStatus="rowLoop">
        <c:if test="${rowLoop.first}">
            <tr>
                <td></td>
                <c:forEach items="${row.value}" var="column">
                    <td>${column.key}</td>
                </c:forEach>
            </tr>
        </c:if>
        <tr>
            <td>${row.key}</td>
            <c:forEach items="${row.value}" var="column">
                <td>${column.value}</td>
            </c:forEach>
        </tr>
    </c:forEach>
</table>

<script>
    $(function () {
        $('.date-mask')
            .attr('placeholder', 'yyyy-mm-dd')
            .on('change keyup', function (evt) {
                evt.preventDefault();
                var value = $(this).val();
                // special handling for backspace or delete
                if (evt.which === 8 || evt.which === 46) {
                    if (value.endsWith('-')) {
                        $(this).val(value.slice(0, -2));
                    }
                    return false;
                }
                if (value.length === 4 || value.length === 7) {
                    $(this).val(value + '-');
                }
            });

        $('[name="groupType"]')
            .on('change', function () {
                $('#group-by-checkboxes').children().hide();
                $('#group-by-checkboxes').find('.' + this.value).show();
            });

        $('[name="queryType"]')
            .on('change', function () {
                if (this.value === 'CUMULATIVE') {
                    $('[name="statistics"]').removeAttr("disabled");
                } else {
                    $('[value="COUNT"]').attr('checked', 'checked');
                    $('[name="statistics"]').attr("disabled", "disabled")
                }
            });
        $('[value="PET_DATE"]').trigger('change');
    });
</script>