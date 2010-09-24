<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

    <zfin2:lookup ontologyName="${formBean.ontologyName}" wildcard="false"/>

<iframe src="javascript:''" id='__gwt_historyFrame'
        style='position:absolute;width:0;height:0;border:0'></iframe>
<table style="width:550px; overflow: auto; border: 0.1px; border-style:solid">
    <tr>
        <td>
            <div id="term-info"></div>
        </td>
    </tr>
</table>

<%-- display aliases --%>
<c:if test="${formBean.actionType.name eq 'SHOW_ALIASES'}" >
    <zfin-ontology:showTerms formBean="${formBean}"  action="${formBean.actionType.name}"/>
</c:if>

<%-- display keys--%>
<c:if test="${formBean.actionType.name eq 'SHOW_KEYS'}" >
    <zfin-ontology:showKeys formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>

<%-- display values --%>
<c:if test="${formBean.actionType.name eq 'SHOW_VALUES'}" >
    <zfin-ontology:showValues formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>

<%-- display values --%>
<c:if test="${formBean.actionType.name eq 'SHOW_OBSOLETE_TERMS'}" >
    <zfin-ontology:showTerms formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>

<%-- display all terms --%>
<c:if test="${formBean.actionType.name eq 'SHOW_ALL_TERMS'}" >
    <zfin-ontology:showTerms formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>

<%-- display all relationships --%>
<c:if test="${formBean.actionType.name eq 'SHOW_RELATIONSHIP_TYPES'}" >
    <zfin-ontology:showRelationshipTypes formBean="${formBean}" action="${formBean.actionType.name}"/>
</c:if>


