<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" type="org.zfin.publication.presentation.ShowPublicationBean" scope="request"/>

<div class="titlebar">
    <h1>Citations</h1> (${formBean.numOfPublications} total)
</div>
<p/>
<span class="primary-entity-attributes">
<b>${formBean.entity.entityType}:</b>
        <zfin:link entity="${formBean.entity}"/>
</span> <br/>

<zfin2:citationList pubListBean="${formBean}" url="?">


</zfin2:citationList>
