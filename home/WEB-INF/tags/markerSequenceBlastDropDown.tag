<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequence" type="java.lang.String" rtexprvalue="true" required="true" %>
<%@ attribute name="databases" type="java.util.Collection" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>


<div class="analysis_tools_box" style="display: inline-block; width: 200px;">
    <c:set var="blastLink" value="blast-popup"/>
    <c:set var="blastLinkPopup" value="blast-links"/>
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
            <c:choose>
                <c:when test="${blastDB.abbrev eq 'RNASequences'}">
                    <a style="font-size: small;"
                       href="/action/blast/blast?program=blastn&sequenceType=nt&queryType=FASTA&shortAndNearlyExact=true&dataLibraryString=RNASequences&querySequence=${sequence}">${blastDB.displayName}</a>
                    <br>
                </c:when>
                <c:when test="${blastDB.abbrev ne 'MEGA BLAST'}">
                    <a
                       href="/action/blast/blast-with-sequence?accession=${sequence}&blastDB=${blastDB.abbrev}">${blastDB.displayName}</a>
                    <br>
                </c:when>
            </c:choose>
            <%--<a style="font-size: small;"--%>
            <%--href="<zfin:blastAccessionURL dbLink="${dbLink}" blastDB="${blastDB}"/>">${blastDB.displayName}</a>--%>
            <%--<option value="${blastDB.abbrev}">${blastDB.displayName}</option>--%>
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

