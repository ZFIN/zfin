<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.publication.presentation.PublicationMetricsFormBean" scope="request"/>

<c:if test="${!empty errors}">
    <div class="no-results-found-message">
        <ul style="text-align: left;">
            <c:forEach items="${errors}" var="error"><li>${error}</li></c:forEach>
        </ul>
    </div>
</c:if>

<form:form method="GET" commandName="formBean">
    <table class="primary-entity-attributes metrics-table">
        <tr>
            <th>Dates</th>
            <td>
                <form:select path="queryType" items="${queryTypes}" itemLabel="display" />
                <span class="control-group">
                    <form:input path="fromDate" cssClass="date-mask" />
                    <form:label path="fromDate">From</form:label>
                </span>
                <span class="control-group">
                    <form:input path="toDate" cssClass="date-mask" />
                    <form:label path="toDate">To</form:label>
                </span>
                <span class="control-group">
                    <form:select path="groupBy" items="${intervals}" itemLabel="display" />
                    <form:label path="groupBy">By</form:label>
                </span>
            </td>
        </tr>

        <tr>
            <th>Group By</th>
            <td>
                <form:select path="groupType">
                    <form:option value="" />
                    <form:options items="${groupTypes}" itemLabel="display" />
                </form:select>
                <div id="group-by-checkboxes">
                    <div style="display: ${formBean.groupType == 'ACTIVE' ? 'block' : 'none'};" class="metrics-checkboxes ACTIVE">
                        <form:checkboxes path="activationStatuses" items="${activationStatuses}" itemLabel="display" />
                    </div>
                    <div style="display: ${formBean.groupType == 'INDEXED' ? 'block' : 'none'};" class="metrics-checkboxes INDEXED">
                        <form:checkboxes path="indexedStatuses" items="${indexedStatuses}" />
                    </div>
                    <div style="display: ${formBean.groupType == 'STATUS' ? 'block' : 'none'};" class="STATUS">
                        <div class="metrics-checkboxes">
                            <form:checkboxes path="statuses" items="${statuses}" itemLabel="display" />
                        </div>
                        <div class="metrics-checkboxes">
                            <span>
                                <input id="all-closed" class="toggle-all" type="checkbox" checked="checked" data-toggle-prefix="CLOSED">
                                <label for="all-closed">All Closed</label>
                            </span>
                            <span>
                                <input id="all-waiting" class="toggle-all" type="checkbox" checked="checked" data-toggle-prefix="WAITING">
                                <label for="all-waiting">All Waiting</label>
                            </span>
                        </div>
                    </div>
                    <div style="display: ${formBean.groupType == 'LOCATION' ? 'block' : 'none'};" class="metrics-checkboxes LOCATION">
                        <form:checkboxes path="locations" items="${locations}" itemLabel="display" />
                    </div>
                </div>
            </td>
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
        <tr ${row.key == 'All' ? 'style="font-weight: bold;"' : ''}>
            <td>${row.key}</td>
            <c:forEach items="${row.value}" var="column">
                <td>
                    <c:if test="${column.value == null}">--</c:if>
                    <fmt:formatNumber value="${column.value}" maxFractionDigits="1" />
                </td>
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
                    return false;
                }
                if (value.length === 4 || value.length === 7) {
                    $(this).val(value + '-');
                }
            });

        $('[name="groupType"]')
            .on('change', function () {
                $('#group-by-checkboxes').children().hide();
                if (this.value) {
                    $('#group-by-checkboxes').find('.' + this.value).show();
                }
            });

        $('[name="queryType"]')
            .on('change', function () {
                if (this.value === 'CUMULATIVE') {
                    $('[name="fromDate"]').attr("disabled", "disabled");
                    $('[name="groupBy"]').attr("disabled", "disabled");
                    $('[value="ACTIVE"]').attr("disabled", "disabled");
                    $('[value="INDEXED"]').attr("disabled", "disabled");
                } else if (this.value === 'STATUS_DATE') {
                    $('[name="fromDate"]').removeAttr("disabled");
                    $('[name="groupBy"]').removeAttr("disabled");
                    $('[value="ACTIVE"]').attr("disabled", "disabled");
                    $('[value="INDEXED"]').removeAttr("disabled");
                } else {
                    $('[name="fromDate"]').removeAttr("disabled");
                    $('[name="groupBy"]').removeAttr("disabled");
                    $('[value="ACTIVE"]').removeAttr("disabled");
                    $('[value="INDEXED"]').removeAttr("disabled");
                }
                if ($('[name="groupType"]').find(":selected").attr("disabled")) {
                    $('[name="groupType"]').val(null).trigger('change');
                }
            });
        $('[value="PET_DATE"]').trigger('change');

        $('.toggle-all').on('change', function () {
            var $this = $(this);
            var val = $this.is(':checked');
            var prefix = $this.data('toggle-prefix');
            $('[name="statuses"]')
                .filter(function () { return this.value.startsWith(prefix) })
                .prop('checked', val);
        });
    });
</script>