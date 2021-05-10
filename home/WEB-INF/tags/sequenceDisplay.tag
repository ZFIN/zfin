<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships for the transcript page --%>

<%--<%@ attribute name="transcript" type="org.zfin.marker.Transcript" rtexprvalue="true" required="true" %>--%>



<table>
    <%--todo: this should use an attribute, not a formBean, which is a nucleotideSequence --%>

    <c:if test="${empty formBean.nucleotideSequences}">
        <table width="100%">
            <tr>
                <td align="center">
                    <span class="error">Sequence not found.</span>
                </td>
            </tr>
        </table>
    </c:if>
    <c:forEach var="sequence" items="${formBean.nucleotideSequences}" varStatus="index">
        <tr>
            <td>
                    ${sequence.dbLink.accessionNumber}
            </td>
        </tr>

        <tr>
            <td>
                <pre>${sequence.formattedData}</pre>
            </td>
        </tr>
    </c:forEach>



</table>
