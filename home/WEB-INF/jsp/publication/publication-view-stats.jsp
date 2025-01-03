<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="ZEBRASHARE" value="Zebrashare Submission Details"/>
<c:set var="DATASETS" value="Data Sets"/>
<c:set var="FIGURES" value="Figures"/>
<c:set var="GENES" value="Genes / Markers"/>
<c:set var="STRS" value="Sequence Targeting Reagents"/>
<c:set var="ANTIBODIES" value="Antibodies"/>
<c:set var="EFGs" value="Engineered Foreign Genes"/>
<c:set var="EXPRESSION" value="Expression Data"/>
<c:set var="MUTATION" value="Mutation and Transgenics"/>
<c:set var="DISEASES" value="Human Disease / Model"/>
<c:set var="FISH" value="Fish"/>
<c:set var="PROBES" value="Probes"/>
<c:set var="DIRECTLY_ATTRIBUTED_DATA" value="Directly Attributed Data"/>

<z:dataPage
        sections="${[SUMMARY, PROBES, ANTIBODIES, EXPRESSION, STRS, ZEBRASHARE, DATASETS]}">

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">PUBLICATION STATISTICS</div>
        </div>

<%--
        <z:section title="${Summary}">
            <jsp:include page="publication-view-stats-summary.jsp"/>
        </z:section>
--%>

        <z:section title="${PROBES}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="probe"></div>
            </z:section>
        </z:section>

        <z:section title="${ANTIBODIES}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="antibody"></div>
            </z:section>
        </z:section>

        <z:section title="${EXPRESSION}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="expression"></div>
            </z:section>
        </z:section>

        <z:section title="${DISEASES}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="disease"></div>
            </z:section>
        </z:section>

        <z:section title="${STRS}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="str"></div>
            </z:section>
        </z:section>

        <%--

                <z:section title="${MUTATION}">
                    <z:section title="">
                        <div class="__react-root" id="PublicationMarkerStatTable" data-type="mutation"></div>
                    </z:section>
                </z:section>

                <z:section title="${FISH}">
                    <z:section title="">
                        <div class="__react-root" id="PublicationMarkerStatTable" data-type="fish"></div>
                    </z:section>
                </z:section>

                <z:section title="${DIRECTLY_ATTRIBUTED_DATA}">
                    <z:section title="">
                        <div class="__react-root" id="PublicationMarkerStatTable" data-type="attribution"></div>
                    </z:section>
                </z:section>

        --%>

        <z:section title="${ZEBRASHARE}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="zebrashare"></div>
            </z:section>
        </z:section>

        <z:section title="${DATASETS}">
            <z:section title="">
                <div class="__react-root" id="PublicationMarkerStatTable" data-type="datasets"></div>
            </z:section>
        </z:section>

    </jsp:body>

</z:dataPage>
