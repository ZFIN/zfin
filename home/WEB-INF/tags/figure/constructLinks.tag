<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="figure" type="org.zfin.expression.Figure" rtexprvalue="true" required="true"  %>

<c:if test="${!empty figure.constructs}">
    <c:set var="title">
        <zfin:choice choicePattern="0#Constructs | 1#Construct | 2#Constructs" collectionEntity="${figure.constructs}"/>
    </c:set>
    <zfin2:subsection title="${title}" inlineTitle="true">
        <zfin2:delimitedLinks items="${figure.constructs}"/>
    </zfin2:subsection>
</c:if>
