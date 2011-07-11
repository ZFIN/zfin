<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="data" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.GeneOntologyOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="webdriverRoot" required="true" rtexprvalue="true" type="java.lang.String" %>

<%@ attribute name="title" required="false"%>

<c:if test="${empty title}">
    <c:set var="title" value="GENE ONTOLOGY"/>
</c:if>

<zfin2:subsectionMarker title="${title}"
                        test="${data.goTermCount>0}" showNoData="true">

    <table class="summary">
        <tr>
            <th width = "25%">Ontology<A class="popup-link info-popup-link" HREF="/zf_info/GO.html"></A></th>
                <%--<th>Ontology</th>--%>
            <th>GO Term</th>
        </tr>

        <c:if test="${!empty data.biologicalProcessEvidence}">
            <tr>
                <td width="25%" nowrap="true">
                        ${fn:split(data.biologicalProcessEvidence.goTerm.ontology.commonName, ':')[1]}
                </td>
                <td>
                        ${data.biologicalProcessEvidence.flag}
                    <zfin:link entity="${data.biologicalProcessEvidence.goTerm}"/>

                    (<a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}"
                            >more</a>)
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty data.cellularComponentEvidence}">
            <tr>
                <td nowrap="true">
                        ${fn:split(data.cellularComponentEvidence.goTerm.ontology.commonName, ':')[1]}
                </td>
                <td>
                        ${data.cellularComponentEvidence.flag}
                    <zfin:link entity="${data.cellularComponentEvidence.goTerm}"/>

                    (<a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}"
                            >more</a>)
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty data.molecularFunctionEvidence}">
            <tr>
                <td nowrap="true">
                        ${fn:split(data.molecularFunctionEvidence.goTerm.ontology.commonName, ':')[1]}
                </td>
                <td>
                        ${data.molecularFunctionEvidence.flag}
                    <zfin:link entity="${data.molecularFunctionEvidence.goTerm}"/>
                    (<a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}"
                            >more</a>)
                </td>
            </tr>
        </c:if>

    </table>

    <c:if test="${data.goTermCount>0}">
        <a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}
    ">GO Terms (all ${data.goTermCount})</a>
    </c:if>

</zfin2:subsectionMarker>

