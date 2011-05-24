<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
     Phenotype: <zfin:name entity="${phenotypeStatement}"/>
</div>

<div class="popup-body phenotype-popup-body">
  <div>

  <c:if test="${phenotypeStatement.tag eq 'normal'}">
      <div style="margin: 1em;">
      The "(normal or recovered)" tag is used when the annotation of a normal phenotype is notable
      or when the annotation represents a recovered normal phenotype, such as that
      resulting from the addition of a morpholino or the creation of a complex
      mutant genotype.
      </div>
      <hr class="popup-divider"/>
  </c:if>

<%--
      <zfin2:termMiniSummary term="${phenotypeStatement.entity.superterm}"/>
      <c:if test="${!empty phenotypeStatement.entity.subterm}">
          <hr class="popup-divider"/>
          <zfin2:termMiniSummary term="${phenotypeStatement.entity.subterm}"/>
      </c:if>
      <hr class="popup-divider"/>

      <zfin2:termMiniSummary term="${phenotypeStatement.quality}"/>

      <c:if test="${!empty phenotypeStatement.relatedEntity}">
          <hr class="popup-divider"/>
          <zfin2:termMiniSummary term="${phenotypeStatement.relatedEntity.superterm}"/>
          <c:if test="${!empty phenotypeStatement.relatedEntity.subterm}">
              <hr class="popup-divider"/>
              <zfin2:termMiniSummary term="${phenotypeStatement.relatedEntity.subterm}"/>
          </c:if>
      </c:if>
--%>

    <c:forEach var="term" items="${uniqueTerms}" varStatus="loop">
        <c:if test="${!loop.first}">
          <hr class="popup-divider"/>
        </c:if>
        <zfin2:termMiniSummary term="${term}"/>
    </c:forEach>


  </div>
</div>


