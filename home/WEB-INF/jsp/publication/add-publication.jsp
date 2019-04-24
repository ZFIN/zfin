<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="/javascript/dist/bootstrap.bundle.css">
<script src="/javascript/dist/bootstrap.bundle.js"></script>

<div class="container-fluid">
    <h2>New Publication</h2>
    <zfin2:publicationForm publicationBean="${publicationBean}" error="${error}"/>
</div>