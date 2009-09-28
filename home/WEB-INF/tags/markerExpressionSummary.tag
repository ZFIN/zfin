<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%-- todo: finish this when we work on transcript expression --%>

<%-- the include below doesn't make intellij happy, so I'm adding taglib directives above --%>


<%@ attribute name="expressionData" type="org.zfin.expression.presentation.MarkerExpression"
    rtexprvalue="true" required="true" %>

<table class="summary">
    <caption>GENE EXPRESSION</caption>
    <tr>
        <td>All expression data:</td>
        <td>
            <a href="">${expressionData.figureCount} Figure(s)</a>
            from ${expressionData.publicationCount} publications
        </td>
    </tr>

    <tr>
        <td>Directly submitted expresion data:</td>
        <td>
            <c:forEach var="probe" items="${expressionData.probes}" varStatus="loop">
                
            </c:forEach>

        </td>
    </tr>
    <tr>
        <td>Wild Type Stages, Structures</td>
        <td>
            <div>
                <zfin:link entity="${expressionData.startStage}"/>
                to <zfin:link entity="${expressionData.endStage}"/>
            </div>
            <div><zfin2:toggledHyperlinkList collection="${expressionData.anatomy}"/><!--todo: finish params--></div>
        </td>
    </tr>
    <tr>
        <td>Curated microarray expression</td>
        <td> </td>
    </tr>

</table>