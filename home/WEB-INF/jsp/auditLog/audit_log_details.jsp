<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<table width=100%>
    <tr>
        <td colspan=2><hr></td>
    </tr>
    <c:forEach var="session" items="${auditLogForm.items}">
        <tr>
            <td colspan=2>
                <TABLE cellpadding=3 width=100% border=2>
                    <tr>
                        <td width=30%>
                            <b>Submitter:</b>
                            <A HREF="/action/people/view-person-detail?person.zdbID=<c:out value='${session.owner.zdbID}'/>">
                                <c:out value="${session.owner.name}"/>
                            </A>
                        </td>
                        <td><b>Changed field:</b> <c:out value="${session.fieldName}"/></td>
                        <td><b>Date:</b>
                            <fmt:formatDate value="${session.dateUpdated}" type="date"/>
                        </td>
                    </tr>
                    <tr>
                        <td align="right" valign="top" width=40%><b>Old value:</b></td>
                        <td colspan="2" valign="top"><c:out value="${session.oldValue}"/></td>
                    </tr>
                    <tr>
                        <td align="right" valign="top" width=40%><b>New value:</b></td>
                        <td colspan="2" valign="top"><c:out value="${session.newValue}"/></td>
                    </tr>
                    <tr>
                        <td align="right" valign="top" width=40%>
                            <b>Submitter comments:</b>
                        </td>
                        <td colspan="2" valign="top">
                            <c:choose>
                                <c:when test="${session.comment != null}">
                                    <c:out value="${session.comment}" default="hs"/>
                                </c:when>
                                <c:otherwise>
                                    &nbsp;
                                </c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </TABLE>
            </td>
        </tr>
    </c:forEach>
</table>
