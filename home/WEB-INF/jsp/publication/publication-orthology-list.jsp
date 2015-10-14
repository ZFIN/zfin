<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Orthology established by:</span></th>
        <td><span class="name-value"><zfin:link entity="${publication}"/></span> (${orthologyBeanList.size()} genes)</td>
    </tr>
</table>



<c:forEach var="formBean" items="${orthologyBeanList}">
    <h4>Orthology for <zfin:link entity="${formBean.marker}"/></h4>
    <zfin2:orthology orthologyPresentationBean="${formBean.orthologyPresentationBean}"
                     marker="${formBean.marker}" showTitle="false"
                     webdriverPathFromRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

    <hr/>
</c:forEach>
