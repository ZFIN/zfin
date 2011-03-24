<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="entity" class="org.zfin.ontology.PostComposedEntity" scope="request"/>

<div style="float: right">
    <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
        <tiles:putAttribute name="subjectName" value="PostComposed Term ${entity.superterm.zdbID} ${entity.subterm.zdbID}"/>
        <tiles:putAttribute name="subjectID" value="${entity.superterm.zdbID}  ${entity.subterm.zdbID}"/>
    </tiles:insertTemplate>
</div>

<table class="primary-entity-attributes">
  <tr>
    <th>Term:</th>
    <td><zfin:name entity="${entity}"/></td>
  </tr>
  <tr>
      <th>Note:</th>
      <td>
          This page represents a term created by the combination ("post-composition")
          of two ontology terms. For more information on the individual terms, click the hyperlinked name.
      </td>
  </tr>
</table>

<div class="summary">

    <zfin2:termMiniSummary term="${entity.superterm}"
               additionalCssClasses="summary horizontal-solidblock"/>
      <c:if test="${!empty entity.subterm}">
          <zfin2:termMiniSummary term="${entity.subterm}"
                   additionalCssClasses="summary horizontal-solidblock"/>
      </c:if>

</div>