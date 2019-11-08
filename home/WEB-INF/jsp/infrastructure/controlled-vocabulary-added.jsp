<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="${zfn:getAssetPath("bootstrap.css")}">


<jsp:useBean id="formBean" class="org.zfin.infrastructure.presentation.ControlledVocabularyAddBean" scope="request"/>

<authz:authorize access="hasRole('root')">
  <c:if test="${empty controlledVocab}">
  <div class="container-fluid">
    <h2 class="page-header">You have added the following new species</h2>

    <form:form id="cv-form-added" commandName="formBean" class="form-horizontal">
      <div class="form-group">
        <form:label path="termName" class="col-md-2 control-label">Term Name: ${formBean.termName}</form:label>
      </div>
      <div class="form-group">
        <form:label path="foreignSpecies" class="col-md-2 control-label">Foreign Species: ${formBean.foreignSpecies}</form:label>
      </div>
      <div class="form-group">
        <form:label path="nameDefinition" class="col-md-2 control-label">Name Definition: ${formBean.nameDefinition}</form:label>
      </div>
      <div class="form-group">
        <c:if test="${!empty newlyCreatedControlledVocab}">
           <span>&nbsp;&nbsp;&nbsp;<a href="/action/infrastructure/controlled-vocabulary-delete?zdbIDToDelete=${newlyCreatedControlledVocab.zdbID}">Delete this record</a></span>
        </c:if>
      </div>
    </form:form>
  </div>
  </c:if>
  <c:if test="${!empty controlledVocab}">
    <div class="form-group">Term Name: ${controlledVocab.cvTermName}</div>
    <div class="form-group">Foreign Species: ${controlledVocab.cvForeignSpecies}</div>
    <div class="form-group">Name Definition: ${controlledVocab.cvNameDefinition}</div>
    <c:if test="${!empty constructComponents}">
      <div class="form-group">The above species could not be deleted because it has been used in the following constructs:</div>
      <c:forEach var="constructComponent" items="${constructComponents}" varStatus="loop">
        <div><a href="/${constructComponent.constructZdbID}">${constructComponent.constructZdbID}</a></div>
      </c:forEach>
    </c:if>
  </c:if>
</authz:authorize>


