<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="referenceDatabase" type="org.zfin.sequence.ReferenceDatabase" rtexprvalue="true" required="true" %>

<tr>
    <%--reference database--%>
    <td align="left"  valign="top">
        ${referenceDatabase.foreignDB.dbName}
        -
        ${referenceDatabase.foreignDBDataType.dataType}
        -
        ${referenceDatabase.foreignDBDataType.superType}
    </td>
    <%--primary blast db--%>
    <td align="left"  valign="top">
        <c:if test="${not empty referenceDatabase.primaryBlastDatabase}">
            <c:set var="database" value="${referenceDatabase.primaryBlastDatabase}"/>
            ${database.name} (${database.abbrev}) <zfin2:databaseOriginationColor origination="${database.origination}"/>
        </c:if>
    </td>

    <%--blastabase dbs--%>
    <td>
        <ul>
            <c:forEach var="blastableDatabase" items="${referenceDatabase.orderedRelatedBlastDB}">
                <li>${blastableDatabase.name} (${blastableDatabase.abbrev}) <zfin2:databaseOriginationColor origination="${blastableDatabase.origination}"/> ${blastableDatabase.publicDatabase} </li>
            </c:forEach>
        </ul>
    </td>

</tr>
