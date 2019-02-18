<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.publication.presentation.PublicationMetricsFormBean" scope="request"/>

<form:form method="GET" commandName="formBean">
    <table class="primary-entity-attributes metrics-table">
        <tr>
            <td colspan="2"><form:radiobuttons path="queryType" items="${queryTypes}" itemLabel="display" /></td>
        </tr>

        <tr>
            <th>From</th>
            <td>
                <form:input path="fromDate" cssClass="date-mask" />
                <b>To</b>
                <form:input path="toDate" cssClass="date-mask" />
                <b>By</b>
                <form:select path="groupBy" items="${intervals}" itemLabel="display" />
            </td>
        </tr>

        <tr>
            <th>Show</th>
            <td>
                <div class="metrics-checkboxes">
                    <form:checkboxes path="statistics" items="${statistics}" itemLabel="display" />
                </div>
            </td>
        </tr>

        <tr>
            <th>Status</th>
            <td>
                <div class="metrics-checkboxes">
                    <form:checkboxes path="statuses" items="${statuses}" itemLabel="display" />
                </div>
            </td>
        </tr>

        <tr>
            <th>Location</th>
            <td>
                <div class="metrics-checkboxes">
                        <form:checkboxes path="locations" items="${curatingLocations}" itemLabel="display" />
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

<%--<table>--%>
    <%--<c:forEach items="${results}" var="result">--%>
        <%--<tr>--%>
            <%--<td>${result.status}</td>--%>
            <%--<td>${result.location}</td>--%>
            <%--<td>${result.date}</td>--%>
            <%--<td>${result.count}</td>--%>
        <%--</tr>--%>
    <%--</c:forEach>--%>
<%--</table>--%>

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
    });
</script>