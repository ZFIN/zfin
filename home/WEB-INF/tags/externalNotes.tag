<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="notes" type="java.util.List" required="true" %>



<zfin2:subsectionMarker noDataText="None Submitted" title="NOTES"
                        test="${!empty notes and fn:length(notes)>0}">
    <table width=100% border=0 cellspacing=0>
        <tr bgcolor="#cccccc">
            <td width="30%"><b>Reference</b></td>
            <td><b>Comment</b></td>
        </tr>
        <c:forEach var="extnote" items="${notes}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td valign="top">
                    <zfin:link entity="${extnote.singlePubAttribution.publication}"/>
                </td>
                <td>
                    <zfin2:toggleTextLength text="${extnote.note}" idName="${loop.index}" shortLength="80"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</zfin2:subsectionMarker>

