<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ tag import="org.zfin.properties.ZfinProperties" %>

<%-- Display of marker relationships for the transcript page --%>

<%@ attribute name="blastBean" type="org.zfin.marker.presentation.BlastBean" rtexprvalue="true" required="true" %>
<html>
<body onload="jump() ; ">

<script type="text/javascript">
    function jump(){
        var thisForm =  document.getElementById("externalBlast") ;

        // todo: this is wrong it should take the value  from the input variable!!!
        thisForm.action='${blastBean.database.location}' ;
//        alert(thisForm) ;
        //            handles internal blast here
        if(thisForm.action == '' ){
            <%--thisForm.action='/<%= ZfinProperties.getWebDriver()%>' ;--%>
            thisForm.action='/action/blast/blast' ;
        }
        thisForm.submit() ;
    }
</script>

<form method="post" id="externalBlast" name="externalBlast" target="_self" style="display:inline;">

    <c:forEach var="hiddenVar" items="${blastBean.hiddenProperties}">
        <input type="hidden" id="${hiddenVar.key}" name="${hiddenVar.key}" value="${hiddenVar.value}"/>
    </c:forEach>
</form>

</body>

</html>