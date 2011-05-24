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

<c:if test="${phenotypeStatement.tag eq 'normal'}">
  <div class="ontology-term-mini-summary">
    <table class="ontology-term-mini-summary summary horizontal-solidblock">
    <tr>
    <th class="name">Tag:</th>
    <td class="name">normal or recovered</td>
    </tr>
    <tr>
        <th class="definition">Definition:</th>
        <td>
            The "(normal or recovered)" tag is used when the annotation of a normal phenotype is notable
            or when the annotation represents a recovered normal phenotype, such as that
            resulting from the addition of a morpholino or the creation of a complex
            mutant genotype.
        </td>
    </tr>
    </table>

  </div>
</c:if>

