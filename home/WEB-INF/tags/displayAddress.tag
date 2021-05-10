<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="address" type="org.zfin.profile.Address" required="true" %>

<c:if test="${!empty address}">

<c:if test="${!empty address.street1}">
    ${address.street1}  <br/>
</c:if>
<c:if test="${!empty address.street2}">
    ${address.street2} <br/>
</c:if>
${address.city}<c:if test="${!empty address.city}">,</c:if>
${address.stateCode} ${address.postalCode}
<c:if test="${!empty address.countryCode}">
    <br/> ${address.countryCode}
</c:if>

</c:if>
