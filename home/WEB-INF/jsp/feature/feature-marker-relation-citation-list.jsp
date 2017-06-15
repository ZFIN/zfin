<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="data_manager">
    <tbody>
    <tr>
        <td>
            <strong>ZFIN ID:</strong>&nbsp;${featureMarkerRelationshipZdbID}
        </td>
    </tr>
    </tbody>
</table>

<zfin2:citationList pubListBean="${citationList}" url="/action/feature/feature-marker-relation-citation-list/${mRel.zdbID}">
</zfin2:citationList>
