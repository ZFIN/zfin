<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="clone" type="org.zfin.marker.Clone"
              rtexprvalue="true" required="true" %>

<%@ attribute name="isThisseProbe" type="java.lang.Boolean" rtexprvalue="true" required="true" %>


<hr width="80%"/>
<div class="summary">
<table class="summary solidblock clonedata">
    <caption>CLONE DATA:</caption>
    <tr>
        <td><b>Library:</b> ${clone.probeLibrary.name}</td>
        <td><b>Species:</b> ${clone.probeLibrary.species}</td>
        <td><b>Sex:</b> ${clone.probeLibrary.sex}</td>
    </tr>
    <tr>
        <td><b>Cloning Site:</b> ${clone.cloningSite}</td>
        <td><b>Digest:</b> ${clone.digest}</td>
        <td><b>Insert Size:</b> ${clone.insertSize}</td>
    </tr>
    <tr>
        <td><b>Vector:</b> ${clone.vector.name}</td>
        <td><b>Vector Type:</b> ${clone.vector.type}</td>
    </tr>
    <tr>
        <td><b>Polymerase:</b> ${clone.polymeraseName}</td>
        <td><b>Insert Size:</b> ${clone.insertSize}</td>
    </tr>

    <tr>
        <td colspan="3">
            <b>PCR Amplification:</b> <br>
            ${clone.pcrAmplification}
        </td>
    </tr>

    <c:if test="${!empty clone.suppliers}">
        <tr>
            <td colspan="3">
                <b>Source:</b> <br>
                <c:forEach var="supplier" items="${clone.suppliers}">
                   <zfin:link entity="${supplier.organization}"/>
                   <small>
                        (<a href="${supplier.orderURL}${supplier.accNum}">${supplier.organization.organizationOrderURL.hyperlinkName}</a>)
                   </small>
                </c:forEach>
            </td>
        </tr>
    </c:if>
    <c:if test="${!empty clone.rating}">
        <tr>
            <td colspan="3">
                <a href="/zf_info/stars.html"><b>Quality:</b></a>
                <img src="/images/${clone.rating+1}0stars.gif" alt="Rating ${clone.rating +1}">
                (
                <c:choose>
                    <c:when test="${clone.rating eq 0}">Probe is difficult to use. Generally basal level of expression with more intense labeling in particular structure. </c:when>
                    <c:when test="${clone.rating eq 1}">Weak expression pattern</c:when>
                    <c:when test="${clone.rating eq 2}">Moderate expression pattern</c:when>
                    <c:when test="${clone.rating eq 3}">Nice strong expression pattern</c:when>
                    <c:when test="${clone.rating eq 4}">Simple to use, intense expression pattern restricted to a few structures</c:when>
                </c:choose>
                )
            </td>
        </tr>
    </c:if>

    <c:if test="${isThisseProbe}">
        <tr>
            <TD colspan="3" nowrap><a href="/ZFIN/Methods/ThisseProtocol.html"><b>Thisse <i>in situ </i> hybridization protocol</b></a></TD>
        </tr>
    </c:if>


</table>
</div>