<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="suppliers" type="java.util.List"  required="true" %>
<%@ attribute name="webdriverPath" type="java.lang.String" required="true" %>


<zfin2:subsection noDataText="None Submitted" title="SOURCE"
                        test="${!empty suppliers}">
    <table width=100% border=0 cellspacing=0>
        <c:forEach var="supplier" items="${suppliers}" varStatus="status">
            <zfin:alternating-tr loopName="status">
                <td>
                    <c:choose>
                        <c:when test="${supplier.organization.url == null}">
                            <a href="/${webdriverPath}?MIval=aa-sourceview.apg&OID=${supplier.organization.zdbID}"
                               id="${supplier.organization.zdbID}">
                                    ${supplier.organization.name}
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${supplier.organization.url}" id="${supplier.organization.zdbID}">${supplier.organization.name}</a>
                        </c:otherwise>
                    </c:choose>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</zfin2:subsection>

