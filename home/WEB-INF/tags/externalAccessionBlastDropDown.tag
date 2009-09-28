<%@ tag import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="dbLink" type="org.zfin.sequence.DBLink" rtexprvalue="true" required="true" %>


<c:choose>
    <c:when test="${fn:length(dbLink.blastableDatabases)==1}">
        <input type="button" value="Blast at ${dbLink.blastableDatabases[0].displayName}"
               onclick="window.location=
        '<zfin2:blastAccessionURL dbLink="${dbLink}" blastDB="${dbLink.blastableDatabases[0]}"/>'">

    </c:when>
    <c:when test="${fn:length(dbLink.blastableDatabases)>1}">
        <SELECT method="get" onChange="
                                if (0 != this.selectedIndex){
                                    window.open(this.options[this.selectedIndex].value,parseInt(Math.random()*2000000000));
                                }
                                return true;
                            ">
            <OPTION VALUE="none" SELECTED>- Select Tool -</OPTION>
            <c:forEach var="blastDB" items="${dbLink.blastableDatabases}">
                <option value='<zfin:blastAccessionURL dbLink="${dbLink}" blastDB="${blastDB}"/>' >${blastDB.displayName}</option>
            </c:forEach>
        </select>


    </c:when>
</c:choose>
