<%@ page import="org.zfin.gwt.marker.ui.GoEvidenceEditController" %>
<%--<authz:authorize ifAnyGranted="root">--%>
<%
    String state = request.getParameter(GoEvidenceEditController.STATE_STRING);
    if(state==null){
        state = GoEvidenceEditController.Action.EDIT.name();
    }
    String zdbID = request.getParameter(GoEvidenceEditController.LOOKUP_ZDBID);
    if(zdbID ==null){
//        zdbID = "ZDB-MRKRGOEV-031121-24";
        zdbID = "ZDB-MRKRGOEV-090309-5";
    }
    String pubID = request.getParameter(GoEvidenceEditController.PUB_ZDBID);
    String markerID = request.getParameter(GoEvidenceEditController.GENE_ZDBID);
%>


<script type="text/javascript">
    var MarkerProperties= {
        <%=GoEvidenceEditController.STATE_STRING%> : "<%=state%>",
    <%=GoEvidenceEditController.LOOKUP_ZDBID%> : "<%= zdbID%>",
    <%=GoEvidenceEditController.GENE_ZDBID%>: "<%= markerID %>",
    <%=GoEvidenceEditController.PUB_ZDBID%> : "<%= pubID%>"
    <%--pubID : "<%= pubID%>"--%>
    <%--goID : "<%= goID%>"--%>
    } ;

</script>


<jsp:include page="../../../jsp-include/go_include.jsp"/>




