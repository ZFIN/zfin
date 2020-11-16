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
                This phenotype statement describes a gene expression or antibody
                labeling pattern. Anatomy terms are combined with a gene or antibody
                and phenotype quality.
            </td>
        </tr>
    </table>

    <zfin2:subsection title="EXPRESSION ANATOMY:">
        <zfin2:termMiniSummary term="${phenotypeStatement.e1a}"/>
        <c:if test="${not empty phenotypeStatement.e1b}">
            <zfin2:termMiniSummary term="${phenotypeStatement.e1b}"/>
        </c:if>
    </zfin2:subsection>

    <zfin2:subsection title="${phenotypeStatement.gene.type == 'ATB' ? 'ANTIBODY LABELING:' : 'EXPRESSED GENE:'}">
        <zfin2:markerSummary marker="${phenotypeStatement.gene}" previousNames="${genePreviousNames}"/>
    </zfin2:subsection>

    <zfin2:subsection title="EXPRESSION QUALITY:">
        <zfin2:termMiniSummary term="${phenotypeStatement.quality}"/>
    </zfin2:subsection>

    <c:if test="${!empty tagNote}">
        <div class="ontology-term-mini-summary">
            <table class="ontology-term-mini-summary">
                <tr>
                    <th class="name">Tag:</th>
                    <td class="name">${phenotypeStatement.tag}</td>
                </tr>
                <tr>
                    <th>Definition:</th>
                    <td>${tagNote}</td>
                </tr>
            </table>
        </div>
    </c:if>
</z:page>