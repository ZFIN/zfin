<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="database" type="org.zfin.sequence.blast.presentation.DatabasePresentationBean" rtexprvalue="true" required="true" %>

<td align="left"  valign="top" width="45%">
   
    <c:forEach begin="1" end="${database.indent}" step="1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</c:forEach>
    ${database.indent}) <b>${database.database.name} (${database.database.abbrev})</b>
</td>

<td  valign="top" >
 <c:if test="${database.databaseStatistics.numAccessions > database.databaseStatistics.numSequences}">
 <font color=red>
 </c:if>
    ${database.databaseStatistics.numAccessions}
     / 
    ${database.databaseStatistics.numSequences}
 <c:if test="${database.unavailable}">
     <span style="color:red;">(Unavailable)</span>
 </c:if>
</td>

<td valign="top" >
    ${database.database.description}
</td>

<td valign="top" >
    ${database.database.publicDatabase}
</td>
<td valign="top" >
    <c:choose>
        <c:when test="${database.database.origination.type eq 'CURATED'}">
            <c:set var="color" value="green"/>
        </c:when>
        <c:when test="${database.database.origination.type eq 'GENERATED'}">
            <c:set var="color" value="gray"/>
        </c:when>
        <c:when test="${database.database.origination.type eq 'LOADED'}">
            <c:set var="color" value="blue"/>
        </c:when>
        <c:when test="${database.database.origination.type eq 'EXTERNAL'}">
            <c:set var="color" value="yellow"/>
        </c:when>
        <c:otherwise>
            <c:set var="color" value="red"/>
        </c:otherwise>
    </c:choose>
    <font color="${color}">
        ${database.database.origination.type}
    </font>
</td>
