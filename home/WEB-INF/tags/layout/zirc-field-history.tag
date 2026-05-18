<%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn"  uri="http://java.sun.com/jsp/jstl/functions" %>
<%--
  Renders a Font Awesome history icon that opens a Bootstrap modal listing
  prior values for a single field (read from the updates audit table).

  Icon is only emitted when there is at least one history entry. CSS in
  line-submission-detail.jsp hides the icon by default and reveals it on
  row hover.

  Modal IDs are derived from fieldName so multiple instances coexist on
  the page.
--%>
<%@ attribute name="fieldName" required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="label"     required="true"  rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="updates"   required="false" rtexprvalue="true" type="java.lang.Object" %>
<%-- Optional disambiguator appended to the modal id so the same field can
     appear on multiple rows (e.g. one history per Mutation) without colliding. --%>
<%@ attribute name="scope"     required="false" rtexprvalue="true" type="java.lang.String" %>

<c:if test="${not empty updates}">
    <c:set var="modalId" value="fieldUpdatesModal-${fieldName}${empty scope ? '' : '-'}${scope}"/>
    <a href="javascript:void(0)" class="ml-2 text-muted field-history-trigger" title="View change history for ${label}"
       data-toggle="modal" data-target="#${modalId}">
        <i class="fas fa-history"></i>
    </a>
    <div class="modal fade" id="${modalId}" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
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
                                <th>Person</th>
                                <th>Old Value</th>
                                <th>New Value</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${updates}" var="u">
                                <tr>
                                    <td><fmt:formatDate value="${u.whenUpdated}" pattern="yyyy-MM-dd HH:mm"/></td>
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
