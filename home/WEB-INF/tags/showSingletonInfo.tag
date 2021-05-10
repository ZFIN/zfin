<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="singleLinkageList" required="true" type="java.util.Collection" %>

<c:if test="${not empty singleLinkageList }">
    <table class="summary rowstripes">
        <tr>
            <td>
                <table>
                    <c:forEach var="singleton" items="${singleLinkageList}">
                        <tr>
                            <td> Chr ${singleton.linkage.chromosome} </td>
                            <td><zfin:link entity="${singleton.linkage.publication}"/></td>
                            <td>
                                <zfin2:toggleTextLength text="${singleton.linkage.comments}" idName="${zfn:generateRandomDomID()}" shortLength="100"/>
                                </td>
                        </tr>
                    </c:forEach>
                </table>
            </td>
        </tr>
    </table>
</c:if>
