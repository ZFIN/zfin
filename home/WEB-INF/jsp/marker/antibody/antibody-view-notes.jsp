<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:dataTable collapse="false" hasData="${!empty formBean.externalNotes and fn:length(formBean.externalNotes) > 0}">
    <thead>
        <tr>
            <th>Comment</th>
            <th width="30%">Citation</th>
        </tr>
    </thead>
    <tbody>
        <c:forEach var="note" items="${formBean.externalNotes}" varStatus="loop">
            <tr>
                <td>
                    <zfin2:toggleTextLength text="${note.note}" idName="${zfn:generateRandomDomID()}"
                                            shortLength="80"/>
                </td>
                <td>
                    <zfin:link entity="${note.publication}"/>
                </td>
            </tr>
        </c:forEach>
    </tbody>
</z:dataTable>