<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>


<jsp:useBean id="formBean" class="org.zfin.infrastructure.presentation.ControlledVocabularyAddBean" scope="request"/>

<authz:authorize access="hasRole('root')">
  <div class="container-fluid">
    <h2 class="page-header">You have added the following new species</h2>

    <form:form id="cv-form-added" commandName="formBean" class="form-horizontal">
      <div class="form-group">
        <form:label path="termName" class="col-sm-2 control-label">Term Name: ${formBean.termName}</form:label>
      </div>
      <div class="form-group">
        <form:label path="foreignSpecies" class="col-sm-2 control-label">Foreign Species: ${formBean.foreignSpecies}</form:label>
      </div>
      <div class="form-group">
        <form:label path="nameDefinition" class="col-sm-2 control-label">Name Definition: ${formBean.nameDefinition}</form:label>
      </div>
      <div class="form-group">
        <c:if test="${!empty newlyCreatedControlledVocab}">
           <span>&nbsp;&nbsp;&nbsp;<a href="/action/infrastructure/controlled-vocabulary-delete?zdbIDToDelete=${newlyCreatedControlledVocab.zdbID}">Delete this record</a></span>
        </c:if>
      </div>
      <div class="form-group">
        <c:if test="${!empty constructComponents}">
          <div>The above species has been used in the following constructs:</div>
          <c:forEach var="constructComponent" items="${constructComponents}" varStatus="loop">
              <div><a href="/${constructComponent.constructZdbID}">${constructComponent.constructZdbID}</a></div>
          </c:forEach>
        </c:if>
      </div>
    </form:form>
  </div>
</authz:authorize>


