<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<c:set var="SUMMARY" value="Summary"/>
<c:set var="TRANSCRIPT" value="Transcript"/>
<c:set var="TRANSCRIPT_SUMMARY" value="Transcript Summary"/>
<c:set var="PLASMIDS" value="Plasmids and Pathways"/>

<z:dataPage
        sections="${[SUMMARY, TRANSCRIPT, PLASMIDS]}">

    <jsp:body>

        <div id="${zfn:makeDomIdentifier(SUMMARY)}">
            <div class="small text-uppercase text-muted">MARKER STATISTICS</div>
        </div>

        <z:section title="${TRANSCRIPT_SUMMARY}">
            <z:section title="">
                <div class="__react-root" id="MarkerStatisticTable" data-type="transcript-header"></div>
            </z:section>
        </z:section>

        <z:section title="${TRANSCRIPT}">
            <z:section title="">
                <div class="__react-root" id="MarkerStatisticTable" data-type="transcript"></div>
            </z:section>
        </z:section>

        <z:section title="${PLASMIDS}">
            <z:section title="">
                <div class="__react-root" id="MarkerStatisticTable" data-type="plasmids"></div>
            </z:section>
        </z:section>

    </jsp:body>

</z:dataPage>
