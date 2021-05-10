<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Deployed Version">
    <div class="container">
        <table class="table">
            <tr>
                <th>Revision</th>
                <td>${commit}</td>
            </tr>
            <tr>
                <th>Branch</th>
                <td>${branch}</td>
            </tr>
            <tr>
                <th>Domain</th>
                <td>${domain}</td>
            </tr>
        </table>
    </div>
</z:devtoolsPage>