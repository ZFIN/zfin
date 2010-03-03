<%@ page import="org.zfin.gwt.marker.ui.GoEvidenceEditController" %>
<%
    String state = request.getParameter(GoEvidenceEditController.STATE_STRING);
    if(state==null){
        state = GoEvidenceEditController.Action.EDIT.name();
    }
    String zdbID = request.getParameter(GoEvidenceEditController.LOOKUP_ZDBID);
    String pubID = request.getParameter(GoEvidenceEditController.PUB_ZDBID);
    String markerID = request.getParameter(GoEvidenceEditController.GENE_ZDBID);
//    String personID = request.getParameter("personID");
%>

<script type="text/javascript">
    var MarkerProperties= {
        <%=GoEvidenceEditController.STATE_STRING%> : "<%= state%>",
    <%=GoEvidenceEditController.LOOKUP_ZDBID%> : "<%= zdbID%>",
    <%=GoEvidenceEditController.GENE_ZDBID%>: "<%= markerID %>",
    <%=GoEvidenceEditController.PUB_ZDBID%> : "<%= pubID%>"
    <%--personID: "<%= personID%>"--%>
    } ;

</script>


<jsp:include page="../../jsp-include/go_include.jsp"/>




