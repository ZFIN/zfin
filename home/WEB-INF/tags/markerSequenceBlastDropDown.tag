<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequence" type="java.lang.String" rtexprvalue="true" required="true" %>
<%@ attribute name="databases" type="java.util.Collection" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>


<form style="display:inline;">
    <SELECT id="blastDB" name="blastDB" onChange="
            {
                if(0!= this.selectedIndex) {
                    var selectedValue = this.options[this.selectedIndex].value;
                    var url ;
                    if(selectedValue == 'RNASequences'){
                        url = '/action/blast/blast' ;
                        url += '?program=blastn&sequenceType=nt&queryType=FASTA';
                        url += '&shortAndNearlyExact=true&dataLibraryString=RNASequences&querySequence=';
                        url += '${sequence}';
                    }
                    else{
                      url = '/action/blast/blast-with-sequence' ;
                        url += '?'  ;
                        url += 'accession=${sequence}'  ;
                        url += '&'  ;
                        url += 'blastDB='+selectedValue  ;
                    }
                    window.open(url,parseInt(Math.random()*2000000000));
                 }
            }
            ">
        <c:choose>
            <c:when test="${!empty instructions}">
                <OPTION VALUE="none" SELECTED>- ${instructions} -</OPTION>
            </c:when>
            <c:otherwise>
                <OPTION VALUE="none" SELECTED>- Select Tool -</OPTION>
            </c:otherwise>
        </c:choose>

        <c:forEach var="blastDB" items="${databases}">
            <c:if test="${blastDB.abbrev ne 'MEGA BLAST'}">
                <option value="${blastDB.abbrev}">${blastDB.displayName}</option>
            </c:if>
        </c:forEach>
    </select>
</form>
