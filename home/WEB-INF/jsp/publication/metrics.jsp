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
                <form:input path="fromDate" />
                <b>To</b>
                <form:input path="toDate" />
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
