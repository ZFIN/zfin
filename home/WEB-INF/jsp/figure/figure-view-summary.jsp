<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="figure" type="org.zfin.expression.Figure" scope="request"/>

<z:attributeList>

    <z:attributeListItem label="Publication">
        ${figure.publication.shortAuthorList} ${figure.publication.title}
    </z:attributeListItem>

    <z:attributeListItem label="Figures">
        <zfin2:toggledLinkList collection="${figure.publication.figures}" maxNumber="5" commaDelimited="true"/>
    </z:attributeListItem>

    <z:attributeListItem label="All Figure Page">
        <a href="/action/figure/all-figure-view/${figure.publication.zdbID}">Show All</a>
    </z:attributeListItem>

</z:attributeList>