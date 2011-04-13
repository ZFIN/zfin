<%@ page import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>


<div class="data-page term-detail-page">

    <zfin2:dataManager oboID="${formBean.term.oboID}"/>

    <div style="float: right;">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="${formBean.term.termName}"/>
            <tiles:putAttribute name="subjectID" value="${formBean.term.zdbID}"/>
        </tiles:insertTemplate>
    </div>


    <table class="primary-entity-attributes">

        <tr>
            <th><span class="name-label">Term&nbsp;Name:</span></th>
            <td><span class="name-value">${formBean.term.termName}</span></td>
            <authz:authorize ifAnyGranted="root">
                <td valign="top" align="right" width="5%">
                    Search:
                </td>
                <td rowspan="3" valign="top" align="right" width="5%">
                    <zfin2:lookup ontology="${formBean.term.ontology}"
                                  action="<%= LookupComposite.ACTION_TERM_SEARCH %>"
                                  wildcard="true" useIdAsTerm="true"/>
                </td>
            </authz:authorize>
        </tr>
        <tr>

            <th>Synonyms:</th>
            <td id="term-synonyms">
                <c:forEach var="alias" varStatus="loop" items="${formBean.term.sortedAliases}">
                    <span class="alias-value" title="${alias.aliasGroup.name}">${alias.alias}<c:if test="${!loop.last}"><span
                            class="alias-separator">,</span></c:if></span>
                </c:forEach>
            </td>
        </tr>

        <tr>
            <th>Definition:</th>
            <td id="term-definition">${formBean.term.definition}</td>
        </tr>

        <tr>
            <th>Ontology:</th>
            <td id="ontology-name">${formBean.term.ontology.commonName} [${formBean.term.oboID}]
                <zfin2:ontologyTermLinks term="${formBean.term}"/></td>
        </tr>

        <c:if test="${formBean.term.obsolete}">
            <tr>
                <th class="red">Obsolete</th>
                <td class="red">true</td>
            </tr>
        </c:if>
    </table>


    <div class="summary">
        <span class="summaryTitle">Relationships</span> (<a href="/zf_info/ontology_relationship_info.html">about</a>)
        <table class="summary horizontal-solidblock">
            <c:forEach var="relationshipPresentation" items="${formBean.termRelationships}">
                <tr id="${fn:replace(relationshipPresentation.type," ","-")}">
                    <th>
                            <%-- keep the relationship types from wrapping --%>
                            ${fn:replace(relationshipPresentation.type," ","&nbsp;")}:
                    </th>
                    <td>
                        <c:forEach var="term" items="${relationshipPresentation.items}">
                            <span class="related-ontology-term" id="${term.termName}"><zfin:link
                                    entity="${term}"/></span>
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>

</div>