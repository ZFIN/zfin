<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<table class="gene-expression-data-modal">
    <tr>
        <td colspan="2" style="text-align: right;">${allExpressionLink}</td>
    </tr>
    <tr>
        <td colspan="2" style="text-align: right;">${wtExpressionLink}</td>
    </tr>
    <tr>
      <th></th>
      <th></th>
    </tr>
    <c:forEach var="entry" items="${expressionTermLinks}">
        <tr>
            <td style="padding-right: 1em;">${entry.key}</td>
            <td>${entry.value}</td>
        </tr>
    </c:forEach>
</table>
