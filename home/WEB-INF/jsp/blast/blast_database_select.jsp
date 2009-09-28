<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--Note:  needs to have setInfo(form) function defined and databaseInfoDiv (or some other div) defined.--%>
<%--See blast.apg and FogBogz 3513.--%>
<authz:authorize ifNotGranted="root">
    <select name = "dataLibraryString" size=1 onchange="return setInfo(this);">
</authz:authorize>
<authz:authorize ifAllGranted="root">
    <select multiple="true" name = "dataLibraryString" size=10 onchange="return setInfo(this);">
</authz:authorize>
<authz:authorize ifAllGranted="root">
    <option> ==========  Nucleotide DB  ===========</option>
</authz:authorize>
<c:forEach var="database" items="${formBean.nucleotideDatabases}">
    <zfin2:blastDatabaseSelect database="${database}" selected="${formBean.dataLibraryString}"/>
</c:forEach>
<%--<authz:authorize ifAllGranted="root">--%>
    <option> =======  Protein DB  ========</option>
<%--</authz:authorize>--%>
<c:forEach var="database" items="${formBean.proteinDatabases}">
    <zfin2:blastDatabaseSelect database="${database}" selected="${formBean.dataLibraryString}"/>
</c:forEach>
</select>




