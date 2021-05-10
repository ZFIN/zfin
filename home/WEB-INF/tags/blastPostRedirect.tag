<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%-- Display of marker relationships for the transcript page --%>

<%@ attribute name="blastBean" type="org.zfin.marker.presentation.BlastBean" rtexprvalue="true" required="true" %>
<html>
<body onload="jump() ; ">

<script type="text/javascript">
    function jump(){
        var thisForm =  document.getElementById("externalBlast") ;

        // added a hack for ensembl to prevent it to send the post to its mirror.  We only want this
        // for the post as using the mirror behavior is correct
        thisForm.action='${blastBean.database.location}${ (blastBean.database.abbrev
        eq "ENSEMBL" ? "&redirect=mirror" : "" )}' ;
//        alert(thisForm) ;
        //            handles internal blast here
        if(thisForm.action == '' ){
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