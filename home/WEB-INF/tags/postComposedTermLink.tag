<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="superterm" type="org.zfin.ontology.Term" required="true" %>
<%@ attribute name="subterm" type="org.zfin.ontology.Term" required="false" %>

<span class="postcomposedtermlink">
    <zfin:link entity="${superterm}"/>
    <c:if test="${subterm ne null && subterm.zdbID ne null && subterm.zdbID ne ''}">
        <zfin:link entity="${subterm}"/>
    </c:if>
</span>

