<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="TRANSCRIPT" value="Transcript"/>

<z:dataPage
        sections="${[SUMMARY, TRANSCRIPT]}">

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">MARKER STATISTICS</div>
        </div>

        <z:section title="${TRANSCRIPT}">
            <z:section title="">
                <div class="__react-root" id="MarkerTranscriptStatTable" data-type="transcript"></div>
            </z:section>
        </z:section>

    </jsp:body>

</z:dataPage>
