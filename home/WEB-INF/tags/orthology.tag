<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="orthologyPresentationBean" required="true" rtexprvalue="true"
              type="org.zfin.marker.presentation.OrthologyPresentationBean" %>
<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="title" required="false" %>
<%@ attribute name="showTitle" required="false" type="java.lang.Boolean" %>
<%@ attribute name="hideCounts" required="false" type="java.lang.Boolean" %>

<c:if test="${empty title && showTitle}">
    <c:set var="title">
        ORTHOLOGY for <zfin:abbrev entity="${marker}"/> (<zfin2:displayLocation entity="${marker}" hideLink="true"/>)
    </c:set>
</c:if>

<c:set var="hideCounts" value="${empty hideCounts ? false : hideCounts}"/>

<zfin2:subsection title="${title}"
                  anchor="orthology"
                  test="${!empty orthologyPresentationBean.orthologs || !empty orthologyPresentationBean.note}"
                  showNoData="true">

    <c:if test="${!empty orthologyPresentationBean.orthologs}">
        <table class="summary rowstripes">
            <tr>
                <th width="10%">Species</th>
                <th width="10%">Symbol</th>
                <th width="20%">Chromosome</th>
                <th width="20%">Accession #</th>
                <th width="40%">Evidence</th>
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
                        <c:if test="${!hideCounts}">
                            <c:choose>
                                <c:when test="${numPubs > 1}">
                                    (<a href="/action/ortholog/${ortholog.orthoID}/citation-list?evidenceCode=${evidence.code.code}">${numPubs}</a>)
                                </c:when>
                                <c:otherwise>
                                    (<a href="/${evidence.publications.iterator().next().zdbID}">1</a>)
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                        <br>
                    </c:forEach>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </table>
    </c:if>

    <c:if test="${!empty orthologyPresentationBean.note}">
        <div class="summary">
            <b>Orthology Note</b>
            <div class="keep-breaks">${orthologyPresentationBean.note}</div>
        </div>
    </c:if>

</zfin2:subsection>

