<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequence" type="java.lang.String" rtexprvalue="true" required="true" %>
<%@ attribute name="databases" type="java.util.Collection" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>


<div class="analysis_tools_box" style="display: inline-block;">
    <c:set var="blastLink" value="${sequence}"/>
    <c:set var="blastLinkPopup" value="blast${zfn:generateRandomDomID()}"/>
    <div id="${blastLink}" class="analysis_tools_box_header">
        <c:choose>
            <c:when test="${!empty instructions}">
                ${instructions}
            </c:when>
            <c:otherwise>
                Select Tool
            </c:otherwise>
        </c:choose>
    </div>

    <div id="${blastLinkPopup}" class="analysis_tools_box_popup_box">
        <c:forEach var="blastDB" items="${databases}">
          <c:if test="${!(blastDB.abbrev.value eq 'UCSC BLAT' && fn:length(sequence) < 20)}">
            <c:choose>
                <c:when test="${blastDB.abbrev.value eq 'RNASequences'}">
                    <a style="font-size: small;"
                       href="/action/blast/blast?program=blastn&sequenceType=nt&queryType=FASTA&shortAndNearlyExact=true&dataLibraryString=RNASequences&querySequence=${sequence}">${blastDB.displayName}</a>
                    <br>
                </c:when>
                <c:when test="${blastDB.abbrev.value ne 'MEGA BLAST'}">
                    <a
                            href="/action/blast/blast-with-sequence?accession=${sequence}&blastDB=${blastDB.abbrev.toString()}">${blastDB.displayName}</a>
                    <br>
                </c:when>
            </c:choose>
            <%--<a style="font-size: small;"--%>
            <%--href="<zfin:blastAccessionURL dbLink="${dbLink}" blastDB="${blastDB}"/>">${blastDB.displayName}</a>--%>
            <%--<option value="${blastDB.abbrev}">${blastDB.displayName}</option>--%>
          </c:if>
        </c:forEach>
    </div>
</div>

<script>
    jQuery(document).ready(function() {
        jQuery('#${blastLink}').click(function() {
            jQuery("#${blastLinkPopup}").slideToggle(70);
        });
    });
</script>

