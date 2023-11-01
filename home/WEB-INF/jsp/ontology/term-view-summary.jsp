<%@ page import="org.zfin.ontology.Ontology" %>
<%@ page import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<c:set var="term" value="${formBean.term}"/>

<z:attributeList>

    <z:attributeListItem label="Term ID">
        ${term.oboID}
    </z:attributeListItem>

    <authz:authorize access="hasRole('root')">
        <z:attributeListItem label="Term ZDB ID">
            ${term.zdbID}
        </z:attributeListItem>
    </authz:authorize>

    <z:attributeListItem label="Synonyms">
        <ul class="comma-separated" data-toggle="collapse" data-show="3">
            <c:forEach items="${term.sortedAliases}" var="alias">
                <li>${alias.alias}</li>
            </c:forEach>
        </ul>
    </z:attributeListItem>

    <z:attributeListItem label="Definition">
        ${term.definition} <zfin2:termDefinitionReferences term="${term}"/>
    </z:attributeListItem>

    <c:if test="${formBean.term.ontology.ontologyName == 'zebrafish_anatomy' && !term.obsolete}">
        <z:attributeListItem label="Appears&nbsp;at">
            <zfin:link entity="${term.start}" longVersion="true"/>
        </z:attributeListItem>
        <z:attributeListItem label="Evident&nbsp;until">
            <zfin:link entity="${term.end}" longVersion="true"/>
        </z:attributeListItem>
    </c:if>

    <z:attributeListItem label="References">
        <zfin2:toggledLinkList collection="${term.sortedXrefs}" maxNumber="3" commaDelimited="true"/>
    </z:attributeListItem>

    <c:if test="${term.obsolete}">
        <z:attributeListItem label="Obsolete">
            <span class="red">true</span>
        </z:attributeListItem>
    </c:if>

    <c:if test="${term.secondary}">
        <z:attributeListItem label="Secondary ID:">
        </z:attributeListItem>
        <z:attributeListItem label="Merged into">
            <c:forEach var="term" items="${term.secondaryMergeTerms}">
                <zfin:link entity="${term}"/>
            </c:forEach>
        </z:attributeListItem>
    </c:if>

    <c:if test="${!empty term.images}">
        <z:attributeListItem label="Figures">
            <c:forEach var="image" items="${term.images}">
                <zfin:link entity="${image}"/>
            </c:forEach>
        </z:attributeListItem>
    </c:if>

    <z:attributeListItem label="Ontology">
            <span>${term.ontology.commonName}</span>
        <zfin2:ontologyTermLinks term="${formBean.term}"/>
    </z:attributeListItem>

    <c:if test="${!empty meshID}">
        <z:attributeListItem label="Resources">
            <a href="https://ctdbase.org/detail.go?type=chem&acc=${meshID}">CTD</a>
        </z:attributeListItem>
    </c:if>



</z:attributeList>
