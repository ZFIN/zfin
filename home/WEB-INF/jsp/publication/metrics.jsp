<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.publication.presentation.PublicationMetricsFormBean" scope="request"/>

<z:page>
    <c:if test="${!empty errors}">
        <div class="no-results-found-message">
            <ul style="text-align: left;">
                <c:forEach items="${errors}" var="error"><li>${error}</li></c:forEach>
            </ul>
        </div>
    </c:if>

    <form:form method="GET" modelAttribute="formBean">
        <table class="primary-entity-attributes metrics-table">
            <tr>
                <th width="75px">Dates</th>
                <td>
                    <form:select path="queryType">
                        <form:option value="" />
                        <form:options items="${queryTypes}" itemLabel="display" />
                    </form:select>
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
                <td></td>
                <td>
                    <ul class="metrics-help pet-date">
                        <li>Dates are publication PET dates</li>
                        <li>Counts are number of publications <i>currently</i> in the listed state (i.e. curation status, location, indexed status)</li>
                        <li>The "All" row displays the total number of publications with the given PET date, regarless of which states are shown</li>
                    </ul>
                    <ul class="metrics-help status-date">
                        <li>Dates are the date a publication changed state (i.e. curation status, location, indexed status)</li>
                        <li>Counts are number of publications which changed to the listed state on that date</li>
                        <li>Because multiple state changes for a single publication can occur on the same day, month, or year, the same publication may be counted in multiple rows</li>
                    </ul>
                    <ul class="metrics-help cumulative">
                        <li>Statistics are computed based on how many days a publication spent in a given status or location <b>once it has left that status or location</b></li>
                        <li>Use caution when interpreting these statistics for Closed statuses because most papers never leave the Closed status</li>
                        <li>This query mimics the old Average-Time-In-Bins-Cumulative_m Jenkins job</li>
                    </ul>
                    <ul class="metrics-help snapshot">
                        <li>Statistics are computed based on how many days a publication has spent in a given status or location <b>up until now</b></li>
                        <li>This query gives a snapshot of the current state of the system. Therefore no dates can be provided.</li>
                    </ul>
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
                            <div style="margin-top: 0.5rem">
                                <span style="margin-right: 0.5rem">
                                    <button role="button" class="toggle-all" data-toggle-prefix="CLOSED" data-toggle-val="true">Check all Closed</button>
                                    <button role="button" class="toggle-all" data-toggle-prefix="CLOSED" data-toggle-val="false">Uncheck all Closed</button>
                                </span>
                                <span>
                                    <button role="button" class="toggle-all" data-toggle-prefix="WAITING" data-toggle-val="true">Check all Waiting</button>
                                    <button role="button" class="toggle-all" data-toggle-prefix="WAITING" data-toggle-val="false">Uncheck all Waiting</button>
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
                    <td class="right-align">
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
                    $('.metrics-help').hide();
                    if (this.value === 'CUMULATIVE') {
                        $('[name="fromDate"]').attr("disabled", "disabled").val('');
                        $('[name="toDate"]').removeAttr("disabled", "disabled");
                        $('[name="groupBy"]').attr("disabled", "disabled");
                        $('[value="ACTIVE"]').attr("disabled", "disabled");
                        $('[value="INDEXED"]').attr("disabled", "disabled");
                        $('.metrics-help.cumulative').show();
                    } else if (this.value === 'STATUS_DATE') {
                        $('[name="fromDate"]').removeAttr("disabled");
                        $('[name="toDate"]').removeAttr("disabled");
                        $('[name="groupBy"]').removeAttr("disabled");
                        $('[value="ACTIVE"]').attr("disabled", "disabled");
                        $('[value="INDEXED"]').removeAttr("disabled");
                        $('.metrics-help.status-date').show();
                    } else if (this.value === 'PET_DATE') {
                        $('[name="fromDate"]').removeAttr("disabled");
                        $('[name="toDate"]').removeAttr("disabled");
                        $('[name="groupBy"]').removeAttr("disabled");
                        $('[value="ACTIVE"]').removeAttr("disabled");
                        $('[value="INDEXED"]').removeAttr("disabled");
                        $('.metrics-help.pet-date').show();
                    } else if (this.value === 'SNAPSHOT') {
                        $('[name="fromDate"]').attr("disabled", "disabled").val('');
                        $('[name="toDate"]').attr("disabled", "disabled").val('');
                        $('[name="groupBy"]').attr("disabled", "disabled");
                        $('[value="ACTIVE"]').attr("disabled", "disabled");
                        $('[value="INDEXED"]').attr("disabled", "disabled");
                        $('.metrics-help.snapshot').show();
                    }
                    if ($('[name="groupType"]').find(":selected").attr("disabled")) {
                        $('[name="groupType"]').val(null).trigger('change');
                    }
                });
            $('[value="PET_DATE"]').trigger('change');

            $('.toggle-all').on('click', function (e) {
                e.preventDefault();
                var $this = $(this);
                var val = $this.data('toggle-val');
                var prefix = $this.data('toggle-prefix');
                $('[name="statuses"]')
                    .filter(function () { return this.value.startsWith(prefix) })
                    .prop('checked', val);
            });

            //set labels so locations '1', '2', '3' are displayed as 'Priority 1', 'Priority 2', 'Priority 3'
            document.querySelector('input[value=INDEXER_PRIORITY_1]').nextElementSibling.innerText = 'Priority 1';
            document.querySelector('input[value=INDEXER_PRIORITY_2]').nextElementSibling.innerText = 'Priority 2';
            document.querySelector('input[value=INDEXER_PRIORITY_3]').nextElementSibling.innerText = 'Priority 3';
        });
    </script>
</z:page>