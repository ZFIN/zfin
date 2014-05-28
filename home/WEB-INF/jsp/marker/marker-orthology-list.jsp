<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>


<h2>Orthology List (${orthologyBeanList.size()} genes)</h2>

<c:forEach var="formBean" items="${orthologyBeanList}">

    <zfin2:orthology orthologyPresentationBean="${formBean.orthologyPresentationBean}"
                     marker="${formBean.marker}" showTitle="false"
                     webdriverPathFromRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>

    <hr/>
</c:forEach>
