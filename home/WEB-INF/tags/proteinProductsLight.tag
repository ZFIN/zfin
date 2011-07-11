<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="referenceDBs" type="org.zfin.marker.presentation.SummaryDBLinkDisplay" rtexprvalue="true" required="true" %>

<div class="summary">
    <c:choose>
        <c:when test="${fn:length(referenceDBs) > 0}">
            <table class="summary">
                <caption>PROTEIN FAMILIES, DOMAINS AND SITES</caption>
                <tr>
                    <c:forEach var="category" items="${referenceDBs}">
                        <td>
                            <c:forEach var="dblink" items="${category.value}">
                                <li style="list-style-type:none;">
                                    <zfin:link entity="${dblink}"/> <zfin:attribution entity="${dblink}"/>
                                </li>
                            </c:forEach>
                        </td>
                    </c:forEach>
                </tr>
            </table>
        </c:when>
        <c:otherwise>
            <b>PROTEIN FAMILIES, DOMAINS AND SITES</b> <span class="no-data-tag">No links to external sites available</span>
        </c:otherwise>
    </c:choose>
</div>
