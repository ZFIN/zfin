<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="notes" type="java.util.List" required="true" %>



<zfin2:subsection noDataText="None Submitted" title="NOTES" 
                        test="${!empty notes and fn:length(notes)>0}">
    <table width=100% border=0 cellspacing=0>
        <tr bgcolor="#cccccc">
            <td width="30%"><b>Reference</b></td>
            <td><b>Comment</b></td>
        </tr>
        <c:forEach var="note" items="${notes}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td valign="top">
                    <c:forEach var="attribution" items="${note.pubAttributions}">
                        <zfin:link entity="${attribution.publication}"/>
                    </c:forEach>
<%--                    <c:forEach var="attribution" items="${note.personAttributions}">
                        <zfin:link entity="${attribution.person}"/>
                    </c:forEach>--%>
                </td>
                <td>
                    <zfin2:toggleTextLength text="${note.note}" idName="${zfn:generateRandomDomID()}" shortLength="80"/>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</zfin2:subsection>

