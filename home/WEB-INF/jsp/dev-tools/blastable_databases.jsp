<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--Based on blast_info.html and trancript_types--%>
<style type="text/css">
    div.summary li { padding-top: .1em; padding-bottom: .1em; }
</style>

<form:form method="get" commandName="formBean">
    <form:select path="selectedReferenceDatabaseZdbID">
        <option value="">-Reference Database-</option>
        <form:options items="${formBean.referenceDatabases}" itemValue="zdbID" itemLabel="view"/>
    </form:select>
    <form:select path="databaseToRemoveZdbID">
        <option value="">-Database to Remove-</option>
        <form:options items="${formBean.databases}" itemValue="zdbID" itemLabel="view"/>
    </form:select>
    <form:select path="databaseToAddZdbID">
        <option value="">-Database to Add-</option>
        <form:options items="${formBean.databases}" itemValue="zdbID" itemLabel="view"/>
    </form:select>
    <form:select path="databaseToSetAsPrimaryZdbID">
        <option>-Set Primary Blast Database-</option>
        <option value="">None</option>
        <form:options items="${formBean.databases}" itemValue="zdbID" itemLabel="view"/>
    </form:select>
    <input type="submit" value="Save Changes">
    <br>
    <div class="summary">
        <table border="1">
            <tr>
                <th>ReferenceDB: name - datatype - supertype</th>
                <th>Primary Blast DB: name(abbrev) - type </th>
                <th>Blastable DBs:  name(abbrev) - type </th>
            </tr>
            <c:forEach var="referenceDB" items="${formBean.referenceDatabases}" varStatus="index">
                <zfin2:blastableDatabases referenceDatabase="${referenceDB}"/>
            </c:forEach>
        </table>
    </div>
</form:form>



