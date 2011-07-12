<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships in a table --%>

<%@ attribute name="geneOntologyOnMarker" required="true" rtexprvalue="true" type="org.zfin.marker.presentation.GeneOntologyOnMarkerBean" %>

<%@ attribute name="marker" required="true" rtexprvalue="true" type="org.zfin.marker.Marker" %>
<%@ attribute name="webdriverRoot" required="true" rtexprvalue="true" type="java.lang.String" %>

<%@ attribute name="title" required="false"%>

<c:if test="${empty title}">
    <c:set var="title" value="GENE ONTOLOGY"/>
</c:if>

<zfin2:subsection title="${title}"
                        test="${geneOntologyOnMarker.goTermCount>0}" showNoData="true">

    <table class="summary">
        <tr>
            <th width = "25%">Ontology<A class="popup-link info-popup-link" HREF="/zf_info/GO.html"></A></th>
                <%--<th>Ontology</th>--%>
            <th>GO Term</th>
        </tr>

        <c:if test="${!empty geneOntologyOnMarker.biologicalProcessEvidence}">
            <tr>
                <td width="25%" nowrap="true">
                        ${fn:split(geneOntologyOnMarker.biologicalProcessEvidence.goTerm.ontology.commonName, ':')[1]}
                </td>
                <td>
                        ${geneOntologyOnMarker.biologicalProcessEvidence.flag}
                    <zfin:link entity="${geneOntologyOnMarker.biologicalProcessEvidence.goTerm}"/>

                    (<a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}"
                            >more</a>)
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty geneOntologyOnMarker.cellularComponentEvidence}">
            <tr>
                <td nowrap="true">
                        ${fn:split(geneOntologyOnMarker.cellularComponentEvidence.goTerm.ontology.commonName, ':')[1]}
                </td>
                <td>
                        ${geneOntologyOnMarker.cellularComponentEvidence.flag}
                    <zfin:link entity="${geneOntologyOnMarker.cellularComponentEvidence.goTerm}"/>

                    (<a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}"
                            >more</a>)
                </td>
            </tr>
        </c:if>
        <c:if test="${!empty geneOntologyOnMarker.molecularFunctionEvidence}">
            <tr>
                <td nowrap="true">
                        ${fn:split(geneOntologyOnMarker.molecularFunctionEvidence.goTerm.ontology.commonName, ':')[1]}
                </td>
                <td>
                        ${geneOntologyOnMarker.molecularFunctionEvidence.flag}
                    <zfin:link entity="${geneOntologyOnMarker.molecularFunctionEvidence.goTerm}"/>
                    (<a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}"
                            >more</a>)
                </td>
            </tr>
        </c:if>

    </table>

    <c:if test="${geneOntologyOnMarker.goTermCount>0}">
        <a href="/${webdriverRoot}?MIval=aa-markergoview.apg&OID=${marker.zdbID}
    ">GO Terms (all ${geneOntologyOnMarker.goTermCount})</a>
    </c:if>

</zfin2:subsection>

