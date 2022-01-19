<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ZEBRASHARE" value="Zebrashare Submission Details"/>
<c:set var="FIGURES" value="Figures"/>
<c:set var="GENES" value="Genes / Markers"/>
<c:set var="STRS" value="Sequence Targeting Reagents"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="EFGs" value="Engineered Foreign Genes"/>
<c:set var="EXPRESSION" value="Expression Data"/>
<c:set var="MUTATION" value="Mutation and Transgenics"/>
<c:set var="FISH" value="Fish"/>
<c:set var="DIRECTLY_ATTRIBUTED_DATA" value="Directly Attributed Data"/>
<c:set var="ERRATA" value="Errata and Notes"/>

<z:dataPage
        sections="${[SUMMARY, FIGURES, GENES, STRS, ANTIBODIES, EFGs, EXPRESSION, MUTATION, FISH, DIRECTLY_ATTRIBUTED_DATA, ERRATA]}">

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">PUBLICATION STATISTICS</div>
        </div>

        <z:section title="${Summary}">
            <jsp:include page="publication-view-stats-summary.jsp"/>
        </z:section>

<%--
        <z:section title="${GENES}">
            <jsp:include page="publication-view-stats-gene.jsp"/>
            <z:section title="Stat">
                <div class="__react-root" id="PublicationMarkerStatTable" data-marker-type="marker"></div>
            </z:section>
        </z:section>

        <z:section title="${STRS}">
            <jsp:include page="publication-view-stats-str.jsp"/>
            <z:section title="Stat">
                <div class="__react-root" id="PublicationMarkerStatTable" data-marker-type="str"></div>
            </z:section>
        </z:section>
--%>

        <z:section title="${STRS}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="str"></div>
            </z:section>
        </z:section>

        <z:section title="${ANTIBODIES}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="antibody"></div>
            </z:section>
        </z:section>

    </jsp:body>

</z:dataPage>
