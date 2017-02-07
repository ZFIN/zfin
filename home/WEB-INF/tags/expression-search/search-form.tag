<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="title" type="java.lang.String" required="true" %>
<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<div class="titlebar">
    <h1>${title}</h1>
    <a href="/ZFIN/misc_html/xpatselect_search_tips.html" class="popup-link help-popup-link"
       id="xpatsel_expression_tips" rel="#searchtips"></a>
</div>

<form:form action="/action/expression/results" method="get" modelAttribute="criteria">
    <form:hidden path="rows"/>
    <table class="primary-entity-attributes">
        <tr>
            <th><form:label path="geneField" cssClass="namesearchLabel">Gene/EST</form:label></th>
            <td><form:input type="text" path="geneField" cssClass="form-control"/></td>
        </tr>
    </table>
    <div class="submitbar">
        <button type="submit">Search</button>
        <button type="reset">Reset</button>
    </div>
</form:form>