<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<div class="popup-body phenotype-popup-body">

    <c:if test="${hasAddToSearchButton}">
        <div style="float: right">
            <input type="button" alt="Add To Search" value="Add To Search"
                   onclick="window.parent.useTerm('${term.termName}'); ">
            &nbsp;
            &nbsp;
            <a title="Close window" class="xpatanat-close_link" onclick="window.parent.hideTerm(); "
               href="javascript:">x</a>

        </div>
    </c:if>
    <table class="primary-entity-attributes">

        <tr>
            <th width="5%"><span class="name-label">Term&nbsp;Name:</span></th>
            <td><span class="name-value">${term.termName}</span></td>

        </tr>
        <tr>

            <th>Synonyms:</th>
            <td id="term-synonyms">
                <c:forEach var="alias" varStatus="loop" items="${term.sortedAliases}">
                    <span class="alias-value" title="${alias.aliasGroup.name}">${alias.alias}<c:if test="${!loop.last}"><span
                            class="alias-separator">,</span></c:if></span>
                </c:forEach>
            </td>
        </tr>

        <tr>
            <th>Definition:</th>
            <td id="term-definition">${term.definition}</td>
        </tr>

        <c:if test="${term.ontology.ontologyName == 'zebrafish_anatomy'}">
            <tr>
                <th>Appears&nbsp;at:</th>
                <td>
                    <zfin:link entity="${term.start}" longVersion="true"/>
                </td>
            </tr>
            <tr>
                <th>Evident&nbsp;until:</th>
                <td>
                    <zfin:link entity="${term.end}" longVersion="true"/>
                </td>
            </tr>
        </c:if>

        <tr>
            <th>Ontology:</th>
            <td id="ontology-name">${term.ontology.commonName} [${term.oboID}]
                <zfin2:ontologyTermLinks term="${term}"/></td>
        </tr>

        <c:if test="${term.obsolete}">
            <tr>
                <th class="red">Obsolete</th>
                <td class="red">true</td>
            </tr>
        </c:if>
    </table>

    <p>

    <div class="summary">
        <span class="summaryTitle">Relationships<a class='popup-link info-popup-link'
                                                   href='/action/ontology/note/ontology-relationship'></a></span>
        <table class="summary horizontal-solidblock">
            <c:forEach var="relationshipPresentation" items="${termRelationships}">
                <tr id="${zfn:makeDomIdentifier(relationshipPresentation.type)}">
                    <th>
                            ${relationshipPresentation.type}:
                    </th>
                    <td>
                        <c:forEach var="term" items="${relationshipPresentation.items}">
                            <c:choose>
                                <c:when test="${hasAddToSearchButton}">
                                    <span class="related-ontology-term" id="${term.termName}">
                                        <a href="/action/ontology/term-detail-popup-button?termID=${term.zdbID}">${term.termName}</a>
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="related-ontology-term" id="${term.termName}">
                                        <a href="/action/ontology/term-detail-popup?termID=${term.zdbID}">${term.termName}</a>
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
    </div>
    <p></p>
    <c:if test="${!empty term && fn:contains(term.ontology.commonName,'Anatomy Ontology')}">
        <a href="/action/ontology/term-detail/${term.zdbID}" target="_blank">Show Anatomy Details</a>
    </c:if>
</div>
