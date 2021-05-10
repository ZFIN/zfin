<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="singleLinkageMap" required="true" type="java.util.Map" %>

<c:if test="${not empty singleLinkageMap }">
    <c:forEach var="entry" items="${singleLinkageMap}">
        <table class="summary rowstripes">
            <tr>
                <th>
                    Genomic Feature
                    <zfin:link entity="${entry.key}"/> is an allele of
                    <zfin:link entity="${marker}"/>
                </th>
            </tr>
            <tr>
                <td>
                    <table>
                        <c:forEach var="singleton" items="${entry.value}">
                            <tr>
                                <td> Chr ${singleton.linkage.chromosome} </td>
                                <td><zfin:link entity="${singleton.linkage.publication}"/></td>
                                <td>
                                    <zfin2:toggleTextLength text="${singleton.linkage.comments}"
                                                            idName="${zfn:generateRandomDomID()}" shortLength="100"/>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </td>
            </tr>
        </table>
    </c:forEach>
</c:if>
