<!-- called by candidate_view.jsp -->
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>


<c:forEach var="query" items="${formBean.runCandidate.candidateQueryList}" varStatus="queryloop">
    <b>GENE:</b>
    <zfin:name entity="${query.runCandidate}"/>
    <br>
    <b>Sequence:</b>
    <c:forEach var="clone" items="${formBean.smallSegments}" varStatus="loop">
        <zfin:link entity="${clone}"/>
        <c:if test="${!loop.last}">, </c:if>
    </c:forEach>
    <br>
    <b>Query:</b> <zfin:link entity="${query.accession}"/>
    <geneRelationLink accession="${query.accession}" showParent="true"/>
    <br>
    ${query.accession.defline}
    <c:choose>
        <c:when test="${query.validBlastHit}">
            [<b>No hits recovered from blast</b>]
        </c:when>
        <c:otherwise>
            <table class="searchresults">
                <tr style="background: #ccc">
                    <th></th>
                    <th>Accession</th>
                    <th>LG</th>
                    <th>Length</th>
                    <th>Score</th>
                    <th>Positives</th>
                    <th>Expect</th>
                    <th>ZFIN</th>
                </tr>
                <c:forEach var="hit" items="${query.blastHits}" varStatus="loop">
                    <zfin:alternating-tr loopName="loop">
                        <td>
                            <a href="alignment-list?runCandidate.zdbID=${formBean.runCandidate.zdbID}#hit${hit.hitNumber}-${queryloop.index}">Hit:${hit.hitNumber}</a>
                        </td>

                        <td>
                            <zfin:link entity="${hit.targetAccession}"/>
                                <%--<zfin:accessionLink beanName="hit" propertyName="targetAccession"/>--%>
                        </td>

                        <td>
                            <c:forEach var="linkageGroup" items="${hit.targetAccession.linkageGroups}"
                                       varStatus="lgIndex">
                                ${linkageGroup.name}
                                <c:if test="${!lgIndex.last}">,</c:if>
                            </c:forEach>
                        </td>

                        <td>${hit.targetAccession.length}</td>

                        <td>${hit.score}</td>

                        <td><c:out value="${hit.positivesNumerator}"/> / <c:out value="${hit.positivesDenominator}"/>
                            (<c:out value="${hit.percentAlignment}"/>%)
                        </td>

                        <td>${hit.expectValue}</td>
                        <td>
                                <%--<zfin:geneRelationLink accession="${hit.targetAccession}"/>--%>
                            <zfin:allMarkerRelationLink accession="${hit.targetAccession}" doAbbrev="true"/>
                        </td>
                    </zfin:alternating-tr>
                </c:forEach>
            </table>
        </c:otherwise>
    </c:choose>
    <br/>
</c:forEach>


