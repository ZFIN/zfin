<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="data_manager">
    <tbody>
    <tr>
        <td>
            <strong>ZFIN ID:</strong>&nbsp;${dataZdbID}
        </td>
    </tr>
    </tbody>
</table>

<zfin2:citationList pubListBean="${citationList}" url="/action/infrastructure/data-citation-list/${dataZdbID}">
</zfin2:citationList>
