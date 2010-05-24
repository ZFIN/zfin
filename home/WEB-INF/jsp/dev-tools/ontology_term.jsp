<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.ontology.presentation.OntologyBean" scope="request"/>

<h2>Term Detail: ${formBean.term.termName}</h2>

<table class="searchresults">
    <tr class="odd">
        <td class="bold" width="200">Term Name</td>
        <td>${formBean.term.termName}</td>
    </tr>
    <tr>
        <td class="bold">Ontology</td>
        <td>${formBean.term.ontology}</td>
    </tr>
    <tr class="odd">
        <td class="bold">OBO ID</td>
        <td>${formBean.term.oboID}</td>
    </tr>
    <tr>
        <td class="bold">Term ID</td>
        <td>${formBean.term.ID}</td>
    </tr>
    <tr class="odd">
        <td class="bold">Definition</td>
        <td>
            <c:choose>
                <c:when test="${formBean.term.definition eq null}">
                    --
                </c:when>
                <c:otherwise>
                    ${formBean.term.definition}
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr>
        <td class="bold">Comment</td>
        <td>
            <c:choose>
                <c:when test="${formBean.term.comment eq null}">
                    --
                </c:when>
                <c:otherwise>
                    ${formBean.term.comment}
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr class="odd">
        <td class="bold">Secondary Term</td>
        <td>${formBean.term.secondary}</td>
    </tr>
    <tr>
        <td class="bold">Root Term</td>
        <td>${formBean.term.root}</td>
    </tr>
    <tr class="odd">
        <td class="bold">Anonymous Term</td>
        <td>${formBean.term.obsolete}</td>
    </tr>
    <tr>
        <td class="bold">Synonyms</td>
        <td>
            <%--
                        <zfin:createDelimitedList collectionEntity="${formBean.term.aliases}" delimiter=","/>
            --%>
            <c:forEach var="alias" items="${formBean.term.aliases}">
                ${alias.alias},
            </c:forEach>
        </td>
    </tr>
    <c:if test="${formBean.term.obsolete}">
        <tr>
            <td class="bold red">Obsolete</td>
            <td class="red">true</td>
        </tr>
    </c:if>
</table>
<p></p>

<h2 class="sectionTitle search-result-table-entries">Relationships:</h2>
<table>
    <tr>
        <td class="bold" width="200" style="text-align:left">Parents
        </td>
    </tr>
    <c:forEach var="relationship" items="${formBean.term.relatedTerms}">
        <c:if test="${relationship.termTwo eq formBean.term}">
            <tr>
                <td style="text-align:right">
                    <fn${relationship.relationshipType}&nbsp;
                </td>
                <td><zfin:link entity="${relationship.termOne}"/></td>
            </tr>
        </c:if>
    </c:forEach>
    <tr>
        <td class="bold" style="text-align:left">Immediate Children
            (${fn:length(formBean.term.relatedTerms)})
        </td>
    </tr>
    <c:forEach var="relationship" items="${formBean.term.relatedTerms}">
        <c:if test="${relationship.termOne eq formBean.term}">
            <tr>
                <td style="text-align:right">${relationship.relationshipType}&nbsp; </td>
                <td><zfin:link entity="${relationship.termTwo}"/></td>
            </tr>
        </c:if>
    </c:forEach>
</table>

<h3>All Children (${fn:length(formBean.allChildren)}):</h3>
<table>
    <tr>
        <td class="bold" width="150" style="text-align:left">Children</td>
        <td class="bold" width="300" style="text-align:left">Term</td>
        <td class="bold" style="text-align:left">Distance</td>
    </tr>
    <c:forEach var="childTermTransitiveClosure" items="${formBean.allChildren}">
        <tr>
            <td>&nbsp;</td>
            <td><zfin:link entity="${childTermTransitiveClosure.child}"/></td>
            <td>${childTermTransitiveClosure.distance}</td>
        </tr>
    </c:forEach>
</table>
