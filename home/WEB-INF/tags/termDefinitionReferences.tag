<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="term" type="org.zfin.ontology.Term" required="true" %>

<c:choose>
    <c:when test="${term.definitionReferences ne null && term.definitionReferences.size() > 0}">
        <c:choose>
            <c:when test="${term.definitionReferences.size() == 1}">
                <c:set var="reference" value="${term.definitionReferences.iterator().next()}"/>
                <c:choose>
                    <c:when test="${jspFunctions.isZfinData(reference.reference)}">
                        (<a href="/${reference.reference}">1</a>)
                    </c:when>
                    <c:otherwise>
                        <a href="${term.getReferenceLink()}">${reference.reference}</a>
                    </c:otherwise>
                </c:choose>
            </c:when>
            <c:otherwise>
                (<a href="/action/ontology/term-citations/${term.oboID}">${term.definitionReferences.size()}</a>)
            </c:otherwise>
        </c:choose>
    </c:when>
</c:choose>


