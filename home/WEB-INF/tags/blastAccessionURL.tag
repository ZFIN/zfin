<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dbLink" type="org.zfin.sequence.DBLink" rtexprvalue="true" required="true" %>
<%@ attribute name="blastDB" type="org.zfin.sequence.blast.Database" rtexprvalue="true" required="true" %>

<c:choose>
    <c:when test="${empty blastDB.location}">
        <c:choose>
            <c:when test="${blastDB.type eq 'nucleotide'}">
                <c:set var="sequenceType" value="nt"/>
                <c:set var="program" value="blastn"/>
            </c:when>
            <c:otherwise>
                <c:set var="sequenceType" value="pt"/>
                <c:set var="program" value="blastp"/>
            </c:otherwise>
        </c:choose>
        <c:set var="blastLink" value="/action/blast/blast?&program=${program}&sequenceType=${sequenceType}&queryType=SEQUENCE_ID&dataLibraryString=RNASequences&sequenceID=${dbLink.accessionNumber}"/>
    </c:when>
    <c:otherwise>
        <c:set var="blastLink" value="${blastDB.location}${dbLink.accessionNumber}"/>
    </c:otherwise>
</c:choose>

<input type="button" value="Blast at ${dbLink.blastableDatabases[0].displayName}"
       onclick="window.open('${blastLink}');">

