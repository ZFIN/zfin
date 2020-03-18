<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.SequenceTargetingReagentBean" scope="request"/>

<z:dataTable collapse="true" hasData="${formBean.expressionDisplays != null && fn:length(formBean.expressionDisplays) > 0 }">
    <thead>
    <tr>
                <th>
                    Expressed Gene
                </th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="xp" items="${formBean.expressionDisplays}" varStatus="loop">
            <td>
                ${xp.expressedGene}
            </td>
    </c:forEach>
    </tbody>
</z:dataTable>



