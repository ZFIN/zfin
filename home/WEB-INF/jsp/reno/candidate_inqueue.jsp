<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>
<%@ page import="org.zfin.sequence.reno.presentation.RunBean" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<c:choose>
<c:when test='${formBean.run == null}'>
    <tr>
        <td><font size=+1 color="red" face="Arial">${formBean.zdbID} is not a valid run zdbID</font></td>
        <td><a href="/action/reno/run-list">View All Runs</a></td>
    </tr>
</c:when>

<c:otherwise>

<h3>Candidates In Queue</h3>
<table width=100%>
    <tr>
        <td>Run Name: ${formBean.run.name}</td>
        <td><a href="/action/reno/run-list">View All Runs</a></td>
    </tr>
</table>

<table>
    <tr>
        <td>Number of Finished: ${formBean.run.finishedCandidateCount}</td>
    </tr>
    <tr>
        <td>Number Pending:
            <c:choose>
                <c:when test="${formBean.run.pendingCandidateCount > 0}">
                    <a href="/action/reno/candidate-pending?zdbID=${formBean.run.zdbID}">
                            ${formBean.run.pendingCandidateCount}
                    </a>
                </c:when>
                <c:otherwise>${formBean.run.pendingCandidateCount}</c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td>Number in Queue: ${formBean.run.queueCandidateCount}</td>
    </tr>
    <form:form commandName="formBean">

        <tr>
            <td>
                    <%--<label for="nomenclaturePublicationZdbID" class="indented-label" />--%>
                <form:label path="<%= RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID%>"
                            cssClass="indented-label">Nomenclature Publication:</form:label>

                <form:input path="<%= RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID%>" size="30"/>
            </td>
        </tr>
        <tr>
            <td>
                <form:errors path="<%= RunBean.NOMENCLATURE_PUBLICATION_ZDB_ID%>" cssClass="error"/>
            </td>
        </tr>


        <c:choose>
            <c:when test="${formBean.run.type eq 'Nomenclature'}">
                <tr>
                    <td>
                        Orthology Publication:
                        <form:input path="<%= RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID%>" size="30"/>
                    </td>
                </tr>
                <tr>
                    <td>
                        <c:if test="${formBean.orthologyPublicationZdbID eq null}">
                            <div class="Error">
                                Orthology publication required to save changes!
                            </div>
                        </c:if>
                        <form:errors path="<%= RunBean.ORTHOLOGY_PUBLICATION_ZDB_ID%>" cssClass="error"/>
                    </td>
                </tr>
            </c:when>
            <%-- %>redundancy run--%>
            <c:otherwise>
                <tr>
                    <td>
                        <form:label path="<%= RunBean.RELATION_PUBLICATION_ZDB_ID%>"
                                    cssClass="indented-label">Link Publication:
                        </form:label>
                        <form:input path="<%= RunBean.RELATION_PUBLICATION_ZDB_ID%>" size="30"/>
                    </td>
                    <c:if test="${formBean.relationPublicationZdbID eq null}">
                        <td class="Error">Relation publication required to save changes!</td>
                    </c:if>
                </tr>
                <tr>
                    <td>
                        <c:if test="${formBean.relationPublicationZdbID eq null}">
                            <div class="Error">Relation publication required to save changes!</div>
                        </c:if>
                        <form:errors path="<%= RunBean.RELATION_PUBLICATION_ZDB_ID%>" cssClass="error"/>
                    </td>
                </tr>
            </c:otherwise>
        </c:choose>
        <tr>
            <td><input type="submit" value="Submit publication update"></td>
        </tr>
    </form:form>
</table>

<br>
<table class="searchresults">
    <tr style="background: #ccc">
        <th>Look</th>
        <th>Lock</th>
        <th>Symbol</th>
        <th align="right">Score</th>
        <th><a href="candidate-inqueue?zdbID=${formBean.run.zdbID}&comparator=expectValue">Expect</a></th>
        <th align="right">
            <a href="candidate-inqueue?zdbID=${formBean.run.zdbID}&comparator=${formBean.comparator == "occurrenceDsc" ? "occurrenceAsc" : "occurrenceDsc"}">
                Occurrence
                <c:choose>
                    <c:when test='${formBean.comparator == "occurrenceAsc"}'>
                        <img src="/images/ARROWS/arrow.plain.up.gif" height="15" border="0" alt="ascendig">
                    </c:when>
                    <c:otherwise>
                        <c:if test='${formBean.comparator == "occurrenceDsc"}'>
                            <img src="/images/ARROWS/arrow.plain.down.gif" height="15" border="0" alt="ascending">
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </a>
        </th>
        <th>
            <a href="candidate-inqueue?zdbID=${formBean.run.zdbID}&comparator=${formBean.comparator == "lastDoneAsc" ? "lastDoneDsc" : "lastDoneAsc"}">
                Last done
                <c:choose>
                    <c:when test='${formBean.comparator == "lastDoneAsc"}'>
                        <img src="/images/ARROWS/arrow.plain.up.gif" height="15" border="0" alt="ascending">
                    </c:when>
                    <c:otherwise>
                        <c:if test='${formBean.comparator == "lastDoneDsc"}'>
                            <img src="/images/ARROWS/arrow.plain.down.gif" height="15" border="0" als="descending">
                        </c:if>
                    </c:otherwise>
                </c:choose>
            </a>
        </th>
    </tr>
    <c:forEach var="rc" items="${formBean.runCandidates}" varStatus="loop">
        <zfin:alternating-tr loopName="loop">
            <td><a href="/action/reno/candidate-view?runCandidate.zdbID=${rc.zdbID}">
                <img src=/images/glasses.jpg height=25 border=0 alt="look"></a></td>
            <td>
            <c:if test="${formBean.relationPublicationZdbID ne null || formBean.orthologyPublicationZdbID ne null}">
                <a href="/action/reno/candidate-view?runCandidate.zdbID=${rc.zdbID}&action=<%=CandidateBean.LOCK_RECORD%>">
                    <img src=/images/lock_yellow.jpg height=25 border=0 alt="lock"></a>
            </c:if>
            <td>
                <c:if test="${formBean.run.nomenclature}">
                    <zfin:link entity="${rc}"/>
                </c:if>
                <c:if test="${formBean.run.redundancy}">
                    <zfin:name entity="${rc}"/>
                </c:if>
            </td>
            <td align="right">${rc.bestHit.score}</td>
            <td align="right">${rc.bestHit.expectValue}</td>
            <td align="right">${rc.occurrenceOrder}</td>
            <td>${rc.candidate.lastFinishedDate}</td>
        </zfin:alternating-tr>
    </c:forEach>
</table>

</c:otherwise>
</c:choose>
