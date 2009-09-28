<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--Based on blast_info.html and trancript_types--%>
<style type="text/css">
    div.summary li { padding-top: .1em; padding-bottom: .1em; }
</style>


<div class="summary">
    <c:if test="${!empty formBean.nucleotideDatabases}">
        <c:if test="${formBean.showTitle}"> <h3>Nucleotide Blast Databases</h3></c:if>

        <c:forEach var="database" items="${formBean.nucleotideDatabases}">
            <zfin2:blastDatabaseInfo database="${database}" showOnlyDefinitions="${!formBean.showTitle}"/>
            <br>
        </c:forEach>
    </c:if>
    <c:if test="${!empty formBean.proteinDatabases}">
        <c:if test="${formBean.showTitle}"> <h3>Protein Blast Databases</h3></c:if>
        <c:forEach var="database" items="${formBean.proteinDatabases}">
            <zfin2:blastDatabaseInfo database="${database}" showOnlyDefinitions="${!formBean.showTitle}"/>
            <br>
        </c:forEach>
    </c:if>
</div>

<%--if its only a single database that populate the hidden for the leaves--%>
<c:if test="${fn:length(formBean.nucleotideDatabases)+ fn:length(formBean.proteinDatabases) == 1}">
    <c:if test="${fn:length(formBean.nucleotideDatabases)==1}">
        <c:forEach var="database" items="${formBean.nucleotideDatabases}">
            <c:forEach var="leaf" items="${database.leaves}">
                <c:choose>
                    <c:when test="${!empty databaseList}">
                        <c:set var="databaseList" value="${databaseList},${leaf.abbrev}"/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="databaseList" value="${leaf.abbrev}"/>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <br>
        </c:forEach>
    </c:if>
    <input type="hidden" id="DATALIB" name="DATALIB" value="${databaseList}">
</c:if>

