<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.NameSubmission"/>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<h1><tiles:getAsString name="headerText" /></h1>

<c:choose>
    <c:when test="${sent}">
        <p class="message success">
            Thank you for your submission. Your submission has been forwarded to the ZFIN nomenclature coordinator who may
            contact you if additional information is required.
        </p>
    </c:when>
    <c:otherwise>
        <p class="message error">
            Looks like something went wrong on our end. You can either try again later or
            <a href="mailto:<%= ZfinPropertiesEnum.NOMEN_COORDINATOR.value()%>">email your submission directly to us</a>.
        </p>
    </c:otherwise>
</c:choose>

<div class="nomenclature-submission">

    <h3>Contact Information</h3>
    <dl>
        <dt>Name</dt>
        <dd>${submission.name}</dd>
        <dt>Email</dt>
        <dd>${submission.email2}</dd>
        <dt>Laboratory</dt>
        <dd>${submission.laboratory}</dd>
    </dl>

    <tiles:insertAttribute name="submission-submit" />

    <h3>Publication Status</h3>
    <dl>
        <dt>Status</dt>
        <dd>${submission.pubStatus}</dd>
        <c:if test="${!empty submission.citations}">
            <dt>Citations</dt>
            <dd>${submission.citations}</dd>
        </c:if>
        <c:if test="${!empty submission.reserveType}">
            <dt>Reserved</dt>
            <dd>${submission.reserveType}</dd>
        </c:if>
    </dl>

    <h3>Additional Comments</h3>
    ${submission.comments}

</div>
