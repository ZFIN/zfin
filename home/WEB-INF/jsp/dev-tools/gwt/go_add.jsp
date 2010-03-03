<%@ page import="org.zfin.gwt.marker.ui.GoEvidenceEditController" %>
<%--<authz:authorize ifAnyGranted="root">--%>
<%
    //    String zdbID = request.getParameter("zdbID");
//    if(zdbID==null){
//        zdbID = "ZDB-GENE-001103-15" ;
//    }

    String pubID = request.getParameter(GoEvidenceEditController.PUB_ZDBID);
    if(pubID ==null){
        pubID = "ZDB-PUB-070210-20" ;
    }


    String markerID = request.getParameter(GoEvidenceEditController.GENE_ZDBID);
    if(markerID==null){
        markerID = "ZDB-GENE-031118-121";
    }

    String zdbID = request.getParameter(GoEvidenceEditController.LOOKUP_ZDBID);
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
        <%=GoEvidenceEditController.STATE_STRING%>: "<%=GoEvidenceEditController.Action.ADD%>",
    <%=GoEvidenceEditController.LOOKUP_ZDBID%>: "ADD-MRKRGOEV" ,
    <%=GoEvidenceEditController.PUB_ZDBID%>: "<%= pubID%>",
    <%=GoEvidenceEditController.GENE_ZDBID%>: "<%= markerID %>"
    <%--goID : "<%= goID%>"--%>
    } ;

</script>


<jsp:include page="../../../jsp-include/go_include.jsp"/>




