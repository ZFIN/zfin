<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="blastResults" type="org.zfin.sequence.blast.results.view.BlastResultBean" rtexprvalue="true" required="true" %>


<%--mostly there should be a single hit #--%>
<c:forEach var="hit" items="${blastResults.hits}" varStatus="loop">
    <a name="${hit.hitNumber}">
    <zfin:link entity="${hit}"/>

    <span style="font-family:monospace;"><zfin:defline defLine="${hit.definition}"/></span>

   <zfin2:toggledHyperlinkList collection="${hit.genes}"
                                            id="relatedGenesData${hit.hitNumber}"
                                            maxNumber="3"
                                            showAttributionLinks="false"/>
    <zfin2:blastResultGeneData hit="${hit}"/>

    <c:if test="${!hit.markerIsHit}">
        <zfin:link entity="${hit.hitMarker}"/>
    </c:if>

    <br>


    <c:forEach var="hsp" items="${hit.highScoringPairs}">
        <a name="${hit.hitNumber}.${hsp.hspNumber}"></a>
        <span style="font-size: small;">
        Score = ${hit.score},
        Identities = ${hsp.identity}/${hsp.alignmentLength} (<fmt:formatNumber value="${hsp.identity/hsp.alignmentLength}" type="percent"/>),
        Positives = ${hsp.positive}/${hsp.alignmentLength} (<fmt:formatNumber value="${hsp.positive/hsp.alignmentLength}" type="percent"/>),
        Length = ${hsp.alignmentLength}
            Strand = ${ ( hsp.queryFrom < hsp.queryTo) ? "+" : "-"}/${ ( hsp.hitFrom < hsp.hitTo ) ? "+" : "-"}
        </span>
        <c:forEach var="alignmentLine" items="${hsp.view}">
            <pre>
Query:  ${alignmentLine.startQueryString}  ${alignmentLine.queryStrand}   ${alignmentLine.stopQueryString}
               ${alignmentLine.midlineStrand}
  Hit:  ${alignmentLine.startHitString}  ${alignmentLine.hitStrand}   ${alignmentLine.stopHitString}
            </pre>
        </c:forEach>

    </c:forEach>

    <br>


</c:forEach>

