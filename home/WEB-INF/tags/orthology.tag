<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="webdriverPathFromRoot" required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="data" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.OrthologyPresentationBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>

<%@ attribute name="title" required="false"%>



<c:if test="${empty title}">
    <c:set var="title" value="ORTHOLOGY"/>
</c:if>

<zfin2:subsection title="${title}"
                        test="${!empty data.evidenceCodes}" showNoData="true">

    <%--<table class="summary horizontal-solidblock" >--%>
    <table class="summary rowstripes" >
        <tr >
            <th style="background: #cccccc" colspan="4">&nbsp;</th>
            <th bgcolor="#cccccc" align="left" colspan="3"><b>Evidence</b> <a class="info-popup-link popup-link" href="/zf_info/oev.html"></a></th>
        </tr>
        <tr bgcolor="#cccccc">
            <th>Species</th>
            <th>Symbol</th>
            <th>Chromosome (Position)</th>
            <th>Accession #</th>
            <c:forEach var="evidenceCode" items="${data.evidenceCodes}">
                <th>
                        ${evidenceCode}
                </th>
            </c:forEach>
        </tr>

            <%--Zebrafish Data--%>
        <tr class="odd">
            <td>
                <b>Zebrafish</b>
            </td>
            <td><i>${marker.abbreviation}</i></td>
            <td>
                <c:forEach var="lg" items="${marker.LG}" varStatus="loop">
                    ${lg}${(!loop.last ? ", " : "")}
                </c:forEach>
            </td>
            <td>
                    <%--no accession--%>
                &nbsp;
            </td>
            <c:forEach var="evidence" items="${data.evidenceCodes}">
                <td style="vertical-align: middle;">
                    <img valign='center' src='/images/fill_green_ball.gif' border=0 height=10>
                </td>
            </c:forEach>
        </tr>

            <%--Human / Mouse / Fly Data--%>
        <c:forEach var="orthologue" items="${data.orthologues}" varStatus="loop">
            <tr class=${loop.index%2==0 ? "even": "odd"}>
                <td>
                    <b>${orthologue.species}</b>
                </td>
                <td>
                    <i>${orthologue.abbreviation}</i>
                </td>
                <td>
                        ${orthologue.chromosome}

                    <c:if test="${orthologue.position !=null}">
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
                <c:forEach var="evidenceCode" items="${data.evidenceCodes}">
                    <td style="vertical-align: middle;">
                        <c:forEach var="evidence" items="${orthologue.evidenceCodes}">
                            ${ (evidence eq evidenceCode ?
                            "<img valign='center' src='/images/fill_green_ball.gif' border=0 height=10>"
                            : "" )}
                        </c:forEach>
                    </td>
                </c:forEach>
            </tr>
        </c:forEach>


        <tr>
            <td colspan="7">
                <a href="/${webdriverPathFromRoot}?MIval=aa-orthoviewdetailed.apg&OID=${marker.zdbID}&abbrev=${marker.abbreviation}">
                    <b>Orthology Details</b>
                </a>
                <c:if test="${!empty data.notes and !empty data.notes[0]}">
                <c:forEach var="note" items="${data.notes}">
                    <a class="popup-link data-popup-link"
                       href="/action/marker/note/external/${marker.zdbID}"></a>
                    <%--${note}--%>
                </c:forEach>
            </td>
        </tr>
        </c:if>
    </table>

</zfin2:subsection>

