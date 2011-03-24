<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="Phenotype Statement id ${phenotypeStatement.id}"/>
        <tiles:putAttribute name="subjectID" value="${phenotypeStatement.id}"/>
    </tiles:insertTemplate>
</div>
<table class="primary-entity-attributes">
  <tr>
    <th>Phenotype:</th>
    <td><zfin:name entity="${phenotypeStatement}"/></td>
  </tr>
  <tr>
      <th>Note:</th>
      <td>
          This statement combines anatomy and/or ontology terms with phenotype quality terms to
          create a complete phenotype (EQ) statement. For detailed information on individual terms,
          click the hyperlinked term name.
      </td>
  </tr>
</table>

<div class="summary">
    <c:forEach var="term" items="${uniqueTerms}">
        <zfin2:termMiniSummary term="${term}" additionalCssClasses="summary horizontal-solidblock"/>
    </c:forEach>
</div>