<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <table class="primary-entity-attributes">
        <tr>
            <th>Phenotype:</th>
            <td><zfin:name entity="${phenotypeStatement}"/></td>
        </tr>
        <tr>
            <th>Note:</th>
            <td>
                This statement combines anatomy and/or ontology terms with phenotype quality terms to
                create a complete phenotype (EQ) statement. For detailed information on individual terms,
                click the hyperlinked term name.
            </td>

        </tr>
    </table>

    <div class="summary">
        <c:forEach var="term" items="${uniqueTerms}">
            <zfin2:termMiniSummary term="${term}" additionalCssClasses="summary horizontal-solidblock"/>
        </c:forEach>
    </div>

    <c:if test="${!empty tagNote}">
        <div class="ontology-term-mini-summary">
            <table class="ontology-term-mini-summary summary horizontal-solidblock">
                <tr>
                    <th class="name">Tag:</th>
                    <td class="name">${phenotypeStatement.tag}</td>
                </tr>
                <tr>
                    <th class="definition">Definition:</th>
                    <td>${tagNote}</td>
                </tr>
            </table>
        </div>
    </c:if>
</z:page>
