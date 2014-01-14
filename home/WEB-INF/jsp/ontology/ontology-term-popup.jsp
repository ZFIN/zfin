<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>
<html>
<head>

    <%-- override some of zfin.css since this will come up in an iframe --%>

    <style type="text/css">


        body {
            font-family: arial, sans-serif;
            background: white;
        }

        a.external {
            background: transparent url(/images/external.png) no-repeat scroll right center;
            padding-right: 13px;
        }

        a.xpatanat-close_link {
            color: #333333;
            font-family: sans-serif;
            font-weight: bold;
            text-decoration: none;
            font-size: large;
        }
    </style>
</head>

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
        <span class="summaryTitle">Relationships</span> (<a href="/zf_info/ontology_relationship_info.html">about</a>)
        <table class="summary horizontal-solidblock">
            <c:forEach var="relationshipPresentation" items="${termRelationships}">
                <tr id="${fn:replace(relationshipPresentation.type," ","-")}">
                    <th>
                            <%-- keep the relationship types from wrapping --%>
                            ${fn:replace(relationshipPresentation.type," ","&nbsp;")}:
                    </th>
                    <td>
                        <c:forEach var="term" items="${relationshipPresentation.items}">
                            <%--<span class="related-ontology-term" id="${term.termName}"><zfin:link
                                    entity="${term}"/></span>--%>
                            <c:choose>
                                <c:when test="${hasAddToSearchButton}">
                            <span class="related-ontology-term" id="${term.termName}"><a
                                    href="/action/ontology/term-detail-popup-button?termID=${term.zdbID}">${term.termName}</a></span>
                                </c:when>
                                <c:otherwise>
                            <span class="related-ontology-term" id="${term.termName}"><a
                                    href="/action/ontology/term-detail-popup?termID=${term.zdbID}">${term.termName}</a></span>
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
</html>
