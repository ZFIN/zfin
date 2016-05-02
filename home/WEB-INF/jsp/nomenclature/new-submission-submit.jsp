<jsp:useBean id="submission" scope="request" type="org.zfin.nomenclature.NameSubmission"/>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<link rel="stylesheet" href="/css/bootstrap3/css/bootstrap.css"/>
<link rel="stylesheet" href="/css/zfin-bootstrap-overrides.css"/>

<div class="container-fluid">
    <h1><tiles:getAsString name="headerText" /></h1>

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
