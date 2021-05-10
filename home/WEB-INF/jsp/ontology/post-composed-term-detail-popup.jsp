<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div class="popup-header">
     Term: <zfin:name entity="${entity}"/>
</div>

<div class="popup-body phenotype-popup-body">
  <div>
      <zfin2:termMiniSummary term="${entity.superterm}"/>
      <c:if test="${!empty entity.subterm}">
          <hr class="popup-divider"/>
          <zfin2:termMiniSummary term="${entity.subterm}"/>
      </c:if>
  </div>
</div>