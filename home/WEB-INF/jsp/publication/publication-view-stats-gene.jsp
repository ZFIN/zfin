<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>

    <z:attributeListItem label="Totals">
        <z:dataTable collapse="true">
            <thead>
                <tr>
                    <th >Publication</th>
                    <th style="width: 17%">Gene</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><fmt:formatNumber value="${pubMarkerCount}" type="number"/></td>
                    <td><fmt:formatNumber value="${markerCount}" type="number"/></td>
                </tr>
                <c:forEach var="statEntry" items="${histogramPubMarker}">
                    <tr>
                        <td>${statEntry.key.shortAuthorList}
                            <a href="${statEntry.key.zdbID}">${statEntry.key.title}</a>
                        </td>
                        <td><fmt:formatNumber value="${statEntry.value}" type="number"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </z:dataTable>
    </z:attributeListItem>


</z:attributeList>