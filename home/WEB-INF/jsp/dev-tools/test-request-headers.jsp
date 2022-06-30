<%@ page import="java.util.Date"%>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Browser Test">
    <div class="container">
        <div class="row">
            <table class="table border">

                <tr class="search-result-table-header">
                    <td colspan="2" class="sectionTitle">Request Information</td></tr>
                <tr>
                    <td class="sectionTitle">Property Key</td>
                    <td class="sectionTitle">Property value</td>
                </tr>

                <c:forEach var="result" items="${results}" varStatus="loop">
                    <tr class="devRow">
                        <td>${result.key}</td>
                        <td>${result.value}</td>
                    </tr>
                </c:forEach>
            </table>
        </div>
    </div>

<style>
    table tr.devRow:nth-child(odd) {
        background-color: aliceblue;
    }
</style>

</z:devtoolsPage>