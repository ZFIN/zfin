<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequence" type="java.lang.String" rtexprvalue="true" required="true" %>
<%@ attribute name="databases" type="java.util.Collection" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>

<div class='btn-group'>
    <button
       class='btn btn-outline-secondary btn-sm dropdown-toggle'
       data-toggle='dropdown'
       aria-haspopup='true'
       aria-expanded='false'
    >
        Select Tool
    </button>
    <div class='dropdown-menu'>
        <c:forEach var="blastDB" items="${databases}">
            <c:if test="${!(blastDB.abbrev.value eq 'UCSC BLAT' && fn:length(sequence) < 20)}">
                <c:choose>
                    <c:when test="${blastDB.abbrev.value eq 'RNASequences'}">
                        <a class='dropdown-item'
                           href="/action/blast/blast?program=blastn&sequenceType=nt&queryType=FASTA&shortAndNearlyExact=true&dataLibraryString=RNASequences&querySequence=${sequence}">${blastDB.displayName}</a>
                    </c:when>
                    <c:when test="${blastDB.abbrev.value ne 'MEGA BLAST'}">
                        <a class='dropdown-item'
                           href="/action/blast/blast-with-sequence?accession=${sequence}&blastDB=${blastDB.abbrev.toString()}">${blastDB.displayName}</a>
                    </c:when>
                </c:choose>
            </c:if>
        </c:forEach>
    </div>
</div>
