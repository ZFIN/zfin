<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>

    <h3>Report Request Received</h3>

    <h3>Credentials</h3>
    <pre>
        ${analyticsReportRequestForm.credentials}
    </pre>

    <h3>Begin Date</h3>
    <pre>
        ${analyticsReportRequestForm.begin}
    </pre>

    <h3>End Date</h3>
    <pre>
        ${analyticsReportRequestForm.end}
    </pre>

</z:page>
