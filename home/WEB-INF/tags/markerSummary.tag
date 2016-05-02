<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="marker" type="org.zfin.marker.Marker" rtexprvalue="true" required="true" %>
<%@ attribute name="previousNames" type="java.util.List" rtexprvalue="true" required="false" %>
<%@ attribute name="additionalCssClasses" description="additional css class for the table" required="false" %>

<div class="ontology-term-mini-summary">
    <table class="ontology-term-mini-summary <c:if test="${!empty additionalCssClasses}">${additionalCssClasses}</c:if>">
        <c:choose>
            <c:when test="${marker.type == 'GENE' || marker.type == 'EFG'}">
                <tr>
                    <th class="name">Symbol:</th>
                    <td class="name"><zfin:link entity="${marker}" suppressPopupLink="true"/></td>
                </tr>
                <tr>
                    <th>Name:</th>
                    <td><zfin:name entity="${marker}"/></td>
                </tr>
            </c:when>
            <c:otherwise>
                <tr>
                    <th class="name">Name:</th>
                    <td class="name"><zfin:link entity="${marker}" suppressPopupLink="true"/></td>
                </tr>
            </c:otherwise>
        </c:choose>

        <c:if test="${!empty previousNames}">
            <zfin2:previousNamesFast label="Previous Name" previousNames="${previousNames}"/>
        </c:if>

        <c:if test="${marker.type == 'GENE'}">
            <tr>
                <th>Location:</th>
                <td>
                    <zfin2:displayLocation entity="${marker}" longDetail="true"/>
                </td>
            </tr>
        </c:if>
    </table>
</div>