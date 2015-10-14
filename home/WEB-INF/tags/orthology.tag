<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="orthologyPresentationBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.OrthologyPresentationBean" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="webdriverPathFromRoot" required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="showTitle" required="false" type="java.lang.Boolean" %>
<%@ attribute name="hideEvidence" required="false" type="java.lang.Boolean" %>

<c:if test="${empty title && showTitle}">
    <c:set var="title" value="ORTHOLOGY"/>
</c:if>

<zfin2:subsection title="${title}"
                  anchor="orthology"
                  test="${!empty orthologyPresentationBean.orthologs || !empty orthologyPresentationBean.note}"
                  showNoData="true">

    <c:if test="${!empty orthologyPresentationBean.orthologs}">
        <table class="summary rowstripes">
            <tr>
                <th>Species</th>
                <th>Symbol</th>
                <th>Chr (Position)</th>
                <th>Accession #</th>
                <th>Evidence</th>
            </tr>

            <c:forEach var="ortholog" items="${orthologyPresentationBean.orthologs}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        <b>${ortholog.species}</b>
                    </td>
                    <td>
                        <i>${ortholog.abbreviation}</i>
                    </td>
                    <td>
                            ${ortholog.chromosome}

                        <c:if test="${ortholog.positionValid}">
                            (${ortholog.position})
                        </c:if>
                    </td>
                    <td>
                        <c:forEach var="accession" items="${ortholog.accessions}">
                            <li style="list-style-type: none;">
                                <zfin:link entity="${accession}"/>
                            </li>
                        </c:forEach>
                    </td>
                    <td>
                    <c:forEach var="evidence" items="${ortholog.evidence}">
                        <c:set var="numPubs" value="${fn:length(evidence.publications)}"/>
                        ${evidence.code.name}
                        <c:choose>
                            <c:when test="${numPubs > 1}">
                                (<a href="/action/ortholog/${ortholog.orthoID}/citation-list?evidenceCode=${evidence.code.code}">${numPubs}</a>)
                            </c:when>
                            <c:otherwise>
                                (<a href="/${evidence.publications.iterator().next().zdbID}">1</a>)
                            </c:otherwise>
                        </c:choose>
                        <br>
                    </c:forEach>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </c:if>

    <c:if test="${!empty orthologyPresentationBean.note}">
        <div class="summary">
            <b>Orthology Note</b><br>
            ${orthologyPresentationBean.note}
        </div>
    </c:if>

</zfin2:subsection>

