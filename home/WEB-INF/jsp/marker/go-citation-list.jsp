<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="data_manager">
    <tbody>
    <tr>
        <td>
            <strong>ZFIN ID:</strong>&nbsp;${marker.zdbID}
        </td>
    </tr>
    </tbody>
</table>

<table width=100%>
    <tr>
        <td bgcolor=#cccccc>
            <span style="font-size: 120%;"><b>CITATIONS</b></span>
            (${pubCount} total)
        </td>
    </tr>
</table>

<table class="primary-entity-attributes">
    <tr>
        <th>
            <span class="name-label">Gene Ontology Pubs:</span>
        </th>
    </tr>
</table>

<zfin2:citationList pubListBean="${citationList}" url="/action/marker//markerID/${marker.zdbID}/go-citation-list/">
</zfin2:citationList>


