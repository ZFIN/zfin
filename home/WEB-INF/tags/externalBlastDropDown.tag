<%@ tag import="org.zfin.sequence.blast.presentation.XMLBlastBean" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dbLink" type="org.zfin.sequence.DBLink" rtexprvalue="true" required="true" %>
<%@ attribute name="instructions" type="java.lang.String" rtexprvalue="true" required="false" %>


<form style="display:inline;">
    <c:choose>
        <c:when test="${fn:length(dbLink.referenceDatabase.relatedBlastDbs) == 1}">
            <c:set var="blastDB" value="${dbLink.blastableDatabases[0]}"/>
            <input type="button" value="Blast at ${blastDB.displayName}"
                   onclick="window.location=calculateURL(${(empty blastDB.location ? blastDB.abbrev :  blastDB.zdbID)})">
        </c:when>
        <c:when test="${fn:length(dbLink.referenceDatabase.relatedBlastDbs) > 1}">
            <SELECT id="blastDB" name="blastDB" onChange="
            {
                if(0!= this.selectedIndex) {
                    var selectedValue = this.options[this.selectedIndex].value;
                    var url ;
                    if(selectedValue.search('ZDB-BLASTDB')>=0){
                        url = '/action/blast/external-blast' ;
                        url += '?'  ;
                        url += 'accession=${dbLink.accessionNumber}'  ;
                        url += '&'  ;
                        url += 'refDB=${dbLink.referenceDatabase.zdbID}'  ;
                        url += '&'  ;
                        url += 'blastDB='+selectedValue  ;
                    }
                    else{
                        url = '/action/blast/blast' ;
                        url += '?'  ;
                        url += 'sequenceID=${dbLink.accessionNumber}'  ;
                        url += '&'  ;
                        url += 'queryType=<%=XMLBlastBean.QueryTypes.SEQUENCE_ID.toString()%>'  ;
                        url += '&'  ;
                        url += 'dataLibraryString='+selectedValue  ;
                    }
                    <c:choose>
                        <c:when test="${dbLink.referenceDatabase.foreignDBDataType.dataType eq 'Polypeptide'}">
                            url += '&'  ;
                            url += 'sequenceType=pt'  ;
                            url += '&'  ;
                            url += 'program=blastp'  ;
                        </c:when>
                        <c:otherwise>
                            url += '&'  ;
                            url += 'sequenceType=nt'  ;
                            url += '&'  ;
                            url += 'program=blastn'  ;
                        </c:otherwise>
                    </c:choose>
                    window.open(url,parseInt(Math.random()*2000000000));
                 }
            };
            ">
                <c:choose>
                    <c:when test="${!empty instructions}">
                        <OPTION VALUE="none" SELECTED>- ${instructions} -</OPTION>
                    </c:when>
                    <c:otherwise>
                        <OPTION VALUE="none" SELECTED>- Select Tool -</OPTION>
                    </c:otherwise>
                </c:choose>

                <c:forEach var="blastdb" items="${dbLink.blastableDatabases}">
                    <c:choose>
                        <c:when test="${empty blastdb.location}">
                            <OPTION VALUE="${blastdb.abbrev}"> ${blastdb.displayName}</OPTION>
                        </c:when>
                        <c:otherwise>
                            <OPTION VALUE="${blastdb.zdbID}"> ${blastdb.displayName}</OPTION>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>
            </SELECT>
        </c:when>
    </c:choose>
</form>
