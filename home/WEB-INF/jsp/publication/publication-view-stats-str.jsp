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
                    <td><fmt:formatNumber value="${pubStrCount}" type="number"/></td>
                    <td><fmt:formatNumber value="${strCount}" type="number"/></td>
                </tr>
            </tbody>
        </z:dataTable>
    </z:attributeListItem>


</z:attributeList>