<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<table class="data_manager">
    <tbody>
        <tr>
            <td>
                <strong>ZFIN ID:</strong>&nbsp;${formBean.marker.zdbID}
            </td>
        </tr>
    </tbody>
</table>

<zfin2:citationList pubListBean="${formBean}" url="snp-publication-list?markerID=${formBean.marker.zdbID}">

    <div class="name-label">
        Clone name: <zfin:link entity="${formBean.marker}"/>
    </div>

</zfin2:citationList>
