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
                                    ${accession}
                            </li>
                        </c:forEach>
                    </td>
                    <td>
                    <c:forEach var="evidence" items="${ortholog.evidence}">
                        ${evidence.code.name} (${fn:length(evidence.publications)})<br>
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

