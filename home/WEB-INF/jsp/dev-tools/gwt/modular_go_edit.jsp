
<%--<authz:authorize ifAnyGranted="root">--%>
<%
//    String zdbID = request.getParameter("zdbID");
//    if(zdbID==null){
//        zdbID = "ZDB-GENE-001103-15" ;
//    }

    String pubID = request.getParameter("pubID");
    if(pubID ==null){
        pubID = "ZDB-PUB-070210-20" ;
    }


//    String goID = request.getParameter("goID");
//    if(goID==null){
//        goID = "ZDB-MRKRGOEV-100107-241926";
//    }

    String zdbID = request.getParameter("zdbID");
    if(zdbID ==null){
//        zdbID = "ZDB-MRKRGOEV-031121-24";
        zdbID = "AlternateZDB-MRKRGOEV-090309-5";
    }

    String personID = request.getParameter("personID");
    if(personID==null){
        personID = "ZDB-PERS-960805-676";
    }
%>

<script type="text/javascript">
    var MarkerProperties= {
        zdbID : "<%= zdbID%>",
        pubID : "<%= pubID%>"
        <%--goID : "<%= goID%>"--%>
    } ;

</script>


<jsp:include page="../../../jsp-include/modular_go_edit_include.jsp"/>




