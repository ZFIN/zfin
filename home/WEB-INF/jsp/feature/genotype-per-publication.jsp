<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Genotypes:</span></th>
        <td><span class="name-value"><zfin:link entity="${publication}"/></span></td>
    </tr>
</table>

<table class="summary rowstripes">
    <tbody>
    <tr>




                    <th>Genotype ID</th>
                    <th>Genotype Name</th>

    </tr>
    <c:forEach var="genotype" items="${genotypeList}"  varStatus="loop">
        <zfin:alternating-tr loopName="loop">

            <td>${genotype.zdbID}</td>

            <td><zfin:link entity="${genotype}"/></td>



        </zfin:alternating-tr>
    </c:forEach>

    </tbody>
</table>
