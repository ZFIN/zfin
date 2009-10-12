<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="formBean" type="org.zfin.sequence.blast.presentation.XMLBlastBean" %>

<table width="100%">
    <tr>

        <td align="left" valign="top" width="30%">
            <strong>Currently Viewing:</strong>
            <a href="/action/blast/blast-view?resultFile=${formBean.ticketNumber}">${formBean.ticketNumber}</a>
            <br>
            <span style="font-size:small;">Links will be active for one week.</span>
            <span style="font-size:small;">
                <a href="/action/blast/blast?previousSearch=${formBean.ticketNumber}">Edit&nbsp;and&nbsp;resubmit</a>
                </span>
            <c:if test="${!empty formBean.blastOutput.blastOutputIterations.iteration[0].iterationMessage}">
                <br>
                <c:choose>
                    <c:when test="${ ( fn:contains(formBean.blastOutput.blastOutputIterations.iteration[0].iterationMessage.content, 'EXIT: 13')
                        || fn:contains(formBean.blastOutput.blastOutputIterations.iteration[0].iterationMessage.content, 'EXIT: 16') ) }">
                        <span style="font-size: small;" class="error-inline">
                            No query sequence was received by blast.
                            Try changing filter parameters.
                        </span>
                    </c:when>
                    <c:otherwise>
                        <span style="font-size: small;" class="error-inline">
                            Error message from blast server: <br>
                       ${formBean.blastOutput.blastOutputIterations.iteration[0].iterationMessage.content}
                        </span>
                    </c:otherwise>
                </c:choose>
                <%--<span style="font-size: small;" class="error-inline">${formBean.blastOutput.blastOutputIterations.iteration[0].iterationMessage.content}</span>--%>
            </c:if>
        </td>

        <td valign="top" width="60%">

            <%--if there are more than one sequences, then show the other links--%>
            <c:if test="${fn:length(formBean.blastResultBean.tickets)>1}">
                <strong>Other Results:</strong>
                <c:forEach var="ticket" items="${formBean.blastResultBean.tickets}" varStatus="loopStatus">
                    <c:choose>
                        <c:when test="${ticket eq formBean.ticketNumber}">
                            <span style="color: gray;"> ${ticket} </span>
                        </c:when>
                        <c:otherwise>
                            <a href="/action/blast/blast-view?resultFile=${ticket}">${ticket}</a>
                        </c:otherwise>
                    </c:choose>

                    <%--add a separator pipe--%>
                    <c:if test="${!loopStatus.last}">|</c:if>
                </c:forEach>
            </c:if>
            <br>
        </td>
    </tr>

</table>

