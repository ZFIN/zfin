<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Curator "Approved" checkbox for a section header. The change handler
  in line-submission-detail.jsp POSTs the new state to
  /action/zirc/section-approval; initial checked state is loaded from
  the sectionApprovals model attribute (keyed "<recId>|<sectionName>").
--%>
<%@ attribute name="recId"       required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="sectionName" required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="approved"    required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%-- Disable the checkbox until every field under this section reads
     "Complete". Defaults to true (enabled) when omitted. --%>
<%@ attribute name="enabled"     required="false" rtexprvalue="true" type="java.lang.Boolean" %>

<c:set var="isEnabled" value="${enabled == null or enabled}"/>
<label class="form-check-inline ml-3 mb-0 small text-muted zirc-section-approval-wrapper${isEnabled ? '' : ' disabled'}"
       onclick="event.stopPropagation();"
       title="${isEnabled ? 'Mark this section as approved' : 'All fields under this section must be Complete before it can be approved'}">
    <input type="checkbox" class="form-check-input zirc-section-approval mr-1"
           data-rec-id="${recId}"
           data-section-name="${sectionName}"
           <c:if test="${approved}">checked="checked"</c:if>
           <c:if test="${not isEnabled}">disabled="disabled"</c:if>/>
    Approved
</label>
