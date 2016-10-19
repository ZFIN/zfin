<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="image" type="org.zfin.expression.Image" rtexprvalue="true" required="true"  %>

<c:if test="${!empty image.terms}">
    <c:set var="title">
        <zfin:choice choicePattern="0#Amatomy Terms | 1#Anatomy Term | 2#Anatomy Terms" collectionEntity="${image.terms}"/>
    </c:set>
    <zfin2:subsection title="${title}" inlineTitle="true">
        <zfin2:delimitedLinks items="${image.terms}"/>
    </zfin2:subsection>
</c:if>
