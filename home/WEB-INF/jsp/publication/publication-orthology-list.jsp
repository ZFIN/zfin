<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Orthology established by:</span></th>
        <td><span class="name-value"><zfin:link entity="${publication}"/></span> (${orthologyBeanList.size()} genes)</td>
    </tr>
</table>



<c:forEach var="formBean" items="${orthologyBeanList}">
    <h4>Orthology for <zfin:link entity="${formBean.marker}"/> (<zfin2:displayLocation entity="${formBean.marker}" hideLink="true"/>)</h4>
    <zfin2:orthology orthologyPresentationBean="${formBean.orthologyPresentationBean}"
                     marker="${formBean.marker}"
                     showTitle="false"
                     hideCounts="true"
                     hideDownloadLink="true"/>

    <hr/>
</c:forEach>
