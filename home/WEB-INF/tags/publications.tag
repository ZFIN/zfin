<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="publications" type="java.util.Collection" required="true" %>

        <c:forEach var="pub" items="${publications}" varStatus="loop">
            <zfin:alternating-tr loopName="loop">
                <td>
                    <div class="show_pubs">
                        <a href="/${pub.zdbID}" id="${pub.zdbID}">${pub.citation}</a>
                        <authz:authorize access="hasRole('root')"><c:if
                                test="${pub.open}">OPEN</c:if><c:if
                                test="${!pub.open}">CLOSED</c:if><c:if
                                test="${pub.indexed}">, INDEXED</c:if>
                        </authz:authorize>
                    </div>
                </td>
            </zfin:alternating-tr>
        </c:forEach>
