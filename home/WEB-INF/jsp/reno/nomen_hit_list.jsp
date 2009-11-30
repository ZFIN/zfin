<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ page import="org.zfin.sequence.reno.presentation.CandidateBean" %>



<!-- called by candidate_view.jsp -->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>



<c:forEach var="query" items="${formBean.runCandidate.candidateQueryList}" varStatus="queryloop">

    <b>GENE:</b>
    <zfin:geneRelationLink accession="${query.accession}">
        <font color="red">
            No zfin gene associated with this accession ${query.accession.number}.
        </font>
    </zfin:geneRelationLink>
    <br>

    <b>Query:</b> <zfin:link entity="${query.accession}"/> <zfin:geneRelationLink accession="${query.accession}" showParenthesis="true"/><br>

    ${query.accession.defline}

    <table class="searchresults">
        <tr style="background: #ccc">
        <th></th>
        <th>Accession</th>
        <th>LG</th>
        <th>GENE</th>
        <th>Species</th>
        <th>Score</th>
        <th>Positives</th>
        <th>Length</th>
        <th>Expect</th>
        <th>RBH</th>
        <c:forEach var="hit" items="${query.blastHits}" varStatus="loop">

            <zfin:alternating-tr loopName="loop">

                <td>
                    <a href="alignment-list?runCandidate.zdbID=${formBean.runCandidate.zdbID}#hit${hit.hitNumber}-${queryloop.index}">Hit:${hit.hitNumber}</a>
                </td>

                <td>
                    <a href="<c:out value='${hit.targetAccession.referenceDatabase.foreignDB.dbUrlPrefix}'/><c:out value='${hit.targetAccession.number}'/><c:out value='${hit.targetAccession.referenceDatabase.foreignDB.dbUrlSuffix}'/>">
                        <c:out value='${hit.targetAccession.number}'/>
                    </a>
                    <c:forEach var="relatedAccession" items="${hit.targetAccession.relatedEntrezAccessions}">
                        <c:if test="${relatedAccession.entrezAccession.abbreviation ne null}">
                            <c:if test="${relatedAccession.organism eq 'Human'}">
                                , <a
                                    href="<c:out value='${formBean.humanReferenceDatabase.foreignDB.dbUrlPrefix}'/><c:out value='${relatedAccession.entrezAccession.entrezAccNum}'/><c:out value='${formBean.humanReferenceDatabase.foreignDB.dbUrlSuffix}'/>">
                                <c:out value="${relatedAccession.entrezAccession.abbreviation}"/>
                            </a>
                            </c:if>
                            <c:if test="${relatedAccession.organism eq 'Mouse'}">
                                , <a
                                    href="<c:out value='${formBean.mouseReferenceDatabase.foreignDB.dbUrlPrefix}'/><c:out value='${relatedAccession.entrezAccession.entrezAccNum}'/><c:out value='${formBean.mouseReferenceDatabase.foreignDB.dbUrlSuffix}'/>">
                                <c:out value="${relatedAccession.entrezAccession.abbreviation}"/>
                            </a>
                            </c:if>
                        </c:if>
                    </c:forEach>
                </td>
                <td>
                    <c:forEach var="linkageGroup" items="${hit.targetAccession.linkageGroups}" varStatus="lgIndex">
                        ${linkageGroup.name}
                        <c:if test="${!lgIndex.last}" >,</c:if>
                    </c:forEach>
                </td>

                <td>
                    <zfin:geneRelationLink accession="${hit.targetAccession}" />
                </td>
                <%--<td>${hit.targetAccession.referenceDatabase.organism}</td>--%>
                <td>
                        ${hit.targetAccession.organism}
                        <%--<c:forEach var="relatedAccession" items="${hit.targetAccession.relatedEntrezAccessions}">
                        <c:if test="${relatedAccession eq null}">
                            <%=CandidateBean.ZEBRAFISH%>
                        </c:if>
                       '<c:out value="${relatedAccession.organism}"/>'
                        <c:if test="${relatedAccession.organism ne null}">
                          <c:out value="${relatedAccession.organism}"/>
                        </c:if>
                        --%><%--<c:if test="${relatedAccession.organism eq null}">
                          <%=CandidateBean.ZEBRAFISH%>
                        </c:if>--%><%--
                    </c:forEach>--%>

                </td>

                <td>${hit.score}</td>

                <td><c:out value="${hit.positivesNumerator}"/> / <c:out value="${hit.positivesDenominator}"/>(<c:out value="${hit.percentAlignment}"/>%)</td>

                <td>${hit.targetAccession.length}</td>

                <td>${hit.expectValue}</td>

                <td>
<%--                typically there will only be 1 or 0 for these . . . if there are more than we can handle that--%>
                    <c:forEach var="dbLink" items="${hit.targetAccession.dbLinks}">
                        <zfin2:externalAccessionBlastDropDown dbLink="${dbLink}"/>
                    </c:forEach>
                </td>
                <c:out value="${relatedAccession.organism}"/>
            </zfin:alternating-tr>

        </c:forEach>

    </table>
</c:forEach>

