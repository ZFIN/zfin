<%@ page import="org.zfin.gwt.marker.ui.GoMarkerEditController" %>
<%--<authz:authorize ifAnyGranted="root">--%>
<%
    //    String state = request.getParameter(GoMarkerEditController.STATE_STRING);
//    if(state==null){
//        state = GoMarkerEditController.Action.EDIT.name();
//    }
    String zdbID = request.getParameter(GoMarkerEditController.LOOKUP_ZDBID);
    if(zdbID ==null){
//        zdbID = "ZDB-MRKRGOEV-031121-24";
        zdbID = "ZDB-GENE-011026-1";
    }
    String pubID = request.getParameter(GoMarkerEditController.PUB_ZDBID);
    String markerID = request.getParameter(GoMarkerEditController.GENE_ZDBID);
%>


<script type="text/javascript">
    var MarkerProperties= {
        <%=GoMarkerEditController.STATE_STRING%>: "<%=GoMarkerEditController.GO_EVIDENCE_DISPLAY%>",
    <%=GoMarkerEditController.LOOKUP_ZDBID%> : "<%= zdbID%>",
    <%=GoMarkerEditController.GENE_ZDBID%>: "<%= markerID %>",
    <%=GoMarkerEditController.PUB_ZDBID%> : "<%= pubID%>"
    <%--pubID : "<%= pubID%>"--%>
    <%--goID : "<%= goID%>"--%>
    } 

</script>


<jsp:include page="../../../jsp-include/go_include.jsp"/>




