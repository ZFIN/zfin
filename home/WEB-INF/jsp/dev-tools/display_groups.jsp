<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<h3>RefDB Display Groups</h3>

<form:form commandName="formBean">
    <form:select path="displayGroupToEditID">
        <option value="">-Display Group to Edit-</option>
        <form:options items="${formBean.displayGroups}" itemLabel="groupName" itemValue="id"/>
    </form:select>
    <form:select path="referenceDatabaseToAddZdbID">
        <option value="">-Reference Database To Add-</option>
        <form:options items="${formBean.referenceDatabases}" itemValue="zdbID" itemLabel="view"/>
    </form:select>
    <form:select path="referenceDatabaseToRemoveZdbID">
        <option value="">-Reference Database To Remove-</option>
        <form:options items="${formBean.referenceDatabases}" itemValue="zdbID" itemLabel="view"/>
    </form:select>
    <input type="submit" value="Save Changed">
</form:form>

<c:forEach var="displayGroup" items="${formBean.displayGroups}">
  <div class="summary">
  <table class="summary largestripes">
  <tr class="odd oddgroup"><td width="10%">Group:</td><td> <b>${displayGroup.groupName}</b></td> </tr>
  <tr class="even evengroup newgroup"><td>Definition: </td><td><small>${displayGroup.definition}</small></td></tr>
  <tr class="odd evengroup"><td>Members:</td>
      <td>
      <c:forEach var="refDB" items="${displayGroup.referenceDatabases}" varStatus="loop">
         <small>${refDB.foreignDB.dbName} ${refDB.foreignDBDataType.dataType} <c:if test="${!loop.last}">,</c:if></small>
      </c:forEach>
  </td>
  </table>
  </div>
</c:forEach>

