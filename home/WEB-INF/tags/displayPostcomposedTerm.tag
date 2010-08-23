<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ attribute name="superTerm" type="org.zfin.ontology.Term" %>
<%@ attribute name="supTerm"  type="org.zfin.ontology.Term"%>

<c:choose>
  <c:when test="${supTerm eq null}">
    <zfin:link entity="${superTerm}"/>
  </c:when>
  <c:otherwise>
    <span class="postcomposedtermlink">
      <zfin:link entity="${superTerm}"/>&nbsp;
        <zfin:link entity="${supTerm}"/>
    </span>
  </c:otherwise>
</c:choose>
