<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Roll-up history icon: renders a Font Awesome history icon next to a section
  heading that, when clicked, opens a Bootstrap modal listing every audited
  change for any field under that section.

  Icon is suppressed when there are no updates. The modal table includes a
  Field column so curators know which row inside the section was edited.

  The `key` attribute must already be a DOM-safe slug (lower-case, hyphenated)
  — callers know the section names and pass the slug explicitly.
--%>
<%@ attribute name="key"     required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="label"   required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="updates" required="false" rtexprvalue="true" type="java.lang.Object" %>
<%-- Optional disambiguator suffix for nested sections (e.g. one "Overview"
     section under each Mutation must not collide on modal id). --%>
<%@ attribute name="scope"   required="false" rtexprvalue="true" type="java.lang.String" %>

<c:if test="${not empty updates}">
    <c:set var="modalId" value="sectionUpdatesModal-${key}${empty scope ? '' : '-'}${scope}"/>
    <a href="javascript:void(0)" class="ml-1 text-muted field-history-trigger" title="View change history for ${label}"
       data-toggle="modal" data-target="#${modalId}">
        <i class="fas fa-history"></i>
    </a>
    <div class="modal fade" id="${modalId}" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-xl" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${label} &mdash; Change History</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body">
                    <table class="table table-sm table-striped">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Field</th>
                                <th>Person</th>
                                <th>Old Value</th>
                                <th>New Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${updates}" var="u">
                                <tr>
                                    <td><fmt:formatDate value="${u.whenUpdated}" pattern="yyyy-MM-dd HH:mm"/></td>
                                    <td><code>${u.fieldName}</code><c:if test="${not empty u.recID and not (fn:startsWith(u.recID, 'ZDB-LSUB-'))}"> <span class="text-muted small">(${u.recID})</span></c:if></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty u.submitter}"><a href="/action/profile/person/view/${u.submitter.zdbID}"><c:if test="${not empty u.submitter.firstName}">${fn:substring(u.submitter.firstName, 0, 1)}. </c:if><c:out value="${u.submitter.lastName}"/></a></c:when>
                                            <c:when test="${not empty u.submitterName}"><c:out value="${u.submitterName}"/></c:when>
                                            <c:otherwise><span class="text-muted">&mdash;</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><c:choose><c:when test="${not empty u.oldValue}"><c:out value="${u.oldValue}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td>
                                    <td><c:choose><c:when test="${not empty u.newValue}"><c:out value="${u.newValue}"/></c:when><c:otherwise><span class="text-muted">&mdash;</span></c:otherwise></c:choose></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</c:if>
