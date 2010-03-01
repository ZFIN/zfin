<%@ page import="org.zfin.gwt.marker.ui.GoEvidenceEditController" %>
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

    String state = request.getParameter(GoEvidenceEditController.STATE_STRING);
    if(state==null){
        state = GoEvidenceEditController.State.EDIT.name();
    }


//    String goID = request.getParameter("goID");
//    if(goID==null){
//        goID = "ZDB-MRKRGOEV-100107-241926";
//    }

    String zdbID = request.getParameter("zdbID");
    if(zdbID ==null){
//        zdbID = "ZDB-MRKRGOEV-031121-24";
        zdbID = "ZDB-MRKRGOEV-090309-5";
    }

    String personID = request.getParameter("personID");
    if(personID==null){
        personID = "ZDB-PERS-960805-676";
    }
%>


<script type="text/javascript">
    var MarkerProperties= {
        <%=GoEvidenceEditController.STATE_STRING%> : "<%=state%>",
        <%=GoEvidenceEditController.LOOKUP_ZDBID%> : "<%= zdbID%>"
    <%--pubID : "<%= pubID%>"--%>
    <%--goID : "<%= goID%>"--%>
    } ;

</script>


<jsp:include page="../../../jsp-include/go_include.jsp"/>




