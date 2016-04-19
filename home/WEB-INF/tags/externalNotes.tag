<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="notes" type="java.util.List" required="true" %>

<c:if test="${!empty notes and fn:length(notes) > 0}">
    <zfin2:subsection title="NOTES">
        <table class="summary rowstripes">
            <tr>
                <th>Comment</th>
                <th width="30%">Citation</th>
            </tr>
            <c:forEach var="note" items="${notes}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        <zfin2:toggleTextLength text="${note.note}" idName="${zfn:generateRandomDomID()}" shortLength="80"/>
                    </td>
                    <td>
                        <c:forEach var="attribution" items="${note.pubAttributions}">
                            <zfin:link entity="${attribution.publication}"/>
                        </c:forEach>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </zfin2:subsection>
</c:if>
