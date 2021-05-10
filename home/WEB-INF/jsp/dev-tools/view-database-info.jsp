<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Metadata">
    <table cellpadding="2" cellspacing="1" border="0" width="50%">
        <tr>
            <td colspan=2 class="sectionTitle">Database Info</td>
        </tr>
        <tr>
            <td class="listContent">
                Database Vendor
            </td>
            <td class="listContent">
                <c:out value="${metadata.databaseProductName}" />
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Database Version
            </td>
            <td class="listContent">
                <c:out value="${metadata.databaseProductVersion}" />
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Major Version
            </td>
            <td class="listContent">
                <c:out value="${metadata.databaseMajorVersion}" />
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Minor Version
            </td>
            <td class="listContent">
                <c:out value="${metadata.databaseMinorVersion}" />
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Default Transaction Isolation
            </td>
            <td class="listContent">
                <c:out value="${metadata.defaultTransactionIsolation}" />
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Transaction Isolation on this connection
            </td>
            <td class="listContent">
                ${conn.transactionIsolation}
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Catalog
            </td>
            <td class="listContent">
                ${conn.catalog}
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Extra Name Characters
            </td>
            <td class="listContent">
                <c:out value="${metadata.extraNameCharacters}" />
            </td>
        </tr>

        <tr>
            <td class="listContent">
                Unload Date
            </td>
            <td class="listContent">
                <fmt:formatDate pattern="d MMM yyyy hh:mm a" value="${unloadDate.date}"/>
            </td>
        </tr>
    </table>
</z:devtoolsPage>