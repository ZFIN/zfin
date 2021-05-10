<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<c:if test="${matchingTextList != null}">
    <table>
        <tr>
            <td valign=top>
                <zfin2:matching-text matchingTextList="${matchingTextList}"/>
            </td>
        </tr>
    </table>
</c:if>
<c:if test="${matchingTextList == null || fn:length(matchingTextList) == 0}">
    No Match found.
</c:if>