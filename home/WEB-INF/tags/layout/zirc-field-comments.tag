<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Trigger icon for the per-field / per-section curator <-> submitter
  comment thread. Clicking opens the shared #zircCommentsModal which
  reads its target context from this trigger's data-* attributes.

  Used for BOTH scopes: pass scope="field" with fieldName, or
  scope="section" with sectionName. The label is the human-readable
  title shown in the modal header.
--%>
<%@ attribute name="recId"       required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="scope"       required="true"  rtexprvalue="true" type="java.lang.String" %>
<%-- For scope="field" --%>
<%@ attribute name="fieldName"   required="false" rtexprvalue="true" type="java.lang.String" %>
<%-- For scope="section" --%>
<%@ attribute name="sectionName" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="label"       required="true"  rtexprvalue="true" type="java.lang.String" %>

<a href="javascript:void(0)" class="ml-2 text-muted field-history-trigger zirc-comments-trigger"
   title="Comments &mdash; ${label}"
   data-rec-id="${recId}"
   data-scope="${scope}"
   data-field-name="${fieldName}"
   data-section-name="${sectionName}"
   data-label="${label}">
    <i class="far fa-comments"></i>
</a>
