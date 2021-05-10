<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page bootstrap="true">
    <div class="container-fluid">
        <h2>New Publication</h2>
        <zfin2:publicationForm publicationBean="${publicationBean}" error="${error}"/>
    </div>
</z:page>