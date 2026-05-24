<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
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

<%-- HTML-escape every value that lands in an attribute: label can come
     from submission / mutation names which are curator-entered, so a
     stray quote or angle bracket must not break the attribute context. --%>
<c:set var="safeLabel"       value="${fn:escapeXml(label)}"/>
<c:set var="safeRecId"       value="${fn:escapeXml(recId)}"/>
<c:set var="safeScope"       value="${fn:escapeXml(scope)}"/>
<c:set var="safeFieldName"   value="${fn:escapeXml(fieldName)}"/>
<c:set var="safeSectionName" value="${fn:escapeXml(sectionName)}"/>

<a href="javascript:void(0)" class="ml-2 text-muted field-history-trigger zirc-comments-trigger"
   title="Comments &mdash; ${safeLabel}"
   data-rec-id="${safeRecId}"
   data-scope="${safeScope}"
   data-field-name="${safeFieldName}"
   data-section-name="${safeSectionName}"
   data-label="${safeLabel}">
    <i class="far fa-comments"></i>
</a>
