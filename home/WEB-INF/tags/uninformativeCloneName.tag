<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="name" type='java.lang.String' required="true" %>
<%@ attribute name="chimericClone" type='java.lang.Boolean' required="false" %>

<c:if test="${fn:contains(name,':')}">
        <c:if test="${fn:startsWith(name,'WITHDRAWN:')}">
            <p>
                    ${ chimericClone ?
                            "This clone has been withdrawn because it is chimeric."
                            :
                            "This clone has been derived from a non-zebrafish species and has been withdrawn."
                            }
                <img src="/images/warning-noborder.gif" title="Withdrawn" alt="Withdrawn" width="20" height="20" align="top">
            </p>
        </c:if>
</c:if>