<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<zfin2:orthology orthologyPresentationBean="${formBean.orthologyPresentationBean}"
                 marker="${formBean.marker}" title="ORTHOLOGY" update="${update}"
                 webdriverPathFromRoot="<%=ZfinPropertiesEnum.WEBDRIVER_PATH_FROM_ROOT.toString()%>"/>
