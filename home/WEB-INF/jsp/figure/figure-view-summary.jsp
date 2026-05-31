<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="figure" type="org.zfin.expression.Figure" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID" copyable="true">
        ${figure.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Publication">
        <zfin:link entity="${figure.publication}" longVersion="true"/> -
        ${figure.publication.title}
    </z:attributeListItem>

    <c:if test="${!empty probe}">
        <z:attributeListItem label="Probe">
            <zfin:link entity="${probe}"/>
        </z:attributeListItem>
    </c:if>

    <c:if test="${!isLargeDataPublication}">
        <z:attributeListItem label="Other Figures">
            <zfin2:toggledLinkList collection="${otherFigures}" maxNumber="5" commaDelimited="true"/>
        </z:attributeListItem>

        <c:set var="probeUrlPart" value=""/>
        <c:if test="${!empty probe}">
            <c:set var="probeUrlPart" value="?probeZdbID=${probe.zdbID}"/>
        </c:if>
        <z:attributeListItem label="All Figure Page">
            <a href="/action/publication/${figure.publication.zdbID}/all-figures${probeUrlPart}">Back to All Figure Page</a>
        </z:attributeListItem>
    </c:if>

</z:attributeList>