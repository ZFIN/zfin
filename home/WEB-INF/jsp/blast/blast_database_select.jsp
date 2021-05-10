<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<authz:authorize access="!hasAnyRole('root')">
    <select name = "dataLibraryString" size=1 class='update-database-info'>
</authz:authorize>
<authz:authorize access="hasAnyRole('root')">
    <select multiple="true" name = "dataLibraryString" size=10 class='update-database-info'>
</authz:authorize>
<authz:authorize access="hasAnyRole('root')">
    <option value=""> ========== Nucleotide DB ===========</option>
</authz:authorize>
<c:forEach var="database" items="${formBean.nucleotideDatabases}">
    <zfin2:blastDatabaseSelect database="${database}" selected="${formBean.dataLibraryString}"/>
</c:forEach>
<option value=""> ======= Protein DB ========</option>
<c:forEach var="database" items="${formBean.proteinDatabases}">
    <zfin2:blastDatabaseSelect database="${database}" selected="${formBean.dataLibraryString}"/>
</c:forEach>
</select>




