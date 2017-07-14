<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table class="data_manager">
    <tbody>
    <tr>
        <td>
            <strong>ZFIN ID:</strong>&nbsp;${feature.zdbID}
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
        <th class="genotype-name-label">
            <span class="name-label">Feature Name:</span>
        </th>
        <td>
            <span class="name-value" geneSymbol><zfin:link entity="${feature}"/></span>
        </td>
    </tr>
</table>

<zfin2:citationList pubListBean="${citationList}" url="/action/feature/type-citation-listt/${feature.zdbID}">
</zfin2:citationList>


