<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="data_manager">
    <tbody>
    <tr>
        <td>
            <strong>OBO ID:</strong>&nbsp;${term.oboID}
        </td>
    </tr>
    </tbody>
</table>

<zfin2:citationList pubListBean="${citationList}" url="/action/ontology/disease-publication-list/${term.oboID}?">

    <table class="primary-entity-attributes">
        <tr>
            <th>Term Name</th>
            <td><a href="/action/ontology/term-detail/${term.oboID}">${term.termName}</a></td>
        </tr>
    </table>

</zfin2:citationList>
