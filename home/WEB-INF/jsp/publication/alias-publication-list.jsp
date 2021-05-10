<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table class="data_manager">
    <tbody>
        <tr>
            <td>
                <strong>>ZFIN ID:</strong>&nbsp;${formBean.markerAlias.zdbID}
            </td>
        </tr>
    </tbody>
</table>

<zfin2:citationList
        pubListBean="${formBean}"
        url="alias-publication-list?markerAlias.zdbID=${formBean.markerAlias.zdbID}"/>
