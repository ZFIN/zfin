<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">
<script src="${zfn:getAssetPath("bootstrap.js")}"></script>

<div class="container-fluid">
    <h2>New Publication</h2>
    <zfin2:publicationForm publicationBean="${publicationBean}" error="${error}"/>
</div>