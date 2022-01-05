<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>

    <z:attributeListItem label="Totals">
        <z:dataTable>
            <thead>
                <tr>
                    <th>No of distinct Publications</th>
                    <th>No of distinct Genes</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td>${pubMarkerCount}</td>
                    <td>${markerCount}</td>
                </tr>
            </tbody>
        </z:dataTable>
    </z:attributeListItem>


</z:attributeList>