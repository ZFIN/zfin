<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<h3>Line Information</h3>

<p>
    Please provide names for each allele and/or transgenic insertion carried by your line. In addition to the genetic
    locus or construct name, a unique lab allele/line designation must be provided for each. Please also provide a
    description of the protocol used to generate each mutation or transgenic insertion, as well as the mutation type if
    known. For example use "ENU treated adult males" and "point", or "embryos treated with DNA" and "transgenic
    insertion". For help with naming conventions, contact the
    <a href="mailto:<%= ZfinPropertiesEnum.NOMEN_COORDINATOR.value()%>">ZFIN Nomenclature Coordinator</a>. If you need
    a laboratory prefix set up for you, please contact us at <a href="mailto:zfinadmn@zfin.org">zfinadmn@zfin.org</a>.
</p>

<div>
    <form:label path="geneticBackground">Genetic Background</form:label>
    <form:input path="geneticBackground"/>
</div>

<table id="line-details">
    <tr>
        <th>Gene or Construct Name</th>
        <th>Gene Symbol</th>
        <th>Allele/Line Designation</th>
        <th>Protocol</th>
        <th>Mutation Type</th>
    </tr>
    <c:forEach var="details" items="${submission.lineDetails}" varStatus="status">
        <tr>
            <td><form:input path="lineDetails[${status.index}].geneName"/></td>
            <td><form:input path="lineDetails[${status.index}].geneSymbol"/></td>
            <td><form:input path="lineDetails[${status.index}].designation"/></td>
            <td><form:input path="lineDetails[${status.index}].protocol"/></td>
            <td><form:input path="lineDetails[${status.index}].mutationType"/></td>
        </tr>
    </c:forEach>
</table>

<script>
    jQuery(function () {
        makeDynamicTable("#line-details", ${fn:length(submission.lineDetails)}, "lineDetails",
                ["geneName", "geneSymbol", "designation", "protocol", "mutationType"]);
    });
</script>
