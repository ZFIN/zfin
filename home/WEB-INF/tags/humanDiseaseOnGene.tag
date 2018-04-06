<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="diseases" type="java.util.List" rtexprvalue="true" required="true" %>

<style>
    .marker-go-table td, .marker-go-table th { line-height: 1.5em; }
</style>

<zfin2:subsection title="DISEASE ASSOCIATED WITH <i>${gene.abbreviation}</i> HUMAN ORTHOLOG <a class='popup-link info-popup-link' href='/action/marker/note/omim-phenotype'></a>"
                  test="${!empty diseases}" showNoData="true">

    <table class="summary rowstripes marker-go-table">
        <tr>
            <th width="35%">Disease Ontology Term</th>
            <th width="35%">OMIM Term</th>
            <th width="30%" style="text-align: center">OMIM Phenotype ID</th>
        </tr>
        <c:forEach var="row" items="${diseases}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${diseases}" groupByBean="diseaseTerm" newGroup="true">
                <td>
                    <zfin:groupByDisplay loopName="loop" groupBeanCollection="${diseases}" groupByBean="diseaseTerm">
                        <c:if test="${!empty row.diseaseTerm}"><zfin:link entity="${row.diseaseTerm}" longVersion="true"/></c:if>
                    </zfin:groupByDisplay>
                </td>
                <td>${row.omimTerm}</td>
                <td style="text-align: center"><c:if test="${!empty row.omimNumber}"><a href="http://omim.org/entry/${row.omimNumber}">${row.omimNumber}</a></c:if></td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>

</zfin2:subsection>


