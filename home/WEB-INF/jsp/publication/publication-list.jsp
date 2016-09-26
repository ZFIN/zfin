<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" type="org.zfin.publication.presentation.ShowPublicationBean" scope="request"/>

<h3 class="primary-entity-attributes">
${formBean.entity.entityType}:
        <zfin:link entity="${formBean.entity}"/>
</h3>

<zfin2:citationList pubListBean="${formBean}" url="?">


</zfin2:citationList>
