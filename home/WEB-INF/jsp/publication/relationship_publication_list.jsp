<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>

<jsp:useBean id="formBean" class="org.zfin.marker.presentation.MarkerRelationshipBean" scope="request"/>

<table class="data_manager">
    <tbody>
        <tr>
            <td>
                <strong>ZFIN ID:</strong>&nbsp;${formBean.markerRelationship.zdbID}
            </td>
        </tr>
    </tbody>
</table>

<zfin2:citationList
        pubListBean="${formBean}"
        url="relationship-publication-list?markerRelationship.zdbID=${formBean.markerRelationship.zdbID}"/>
