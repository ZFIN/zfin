<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dbLink" type="org.zfin.sequence.DBLink" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>
<%@ attribute name="minWidth" type="java.lang.String" rtexprvalue="true" required="false" %>


<div class='dropdown'>
    <a
            class='btn btn-outline-secondary btn-sm dropdown-toggle'
            href='#'
            role='button'
            data-toggle='dropdown'
            data-boundary='window'
            aria-haspopup='true'
            aria-expanded='false'
    >
        Select Tool
    </a>
    <div class='dropdown-menu'>
        <c:forEach var="blast" items="${dbLink.blastableDatabases}">
            <a class='dropdown-item' href=${blast.urlPrefix}${dbLink.accessionNumber}>
             ${blast.displayName}
            </a>
        </c:forEach>
    </div>
</div>