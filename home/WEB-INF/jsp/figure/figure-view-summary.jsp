<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="figure" type="org.zfin.expression.Figure" scope="request"/>

<z:attributeList>
    <z:attributeListItem label="ID">
        ${figure.zdbID}
    </z:attributeListItem>

    <z:attributeListItem label="Publication">
        <zfin:link entity="${figure.publication}" longVersion="true"/> -
        ${figure.publication.title}
    </z:attributeListItem>

    <z:attributeListItem label="Other Figures">
        <zfin2:toggledLinkList collection="${otherFigures}" maxNumber="5" commaDelimited="true"/>
    </z:attributeListItem>

    <z:attributeListItem label="All Figure Page">
        <a href="/action/figure/all-figure-view/${figure.publication.zdbID}">Back to All Figure Page</a>
    </z:attributeListItem>

</z:attributeList>