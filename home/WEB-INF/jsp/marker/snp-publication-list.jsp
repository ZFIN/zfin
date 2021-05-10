<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<z:page>
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

        <table class="primary-entity-attributes">
            <tr>
                <th>Clone name</th>
                <td><zfin:link entity="${formBean.marker}"/></td>
            </tr>
        </table>

    </zfin2:citationList>
</z:page>