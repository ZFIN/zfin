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
                  test="${!empty orthologyPresentationBean.evidenceCodes || !empty orthologyPresentationBean.notes}"
                  showNoData="true">

    <%--<table class="summary horizontal-solidblock" >--%>
    <c:if test="${!empty orthologyPresentationBean.evidenceCodes}">
        <table class="summary rowstripes">
            <c:if test="${not hideEvidence}">
                <tr>
                    <th style="background: #cccccc" colspan="4">&nbsp;</th>
                    <th bgcolor="#cccccc" align="left" colspan="3"><b>Evidence</b> <a class="info-popup-link popup-link"
                                                                                      href="/zf_info/oev.html"></a></th>
                </tr>
            </c:if>
            <tr bgcolor="#cccccc">
                <th>Species</th>
                <th>Symbol</th>
                <th>Chr (Position)</th>
                <th>Accession #</th>
                <c:if test="${not hideEvidence}">
                    <c:forEach var="evidenceCode" items="${orthologyPresentationBean.evidenceCodes}">
                        <th>
                                ${evidenceCode}
                        </th>
                    </c:forEach>
                </c:if>
            </tr>

                <%--Zebrafish Data--%>
            <tr class="odd">
                <td>
                    <b>Zebrafish</b>
                </td>
                <td><i>${marker.abbreviation}</i></td>
                <td>
                    <zfin2:displayLocation entity="${marker}" hideTitles="true"/>
                </td>
                <td>
                        <%--no accession--%>
                    &nbsp;
                </td>
                <c:if test="${not hideEvidence}">
                    <c:forEach var="evidence" items="${orthologyPresentationBean.evidenceCodes}">
                        <td style="vertical-align: middle;">
                            <img valign='center' src='/images/fill_green_ball.gif' border=0 height=10>
                        </td>
                    </c:forEach>
                </c:if>
            </tr>

                <%--Human / Mouse / Fly Data--%>
            <c:forEach var="orthologue" items="${orthologyPresentationBean.orthologues}" varStatus="loop">
                <tr class=${loop.index%2==0 ? "even": "odd"}>
                    <td>
                        <b>${orthologue.species}</b>
                    </td>
                    <td>
                        <i>${orthologue.abbreviation}</i>
                    </td>
                    <td>
                            ${orthologue.chromosome}

                        <c:if test="${orthologue.positionValid}">
                            (${orthologue.position})
                        </c:if>
                    </td>
                    <td>
                        <c:forEach var="accession" items="${orthologue.accessions}">
                            <li style="list-style-type: none;">
                                    ${accession}
                            </li>
                        </c:forEach>
                    </td>
                    <c:if test="${not hideEvidence}">
                        <c:forEach var="evidenceCode" items="${orthologyPresentationBean.evidenceCodes}">
                            <td style="vertical-align: middle;">
                                <c:forEach var="evidence" items="${orthologue.evidenceCodes}">
                                    ${ (evidence eq evidenceCode ?
                            "<img valign='center' src='/images/fill_green_ball.gif' border=0 height=10>"
                            : "" )}
                                </c:forEach>
                            </td>
                        </c:forEach>
                    </c:if>
                </tr>
            </c:forEach>
        </table>
    </c:if>

    <c:if test="${!empty orthologyPresentationBean.notes and !empty orthologyPresentationBean.notes[0]}">
        <div class="summary">
            <b>Orthology Note</b><a class="popup-link data-popup-link"
                                    href="/action/marker/note/external/${marker.zdbID}"></a>
        </div>
    </c:if>

    <c:if test="${not hideEvidence}">
        <c:if test="${!empty orthologyPresentationBean.evidenceCodes}">
            <div class="summary">
                <a href="/action/marker/${marker.zdbID}/orthology-detail">
                    <b>Orthology Details</b>
                </a>
            </div>
        </c:if>
    </c:if>

</zfin2:subsection>

