<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>

    <br/>
    <table class="summary rowstripes">
        <tr>
            <th width="70%">Human Ortholog Phenotype Data</th>
            <th width="30%">OMIM Phenotype ID <a class='popup-link info-popup-link' href='/action/marker/note/omim-phenotype-id'></a></th>
        </tr>

        <c:forEach var="omimPhenotype" items="${marker.omimPhenotypes}" varStatus="loop">
            <tr class=${loop.index%2==0 ? "even" : "odd"}>
                <td>
                    ${omimPhenotype.name}
                </td>
                <td>
                    <a href="http://omim.org/entry/${omimPhenotype.omimNum}">${omimPhenotype.omimNum}</a>
                </td>
            </tr>
        </c:forEach>
    </table>



