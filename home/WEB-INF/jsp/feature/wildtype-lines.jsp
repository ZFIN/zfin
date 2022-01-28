<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<c:set var="SECTION" value=""/>
<c:set var="TITLE" value="ZFIN: Wild-Type Lines: Summary Listing"/>

<z:dataPage sections="${[SECTION]}" title="${TITLE}">

    <jsp:body>
        <z:section title=" Wild-Type Lines">
            <z:dataTable>
                <thead>
                    <tr>
                        <th>Strain</th>
                        <th>Current Source</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="genotype" items="${wildtypes}" varStatus="loop">
                        <tr>
                            <td><zfin:link entity="${genotype}"/></td>
                            <td>
                                <c:forEach var="supplier" items="${genotype.suppliers}">
                                    <zfin:link entity="${supplier.organization}"/>
                                    <zfin2:orderThis organization="${supplier.organization}"
                                                     accessionNumber="${genotype.zdbID}"/>
                                    <br/>
                                </c:forEach>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </z:dataTable>
        </z:section>
    </jsp:body>
</z:dataPage>