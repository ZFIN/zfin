<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<%@ attribute name="gene" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>

<%-- optionally allow the title to be set --%>
<%@ attribute name="title" type="java.lang.String" required="false"%>

<%-- set the tag to a default value if nothing is passed in --%>
<c:if test="${empty title}">
    <c:set var="title" value="DISEASE ASSOCIATED WITH ${gene.abbreviation} HUMAN ORTHOLOG"/>
</c:if>

<zfin2:subsection title="${title}" test="${!empty gene.diseaseDisplays}" showNoData="true">
    <table class="summary rowstripes">
        <tr>
            <th width="35%">Disease Ontology Term</th>
            <th width="35%">OMIM Term</th>
            <th width="30%" style="text-align: center">OMIM Phenotype ID</th>
        </tr>


        <c:set var="groupIndex" value="0"/>
        <c:forEach var="disease" items="${gene.diseaseDisplays}" varStatus="loop">
            <c:set var="lastDOterm" value=""/>
            <c:forEach var="omim" items="${disease.omimPhenotypes}" varStatus="innerloop">
                <tr class=${loop.index%2==0 ? "even" : "odd"}>
                    <td>
                        <c:if test="${!empty disease.diseaseTerm.termName && lastDOterm != disease.diseaseTerm.termName}"><zfin:link entity="${disease.diseaseTerm}" longVersion="true"/></c:if>
                        <c:set var="lastDOterm" value="${disease.diseaseTerm.termName}"/>
                    </td>
                    <td>
                            ${omim.name}
                    </td>
                    <td style="text-align: center">
                        <c:if test="${!empty omim.omimNum}"><a href="http://omim.org/entry/${omim.omimNum}">${omim.omimNum}</a></c:if>
                    </td>
                </tr>
            </c:forEach>

        </c:forEach>
    </table>
</zfin2:subsection>

