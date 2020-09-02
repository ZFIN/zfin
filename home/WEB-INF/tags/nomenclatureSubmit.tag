<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.NameSubmission"/>
<%@ tag import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="headerText" required="true" %>
<%@ attribute name="keepPrivateOption" fragment="true" required="false" %>

<div class="container-fluid">
    <h1>${headerText}</h1>

    <c:choose>
        <c:when test="${sent}">
            <div class="alert alert-success">
                <strong>Thank you for your submission.</strong> Your submission has been forwarded to the ZFIN nomenclature coordinator who may
                contact you if additional information is required.
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-danger">
                <strong>Looks like something went wrong on our end.</strong> You can either try again later or
                <a href="mailto:<%= ZfinPropertiesEnum.NOMEN_COORDINATOR.value()%>">email your submission directly to us</a>.
            </div>
        </c:otherwise>
    </c:choose>

    <h3>Contact Information</h3>
    <dl>
        <dt>Name</dt>
        <dd><c:out value="${submission.name}" /></dd>
        <dt>Email</dt>
        <dd><c:out value="${submission.email2}" /></dd>
        <dt>Laboratory</dt>
        <dd><c:out value="${submission.laboratory}" /></dd>
    </dl>

    <jsp:doBody />

    <h3>Publication Status</h3>
    <dl>
        <dt>Status</dt>
        <dd><c:out value="${submission.pubStatus}" /></dd>
        <c:if test="${!empty submission.citations}">
            <dt>Citations</dt>
            <dd><c:out value="${submission.citations}" /></dd>
        </c:if>

        <jsp:invoke fragment="keepPrivateOption" />
    </dl>

    <h3>Additional Comments</h3>
    <c:out value="${submission.comments}" />

</div>
