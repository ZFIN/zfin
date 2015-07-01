<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
    Fish Name: ${fish.name}
</div>
<div class="popup-body">
    <table class="primary-entity-attributes">
        <tr>
            <th>Genotype:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty fish.genotype}">
                        <zfin:link entity="${fish.genotype}"/>
                    </c:when>
                    <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
                </c:choose>

            </td>
        </tr>
        <tr>
            <th>Targeting Reagent:</th>
            <td>
                <c:choose>
                    <c:when test="${!empty fish.strList}">
                        <zfin:link entity="${fish.strList}"/>
                    </c:when>
                    <c:otherwise><span class="no-data-tag">none</span></c:otherwise>
                </c:choose>

            </td>
        </tr>
        </table>
    
</div>