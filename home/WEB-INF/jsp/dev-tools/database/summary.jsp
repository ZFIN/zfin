<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage>
    <table cellpadding="2" cellspacing="1" border="0" width="70%">
        <tr>
            <td colspan="3" class="sectionTitle"><b>All Instances</b></td>
        </tr>
        <tr>
            <td><a href="overview">Back to overview</a></td>
            <td>&nbsp;</td>
        </tr>
    </table>


    <table width="80%" class="rowstripes summary">
        <tr>
            <th><a href="?orderBy=name">Database Name</a></th>
            <th><a href="?orderBy=owner">Owner</a></th>
            <th><a href="?orderBy=dateCreated">Date Created</a></th>
            <th>Logging enabled</th>
        </tr>
        <c:forEach var="database" items="${databases}" varStatus="loop">
            <tr>
                <td>${database.name}</td>
                <td>${database.owner}</td>
                <td><fmt:formatDate value="${database.dateCreated}" type="date"/></td>
                <td>${database.logging}</td>
            </tr>
        </c:forEach>
    </table>
</z:devtoolsPage>