<!-- called by candidate_view.jsp -->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<table class="searchresults">
    <tr>
        <th><!-- hit, blank on purpose --></th>
        <th>Accession</th>
        <th>Gene</th>
        <th>Species</th>
        <th>Score</th>
        <th>Position</th>
        <th>Length</th>
        <th>Expect</th>
        <th>RBH</th>
    </tr>

    <c:forEach var="hit" items="${formBean.hits}" varStatus="loop">
        <tr>
            <c:choose>
                <c:when test="${loop.count % 2 == 0}">
                    <tr class="odd">
                </c:when>
                <c:otherwise>
                    <tr>
                </c:otherwise>
            </c:choose>
         

        </tr>
    </c:forEach>
</table>