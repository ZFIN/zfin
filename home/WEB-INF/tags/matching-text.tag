<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="matchingTextList" type="java.util.Collection" required="true" %>

<c:choose>
    <c:when test="${matchingTextList== null || fn:length(matchingTextList) == 1}">
        <c:forEach var="matchingTerm" items="${matchingTextList}" varStatus="loop">
            <c:choose>
                <c:when test="${matchingTerm.matchingQuality eq 'SUBSTRUCTURE'}">
                    Match on ${matchingTerm.matchingQuality.name}: Anatomy Term:
                    ${matchingTerm.matchedString}
                    [<zfin:highlight highlightEntity="${matchingTerm.queryString}"
                                     highlightString="${matchingTerm.queryString}"/>]
                    ${matchingTerm.appendix}
                </c:when>
                <c:otherwise>
                    ${matchingTerm.matchingQuality.name}: ${matchingTerm.descriptor}:
                    <zfin:highlight highlightEntity="${matchingTerm.matchedString}"
                                    highlightString="${matchingTerm.queryString}"/> ${matchingTerm.appendix}
                    ${matchingTerm.appendix} ${matchingTerm.relatedEntityDisplay}
                </c:otherwise>
            </c:choose>
        </c:forEach>
    </c:when>
    <c:otherwise>
        <ul>
            <c:forEach var="matchingTerm" items="${matchingTextList}"
                       varStatus="loop">
                <li style="list-style: circle outside; green;">${matchingTerm.matchingQuality.name}: ${matchingTerm.descriptor}:
                    <c:choose>
                        <c:when test="${matchingTerm.matchingQuality eq 'SUBSTRUCTURE'}">
                            ${matchingTerm.matchedString} ${matchingTerm.appendix}
                            ${matchingTerm.matchingQuality.name} of
                            <zfin:highlight highlightEntity="${matchingTerm.queryString}"
                                            highlightString="${matchingTerm.queryString}"/>
                            ${matchingTerm.appendix}
                        </c:when>
                        <c:otherwise>
                            <zfin:highlight highlightEntity="${matchingTerm.matchedString}"
                                            highlightString="${matchingTerm.queryString}"/>
                            ${matchingTerm.appendix} ${matchingTerm.relatedEntityDisplay}
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>
<authz:authorize ifAnyGranted="root">
    <c:if test="${matchingTextList == null || fn:length(matchingTextList) == 0}">
        No Match found.
    </c:if>
</authz:authorize>
