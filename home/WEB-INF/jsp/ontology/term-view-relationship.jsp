<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<c:set var="term" value="${formBean.term}"/>

<z:attributeList>

    <c:forEach var="relationshipPresentation" items="${formBean.termRelationships}" varStatus="index">
        <z:attributeListItem label="${relationshipPresentation.type}">
            <zfin2:createExpandCollapseList items="${relationshipPresentation.items}" id="${index.count}"/>
        </z:attributeListItem>
    </c:forEach>

</z:attributeList>


<script type="text/javascript">
    function toggle(shortVal, longVal) {
        document.getElementById(shortVal).style.display = 'none';
        document.getElementById(longVal).style.display = 'inline';
    }
</script>


