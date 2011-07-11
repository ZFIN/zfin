<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="cloneBean" type="org.zfin.marker.presentation.CloneBean"
              rtexprvalue="true" required="true" %>

<c:set var="clone" value="${cloneBean.clone}"/>

<%--<%@ attribute name="isThisseProbe" type="java.lang.Boolean" rtexprvalue="true" required="true" %>--%>

<%-- this is intended to be a fragment of the primary entity attributes table called from within
cloneHead.tag ... that's why there's no table tag here, just rows --%>

<tr>
    <th>Species:</th>
    <td>${clone.probeLibrary.species}</td>
</tr>

<tr>
    <th>Library:</th>
    <td>${clone.probeLibrary.name}</td>
</tr>


<c:choose>
    <c:when test="${clone.rnaClone}">
        <tr>
            <th>Digest:</th>
            <td>${clone.digest}</td>
        </tr>

        <tr>
            <th>Insert Size:</th>
            <td>${clone.insertSize}</td>
        </tr>
        <tr>
            <th>Cloning Site:</th>
            <td>${clone.cloningSite}</td>
        </tr>
    </c:when>
    <c:otherwise>
        <tr>
            <th>Strain:</th>
            <td><zfin:link entity="${clone.probeLibrary.strain}"/></td>
        </tr>
        <tr>
            <th>Sex:</th>
            <td>${clone.probeLibrary.sex}</td>
        </tr>
        <tr>
            <th>Tissue:</th>
            <td><zfin:link entity="${clone.probeLibrary.tissue}"/></td>
            <%--<td>${clone.probeLibrary.tissue}</td>--%>
                <%--<td>${clone.probeLibrary.tissue.termName}</td>--%>
        </tr>
        <tr>
            <th>Host:</th>
            <td>${clone.probeLibrary.host}</td>
        </tr>
    </c:otherwise>
</c:choose>

<tr>
    <th>Vector Type:</th>
    <td>${clone.vector.type}</td>
</tr>

<tr>
    <th>Vector:</th>
    <td>${clone.vector.name}</td>
</tr>

<c:if test="${clone.rnaClone}">
    <tr>
        <th>Polymerase:</th>
        <td>${clone.polymeraseName}</td>
    </tr>
    <tr>
        <th>PCR Amplification:</th>
        <td>${clone.pcrAmplification}</td>
    </tr>
</c:if>

<c:if test="${!empty cloneBean.suppliers}">
    <tr>
        <th>Source:</th>
        <td>
            <c:forEach var="supplier" items="${cloneBean.suppliers}">
                <div>
                    ${supplier.linkWithAttributionAndOrderThis}
                    <%--<zfin:link entity="${supplier.organization}"/>--%>
                    <%--<small>--%>
                        <%--(<a href="${supplier.orderURL}${supplier.accNum}">${supplier.organization.organizationOrderURL.hyperlinkName}</a>)--%>
                    <%--</small>--%>
                </div>
            </c:forEach>
        </td>
    </tr>
</c:if>

<c:if test="${clone.rnaClone && !empty clone.rating}">
    <tr>
        <th>
            <a href="/zf_info/stars.html">Quality:</a>
        </th>
        <td>
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

<c:if test="${cloneBean.thisseProbe}">
    <tr>
        <th>Protocol:</th>
        <td>
            <a href="/ZFIN/Methods/ThisseProtocol.html"><b>Thisse <i>in situ </i> hybridization protocol</b></a>
        </td>
    </tr>
</c:if>