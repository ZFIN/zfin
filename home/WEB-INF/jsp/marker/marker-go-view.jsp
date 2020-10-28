<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <meta name="marker-go-view-page"/> <%-- this is used by the web testing framework to know which page this is--%>

    <zfin2:dataManager zdbID="${marker.zdbID}"
                       editURL="/action/marker/marker-go-edit/${marker.zdbID}"
                       editLinkText="Add/Update GO Annotations"/>

    <style>
        .marker-go-table td, .marker-go-table th { line-height: 1.5em; }
    </style>

    <table class="primary-entity-attributes">
        <tr>
            <th><span class="name-label">${marker.markerType.displayName} Name:</span></th>
            <td><span class="name-value"><zfin:name entity="${marker}"/></span></td>
        </tr>
        <tr>
            <th><span class="name-label">${marker.markerType.displayName} Symbol:</span></th>
            <td><span class="name-value"><zfin:link entity="${marker}"/></span></td>
        </tr>
    </table>

    <zfin2:subsection title="GO Details"
                      test="${!empty markerGoViewTableRows}" showNoData="true">

        <table class="summary rowstripes marker-go-table">
            <tr>
                <th>Ontology</th>
                <th>Qualifier</th>
                <th>Term</th>
                <th>Evidence</th>
                <th>Inferred From</th>
                <th>Annotation Extension</th>
                <th>Reference(s)</th>
            </tr>
            <c:forEach var="row" items="${markerGoViewTableRows}" varStatus="loop">
                <zfin:alternating-tr loopName="loop" groupBeanCollection="${markerGoViewTableRows}" groupByBean="ontology" newGroup="true">
                    <td>
                        <zfin:groupByDisplay loopName="loop" groupBeanCollection="${markerGoViewTableRows}" groupByBean="ontology">
                            ${row.ontology}
                        </zfin:groupByDisplay>
                    </td>
                    <td>${row.qualifier}</td>
                    <td><zfin:link entity="${row.term}"/></td>
                    <td>
                        <a href="http://www.geneontology.org/GO.evidence.shtml#${fn:toLowerCase(row.evidenceCode.code)}">
                            ${row.evidenceCode.code}
                        </a>
                    </td>
                    <td>${row.inferredFrom}</td>
                    <td>${row.annotExtns}</td>
                    <td>${row.referencesLink}</td>
                </zfin:alternating-tr>
            </c:forEach>

        </table>

    </zfin2:subsection>
</z:page>