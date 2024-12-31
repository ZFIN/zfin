<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <table class="data_manager">
        <tbody>
        <tr>
            <td>
                <strong>UniProt ID:</strong>&nbsp;${uniprotID}
            </td>
        </tr>
        </tbody>
    </table>

    <p>The following PDB IDs are associated with ${uniprotID}:</p>
    <table class="summary rowstripes sortable" id="pubsByDate">
        <thead>
        <tr>
            <div>
                <th>PDB ID</th>
            </div>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="pdbLink" items="${pdbList}">
            <tr class="newgroup">
                <td>
                    <div>
                        <a href="${pdbLink.value}">${pdbLink.key}</a>
                    </div>
                </td>
            </tr>
        </c:forEach>

        </tbody>
        <tfoot></tfoot>
    </table>

</z:page>
