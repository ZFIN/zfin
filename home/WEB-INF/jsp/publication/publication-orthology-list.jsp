<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<script src="/javascript/angular/angular.min.js"></script>
<script src="/javascript/angular/paging.min.js"></script>
<script src="/javascript/zfin-app.module.js"></script>
<script src="/javascript/ortho-edit.js"></script>

<table class="primary-entity-attributes">
    <tr>
        <th><span class="name-label">Orthology established by:</span></th>
        <td><span class="name-value"><zfin:link entity="${publication}"/></span> (${formBean.totalRecords} genes)</td>
    </tr>
</table>

<div ng-app="app">
    <c:forEach var="formBean" items="${orthologyBeanList}">
        <h4>Orthology for <zfin:link entity="${formBean.marker}"/> (<zfin2:displayLocation entity="${formBean.marker}" hideLink="true"/>)</h4>
        <zfin2:orthology marker="${formBean.marker}"
                         showTitle="false"
                         hideDownloadLink="true"/>
        <hr/>
    </c:forEach>
</div>

<div style="display: inline;">
    <zfin2:pagination paginationBean="${formBean}"/>
</div>
