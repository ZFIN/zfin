<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="table" class="java.lang.String" scope="request"/>
<jsp:useBean id="ID" class="java.lang.String" scope="request"/>
<jsp:useBean id="query" class="org.zfin.util.DatabaseJdbcStatement" scope="request"/>

<table>
    <tr>
        <th>
            <span class="name-label">Browse Database Tables</span>
        </th>
    </tr>
</table>

<table>
    <tr>
        <th>
            <span class="name-label">Enter ZDB ID:</span>

            <form action="" name="showRecord">
                <label> <input type="text" name="identifier"/> </label>
                <input type="button" name="ID" value="Show" onclick="submitForm();"/>
            </form>
        </th>
    </tr>
</table>

<script type="text/javascript">
    function submitForm() {
        var val = document.showRecord.identifier.value;
        window.location = '/action/database/view-record/' + val;
    }
</script>