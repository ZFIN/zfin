<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Renders a Bootstrap badge for a LineSubmissionStatusComputer.FieldStatus.
  The status enum carries its own abbreviation, displayName, and cssClass —
  no presentation logic here. Status may be null (e.g. an inapplicable
  conditional field); in that case the tag emits nothing.
--%>
<%@ attribute name="status" required="false" rtexprvalue="true" type="java.lang.Object" %>

<c:if test="${not empty status}"><span class="badge ${status.cssClass}" title="${status.displayName}">${status.abbreviation}</span></c:if>